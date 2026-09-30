import base64,json,tempfile,unittest,sys
from pathlib import Path
from unittest.mock import patch
sys.path.insert(0,str(Path(__file__).resolve().parents[1]))
import runtime as m
class Recovery(unittest.TestCase):
 def setUp(self):self.tmp=tempfile.TemporaryDirectory();self.r=m.Runtime(self.tmp.name)
 def tearDown(self):self.tmp.cleanup()
 def test_interrupted_command_becomes_unknown(self):
  op={'id':'c'*32,'serial':'DEVICE','status':'running','pending_command':['fastboot','flash','boot_a'],'logs':[],'completed_partitions':0};self.r.journal(op)
  restored=m.Runtime(self.tmp.name).operations[op['id']]
  self.assertEqual(restored['status'],'interrupted_unknown');self.assertIn('UNKNOWN',restored['verification'])
 def test_tampered_evidence_not_loaded_as_completed(self):
  op={'id':'d'*32,'status':'writes_completed_unverified'};e={'payload':op,'signature':base64.b64encode(self.r.key.sign(m.canonical(op))).decode(),'public_key':self.r.public_key()};e['payload']['status']='success'
  (self.r.data/'evidence'/(op['id']+'.json')).write_text(json.dumps(e));self.assertNotIn(op['id'],m.Runtime(self.tmp.name).operations)
 def test_disk_error_always_releases_executor_lock(self):
  op={'id':'e'*32,'serial':'DEVICE','status':'running','logs':[]};self.r.lock.acquire()
  with patch.object(self.r,'journal',side_effect=OSError('disk full')):self.r.worker(op,{'manifest':{'partitions':[]}})
  self.assertFalse(self.r.lock.locked());self.assertFalse(op['evidence_available'])
 def test_diagnostics_exposes_real_capabilities(self):
  d=self.r.diagnostics();self.assertFalse(d['capabilities']['odin']);self.assertTrue(d['capabilities']['signed_evidence'])
if __name__=='__main__':unittest.main()
