import base64, importlib.util, json, tempfile, unittest, zipfile
from pathlib import Path
from unittest.mock import patch
import sys
sys.path.insert(0,str(Path(__file__).resolve().parents[1]))
import runtime as m
from cryptography.hazmat.primitives import serialization
class Gates(unittest.TestCase):
 def setUp(self):
  self.tmp=tempfile.TemporaryDirectory();self.r=m.Runtime(self.tmp.name);self.key=m.Ed25519PrivateKey.generate();(self.r.data/'trusted-keys'/'publisher.pub').write_bytes(self.key.public_key().public_bytes(serialization.Encoding.Raw,serialization.PublicFormat.Raw))
 def tearDown(self):self.tmp.cleanup()
 def package(self,target='cheetah',partition='boot_a',tamper=False,traversal=False):
  image=b'ANDROID!test-image';manifest={'schema':'dh-flash/v1','target':target,'key_id':'publisher','bootloader_version':'rev1','partitions':[{'partition':partition,'file':'boot.img','sha256':m.hashlib.sha256(image).hexdigest()}]};p=self.r.data/'package.zip'
  with zipfile.ZipFile(p,'w') as z:
   z.writestr('manifest.json',json.dumps(manifest));z.writestr('manifest.sig',base64.b64encode(self.key.sign(m.canonical(manifest))));z.writestr('boot.img',image+b'bad' if tamper else image)
   if traversal:z.writestr('../escaped',b'x')
  return p,m.digest(p)
 def test_valid_signed_package(self):
  p,h=self.package();self.assertEqual(self.r.inspect(p,h)['signature'],'verified')
 def test_wrong_archive_hash(self):
  p,h=self.package()
  with self.assertRaises(ValueError):self.r.inspect(p,'0'*64)
 def test_tampered_image(self):
  p,h=self.package(tamper=True)
  with self.assertRaises(ValueError):self.r.inspect(p,h)
 def test_archive_traversal(self):
  p,h=self.package(traversal=True)
  with self.assertRaises(ValueError):self.r.inspect(p,h)
 def test_unsupported_partition(self):
  p,h=self.package(partition='userdata')
  with self.assertRaises(ValueError):self.r.inspect(p,h)
 def test_untrusted_signer(self):
  p,h=self.package();(self.r.data/'trusted-keys'/'publisher.pub').unlink()
  with self.assertRaises(ValueError):self.r.inspect(p,h)
 def test_device_mismatch_blocks(self):
  p,h=self.package();fw=self.r.inspect(p,h);d={'serial':'ABC','mode':'fastboot','unlocked':True,'battery':90,'codename':'other','userspace':'no'}
  with patch.object(m,'getvar',side_effect=lambda s,k:'rev1' if k=='version-bootloader' else '0x10000'):
   self.assertIn('Firmware target does not match device product',self.r.gates(d,fw))
 def test_unknown_battery_blocks(self):
  p,h=self.package();fw=self.r.inspect(p,h);d={'serial':'ABC','mode':'fastboot','unlocked':True,'battery':None,'codename':'cheetah','userspace':'no'}
  with patch.object(m,'getvar',return_value=None):self.assertTrue(self.r.gates(d,fw))
 def test_expired_approval(self):
  self.r.plans['p']={'allowed':True,'expires':0}
  with self.assertRaises(ValueError):self.r.approve('p',{'backup':True,'risk':True,'confirmation':'FLASH'})
 def test_no_writes_by_default(self):
  with patch.dict(m.os.environ,{},clear=True),patch.object(m,'run') as run:
   with self.assertRaises(ValueError):self.r.execute('p','a')
   run.assert_not_called()
 def test_abort_first_failure_and_signed_evidence(self):
  p,h=self.package();fw=self.r.inspect(p,h);op={'id':'a'*32,'serial':'ABC','firmware_sha256':h,'plan':{},'status':'running','logs':[],'completed_partitions':0};self.r.lock.acquire()
  with patch.object(m,'getvar',side_effect=lambda s,k:'cheetah' if k=='product' else 'yes'),patch.object(m,'run',return_value={'code':1,'stdout':'','stderr':'FAILED'}) as run:
   self.r.worker(op,fw);self.assertEqual(run.call_count,1)
  self.assertEqual(op['status'],'failed');e=self.r.evidence(op['id']);m.Ed25519PublicKey.from_public_bytes(base64.b64decode(e['public_key'])).verify(base64.b64decode(e['signature']),m.canonical(e['payload']))
 def test_success_is_not_boot_verification(self):
  p,h=self.package();fw=self.r.inspect(p,h);op={'id':'b'*32,'serial':'ABC','plan':{},'status':'running','logs':[],'completed_partitions':0};self.r.lock.acquire()
  with patch.object(m,'getvar',side_effect=lambda s,k:'cheetah' if k=='product' else 'yes'),patch.object(m,'run',return_value={'code':0,'stdout':'','stderr':'OKAY'}):self.r.worker(op,fw)
  self.assertEqual(op['status'],'writes_completed_unverified')
if __name__=='__main__':unittest.main()
