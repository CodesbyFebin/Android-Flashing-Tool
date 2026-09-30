"""Package raw images under a reviewed manifest. Does not create trust or claim OEM approval."""
import argparse,base64,json,zipfile
from pathlib import Path
from cryptography.hazmat.primitives.asymmetric.ed25519 import Ed25519PrivateKey
from runtime import canonical,digest,PART
p=argparse.ArgumentParser(description=__doc__)
p.add_argument('manifest',type=Path);p.add_argument('--private-key',required=True,type=Path);p.add_argument('--images',required=True,type=Path);p.add_argument('--output',required=True,type=Path)
a=p.parse_args();m=json.loads(a.manifest.read_text());key=Ed25519PrivateKey.from_private_bytes(a.private_key.read_bytes())
if m.get('schema')!='dh-flash/v1' or not m.get('bootloader_version'):p.error('Reviewed schema and bootloader_version required')
for part in m['partitions']:
 if not PART.fullmatch(part['partition']) or Path(part['file']).name!=part['file']:p.error('Unsupported partition or image name')
 part['sha256']=digest(a.images/part['file'])
with zipfile.ZipFile(a.output,'w',compression=zipfile.ZIP_STORED) as z:
 z.writestr('manifest.json',json.dumps(m,indent=2));z.writestr('manifest.sig',base64.b64encode(key.sign(canonical(m))))
 for part in m['partitions']:z.write(a.images/part['file'],part['file'])
print('SHA-256:',digest(a.output))
