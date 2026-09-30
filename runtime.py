"""Loopback-only Android device operations runtime. No shell command API."""
import base64, hashlib, json, os, re, secrets, shutil, subprocess, threading, time, uuid, zipfile
from pathlib import Path
from http.server import ThreadingHTTPServer, SimpleHTTPRequestHandler
from cryptography.hazmat.primitives.asymmetric.ed25519 import Ed25519PrivateKey, Ed25519PublicKey
from cryptography.hazmat.primitives import serialization
ROOT=Path(__file__).parent.resolve()
DATA=Path(os.environ.get('DH_FLASH_DATA',str(Path.home()/'.dh-flash')))
SERIAL=re.compile(r'^[A-Za-z0-9._:-]{1,80}$')
PART=re.compile(r'^(boot|init_boot|vendor_boot|dtbo|vbmeta|recovery)(_[ab])?$')
def canonical(x): return json.dumps(x,sort_keys=True,separators=(',',':')).encode()
def digest(path):
 h=hashlib.sha256()
 with open(path,'rb') as f:
  for chunk in iter(lambda:f.read(1048576),b''): h.update(chunk)
 return h.hexdigest()
def run(tool,args,timeout=10):
 path=shutil.which(tool)
 if not path:return {'code':127,'stdout':'','stderr':tool+' unavailable'}
 try:
  p=subprocess.run([path,*args],capture_output=True,text=True,timeout=timeout,shell=False)
  return {'code':p.returncode,'stdout':p.stdout,'stderr':p.stderr}
 except subprocess.TimeoutExpired as e:
  decode=lambda v:v.decode(errors='replace') if isinstance(v,bytes) else (v or '')
  return {'code':124,'stdout':decode(e.stdout),'stderr':decode(e.stderr)+'\nCommand timed out; device state unknown'}
 except OSError as e:return {'code':126,'stdout':'','stderr':str(e)}
def getvar(serial,key):
 r=run('fastboot',['-s',serial,'getvar',key])
 if r['code']!=0:return None
 m=re.search(r'(?:\(bootloader\)\s*)?'+re.escape(key)+r':\s*([^\r\n]+)',r['stderr']+'\n'+r['stdout'])
 return m.group(1).strip() if m else None
class Runtime:
 def __init__(self,data=DATA):
  self.data=Path(data);self.data.mkdir(parents=True,exist_ok=True,mode=0o700)
  for n in ['firmware','evidence','trusted-keys','journal']:(self.data/n).mkdir(exist_ok=True)
  key=self.data/'signer.key'
  if not key.exists():
   fd=os.open(key,os.O_WRONLY|os.O_CREAT|os.O_EXCL,0o600)
   with os.fdopen(fd,'wb') as f:f.write(Ed25519PrivateKey.generate().private_bytes(serialization.Encoding.Raw,serialization.PrivateFormat.Raw,serialization.NoEncryption()))
  self.key=Ed25519PrivateKey.from_private_bytes(key.read_bytes());self.token=secrets.token_urlsafe(32)
  self.firmware={};self.plans={};self.approvals={};self.operations={};self.lock=threading.Lock()
  for file in (self.data/'evidence').glob('*.json'):
   try:
    record=json.loads(file.read_text())
    Ed25519PublicKey.from_public_bytes(base64.b64decode(record['public_key'])).verify(base64.b64decode(record['signature']),canonical(record['payload']))
    if record['public_key']!=self.public_key():continue
    self.operations[record['payload']['id']]=record['payload']
   except Exception:pass
  for file in (self.data/'journal').glob('*.json'):
   try:
    op=json.loads(file.read_text())
    if op['id'] not in self.operations:
     op['status']='interrupted_unknown';op['verification']='Runtime stopped before signed completion. Pending command outcome is UNKNOWN. Do not resume automatically.'
     self.operations[op['id']]=op
   except (ValueError,KeyError):pass
 def diagnostics(self):
  return {'version':'1.1.0','execution_enabled':os.environ.get('DH_FLASH_ENABLE_EXECUTION')=='1','busy':self.lock.locked(),'data_directory':str(self.data),'signer_public_key':self.public_key(),'tools':{x:shutil.which(x) for x in ['adb','fastboot']},'trusted_publishers':[p.stem for p in (self.data/'trusted-keys').glob('*.pub')],'capabilities':{'device_probe':True,'signed_zip_inspection':True,'explicit_raw_partition_flash':True,'reboot':True,'signed_evidence':True,'automatic_backup':False,'root':False,'odin':False,'sparse_images':False,'post_boot_verification':False,'full_anti_rollback_verification':False}}
 def probe(self):
  devices=[]
  for tool in ['adb','fastboot']:
   result=run(tool,['devices','-l'] if tool=='adb' else ['devices'])
   if result['code']!=0:continue
   for line in result['stdout'].splitlines():
    a=line.split()
    if len(a)<2 or not SERIAL.fullmatch(a[0]) or a[0]=='List':continue
    s,state=a[:2];d={'serial':s,'mode':tool,'state':state,'model':None,'codename':None,'battery':None,'unlocked':None}
    if tool=='adb' and state=='device':
     for field,prop in [('model','ro.product.model'),('codename','ro.product.device')]:
      r=run('adb',['-s',s,'shell','getprop',prop]);d[field]=r['stdout'].strip() or None if r['code']==0 else None
     r=run('adb',['-s',s,'shell','dumpsys','battery']);m=re.search(r'level:\s*(\d+)',r['stdout']);d['battery']=int(m[1]) if m else None
    elif tool=='fastboot':
     d['codename']=getvar(s,'product');unlock=getvar(s,'unlocked');d['unlocked']=unlock=='yes' if unlock in ['yes','no'] else None
     b=getvar(s,'battery-soc');d['battery']=int(b.rstrip('%')) if b and b.rstrip('%').isdigit() else None
     d['userspace']=getvar(s,'is-userspace')
    devices.append(d)
  return {'devices':devices,'tools':{x:shutil.which(x) is not None for x in ['adb','fastboot']},'execution_enabled':os.environ.get('DH_FLASH_ENABLE_EXECUTION')=='1','signer':self.public_key()}
 def public_key(self):return base64.b64encode(self.key.public_key().public_bytes(serialization.Encoding.Raw,serialization.PublicFormat.Raw)).decode()
 def inspect(self,path,expected):
  actual=digest(path)
  if not re.fullmatch('[0-9a-f]{64}',expected or '') or actual!=expected:raise ValueError('Publisher SHA-256 required and must match the complete ZIP')
  fid=uuid.uuid4().hex;dest=self.data/'firmware'/fid;dest.mkdir()
  try:
   with zipfile.ZipFile(path) as z:
    names=z.namelist()
    if len(names)!=len(set(names)) or len(names)>64 or sum(i.file_size for i in z.infolist())>4*1024**3:raise ValueError('Archive exceeds policy or contains duplicates')
    for i in z.infolist():
     p=Path(i.filename)
     if p.is_absolute() or '..' in p.parts or '\\' in i.filename or (i.external_attr>>16)&0o170000==0o120000:raise ValueError('Unsafe archive member')
    m=json.loads(z.read('manifest.json'))
    if m.get('schema')!='dh-flash/v1' or not SERIAL.fullmatch(m.get('target','')):raise ValueError('Invalid target manifest')
    keyid=m.get('key_id','')
    if not re.fullmatch(r'[a-zA-Z0-9_-]{1,64}',keyid):raise ValueError('Invalid signing key ID')
    keypath=self.data/'trusted-keys'/(keyid+'.pub')
    if not keypath.exists():raise ValueError('Publisher key is not trusted; install an independently verified raw Ed25519 .pub key')
    Ed25519PublicKey.from_public_bytes(keypath.read_bytes()).verify(base64.b64decode(z.read('manifest.sig'),validate=True),canonical(m))
    parts=m.get('partitions',[])
    if not 1<=len(parts)<=12 or len({p['partition'] for p in parts})!=len(parts):raise ValueError('Invalid partition list')
    for p in parts:
     if not PART.fullmatch(p['partition']) or not re.fullmatch(r'[A-Za-z0-9_-]+\.img',p['file']):raise ValueError('Partition/file outside supported policy')
     if not re.fullmatch('[0-9a-f]{64}',p['sha256']):raise ValueError('Missing partition hash')
     with z.open(p['file']) as src,open(dest/p['file'],'wb') as out:shutil.copyfileobj(src,out)
     if digest(dest/p['file'])!=p['sha256']:raise ValueError('Image hash mismatch')
     with open(dest/p['file'],'rb') as f:
      if f.read(4)==b'\x3a\xff\x26\xed':raise ValueError('Sparse images are unsupported in this release')
    firmware={'id':fid,'sha256':actual,'size':path.stat().st_size,'manifest':m,'name':m.get('name','Signed firmware package'),'signature':'verified'}
    self.firmware[fid]=firmware;return firmware
  except Exception:
   shutil.rmtree(dest);raise
 def gates(self,device,fw):
  reasons=[]
  if not device: return ['Selected device is not detected']
  if device['mode']!='fastboot':reasons.append('Reboot device into bootloader using the device controls')
  if device['unlocked'] is not True:reasons.append('Bootloader unlock state is not verified')
  if device['battery'] is None or device['battery']<60:reasons.append('Machine-verified battery must be at least 60%')
  if device['codename']!=fw['manifest']['target']:reasons.append('Firmware target does not match device product')
  if device.get('userspace')!='no':reasons.append('Supported bootloader fastboot mode is not verified')
  for p in fw['manifest']['partitions']:
   f=self.data/'firmware'/fw['id']/p['file']
   if digest(f)!=p['sha256']:reasons.append('Stored image changed')
   size=getvar(device['serial'],'partition-size:'+p['partition']) if device['mode']=='fastboot' else None
   try:valid=int(size,16)>=f.stat().st_size
   except (ValueError,TypeError):valid=False
   if not valid:reasons.append('Partition size unavailable or insufficient: '+p['partition'])
  # Require a device-reported revision match, avoiding unsupported rollback guesses.
  revision=fw['manifest'].get('bootloader_version')
  if not revision or getvar(device['serial'],'version-bootloader')!=revision:reasons.append('Exact bootloader revision is not verified')
  return reasons
 def plan(self,serial,fid):
  fw=self.firmware[fid];d=next((d for d in self.probe()['devices'] if d['serial']==serial),None)
  reasons=self.gates(d,fw);p={'id':uuid.uuid4().hex,'serial':serial,'firmware_id':fid,'expires':time.time()+300,'reasons':reasons,'allowed':not reasons,'partitions':fw['manifest']['partitions']}
  self.plans[p['id']]=p;return p
 def approve(self,pid,acks):
  p=self.plans[pid]
  if not p['allowed'] or p['expires']<time.time() or acks!={'backup':True,'risk':True,'confirmation':'FLASH'}:raise ValueError('Valid plan and explicit acknowledgements required')
  a=uuid.uuid4().hex;self.approvals[a]={'plan':pid,'expires':time.time()+60};return {'approval_id':a}
 def execute(self,pid,aid):
  if os.environ.get('DH_FLASH_ENABLE_EXECUTION')!='1':raise ValueError('Execution disabled; start with DH_FLASH_ENABLE_EXECUTION=1 after reviewing the supported device policy')
  if not self.lock.acquire(False):raise ValueError('Another device operation is active')
  try:
   a=self.approvals.pop(aid,None);p=self.plans[pid]
   if not a or a['plan']!=pid or a['expires']<time.time() or p['expires']<time.time():raise ValueError('Approval expired or already consumed')
   fw=self.firmware[p['firmware_id']];d=next((d for d in self.probe()['devices'] if d['serial']==p['serial']),None)
   reasons=self.gates(d,fw)
   if reasons:raise ValueError('; '.join(reasons))
   op={'id':uuid.uuid4().hex,'serial':p['serial'],'firmware_sha256':fw['sha256'],'plan':p,'status':'running','stage':'flash','started':time.time(),'logs':[],'completed_partitions':0}
   self.operations[op['id']]=op
   threading.Thread(target=self.worker,args=(op,fw),daemon=True).start();return op
  except Exception:self.lock.release();raise
 def journal(self,op):
  path=self.data/'journal'/(op['id']+'.json');tmp=path.with_suffix('.tmp')
  with open(tmp,'w') as f:
   json.dump(op,f);f.flush();os.fsync(f.fileno())
  os.replace(tmp,path)
 def worker(self,op,fw):
  try:
   for p in fw['manifest']['partitions']:
    if getvar(op['serial'],'product')!=fw['manifest']['target'] or getvar(op['serial'],'unlocked')!='yes':raise ValueError('Identity or unlock changed')
    image=self.data/'firmware'/fw['id']/p['file']
    if digest(image)!=p['sha256']:raise ValueError('Image changed before execution')
    args=['-s',op['serial'],'flash',p['partition'],str(image)]
    op['pending_command']=['fastboot',*args];self.journal(op)
    r=run('fastboot',args,180);op['logs'].append({'at':time.time(),'argv':['fastboot',*args],**r})
    op.pop('pending_command',None);self.journal(op)
    if r['code']!=0 or 'FAILED' in r['stderr']+r['stdout']:raise ValueError('Partition write failed; stopped immediately')
    op['completed_partitions']+=1
   op['stage']='verification';op['status']='writes_completed_unverified';op['verification']='Fastboot acknowledged writes. Independent device readback/boot verification unavailable; no success claim.'
  except Exception as e:op['status']='failed';op['error']=str(e)
  finally:
   try:
    op['completed']=time.time();self.journal(op);payload=json.loads(json.dumps(op));e={'payload':payload,'signature':base64.b64encode(self.key.sign(canonical(payload))).decode(),'public_key':self.public_key(),'algorithm':'Ed25519'}
    path=self.data/'evidence'/(op['id']+'.json');tmp=path.with_suffix('.tmp')
    with open(tmp,'w') as f:
     json.dump(e,f,indent=2);f.flush();os.fsync(f.fileno())
    os.replace(tmp,path);op['evidence_available']=True
   except Exception as e:
    op['evidence_available']=False;op['evidence_error']=str(e)
   finally:self.lock.release()
 def evidence(self,oid):
  if not re.fullmatch('[a-f0-9]{32}',oid):raise ValueError('Invalid operation ID')
  return json.loads((self.data/'evidence'/(oid+'.json')).read_text())
R=None
class Handler(SimpleHTTPRequestHandler):
 def __init__(self,*a,**kw):super().__init__(*a,directory=str(ROOT/'web'),**kw)
 def json(self,obj,status=200):
  body=json.dumps(obj).encode();self.send_response(status);self.send_header('Content-Type','application/json');self.send_header('Content-Length',str(len(body)));self.send_header('Cache-Control','no-store');self.end_headers();self.wfile.write(body)
 def secure(self):
  host=self.headers.get('Host','');origin=self.headers.get('Origin')
  if host not in ['127.0.0.1:8765','localhost:8765'] or (origin and origin not in ['http://'+host]):raise ValueError('Untrusted host or origin')
  if self.headers.get('X-DH-Token')!=R.token:raise ValueError('Invalid local session token')
 def do_GET(self):
  if self.path.startswith('/api/'):
   try:
    self.secure()
    if self.path=='/api/health':return self.json({'status':'ok','version':'1.1.0'})
    if self.path=='/api/diagnostics':return self.json(R.diagnostics())
    if self.path=='/api/openapi.json':return self.json(json.loads((ROOT/'openapi.json').read_text()))
    if self.path=='/api/devices':return self.json(R.probe())
    if self.path=='/api/operations':return self.json(list(R.operations.values()))
    if self.path.startswith('/api/evidence/'):return self.json(R.evidence(self.path.split('/')[-1]))
    return self.json({'error':'Unknown endpoint'},404)
   except Exception as e:return self.json({'error':str(e)},400)
  if self.path=='/session':
   if self.headers.get('Host') not in ['127.0.0.1:8765','localhost:8765'] or self.headers.get('Sec-Fetch-Site')=='cross-site' or self.headers.get('Origin') not in [None,'http://'+self.headers.get('Host','')]:return self.json({'error':'Untrusted request'},403)
   return self.json({'token':R.token})
  return super().do_GET()
 def do_POST(self):
  try:
   self.secure();length=int(self.headers.get('Content-Length',0))
   if self.path=='/api/firmware':
    if length<=0 or length>4*1024**3:raise ValueError('Package size exceeds policy')
    path=R.data/(uuid.uuid4().hex+'.upload')
    try:
     with open(path,'wb') as f:
      remaining=length
      while remaining:
       chunk=self.rfile.read(min(1048576,remaining))
       if not chunk:raise ValueError('Incomplete upload')
       f.write(chunk);remaining-=len(chunk)
     return self.json(R.inspect(path,self.headers.get('X-Firmware-SHA256')))
    finally:path.unlink(missing_ok=True)
   if length>65536:raise ValueError('Request too large')
   b=json.loads(self.rfile.read(length))
   if self.path=='/api/plans':return self.json(R.plan(b['serial'],b['firmware_id']))
   if self.path=='/api/approvals':return self.json(R.approve(b['plan_id'],b['acknowledgements']))
   if self.path=='/api/execute':return self.json(R.execute(b['plan_id'],b['approval_id']))
   if self.path=='/api/reboot':
    if not R.lock.acquire(False):raise ValueError('Device operation is active')
    try:
     return self.reboot(b)
    finally:R.lock.release()
   if self.path=='/api/verify-evidence':
    Ed25519PublicKey.from_public_bytes(base64.b64decode(b['public_key'])).verify(base64.b64decode(b['signature']),canonical(b['payload']));return self.json({'valid':True,'trusted_local_signer':b['public_key']==R.public_key()})
   return self.json({'error':'Unknown endpoint'},404)
  except Exception as e:return self.json({'error':str(e) or type(e).__name__},400)
 def reboot(self,b):
    s=b['serial'];mode=b['mode']
    if not SERIAL.fullmatch(s) or mode not in ['bootloader','recovery','system']:raise ValueError('Invalid reboot request')
    d=next((d for d in R.probe()['devices'] if d['serial']==s),None)
    if not d:raise ValueError('Device not detected')
    if d['mode']=='fastboot' and mode!='system':raise ValueError('Only system reboot supported from fastboot')
    return self.json(run(d['mode'],['-s',s,'reboot',*([mode] if mode!='system' else [])]))
if __name__=='__main__':
 R=Runtime();print('Android Flashing Tool: http://127.0.0.1:8765',flush=True)
 ThreadingHTTPServer(('127.0.0.1',8765),Handler).serve_forever()
