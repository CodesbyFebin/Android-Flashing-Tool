"""Start the local runtime and open its UI. Writes remain disabled by default."""
import subprocess,sys,time,urllib.request,webbrowser
from pathlib import Path
root=Path(__file__).resolve().parent
process=subprocess.Popen([sys.executable,str(root/'runtime.py')],cwd=root)
try:
 for attempt in range(30):
  if process.poll() is not None:raise RuntimeError('Runtime exited; inspect the terminal error')
  try:
   urllib.request.urlopen('http://127.0.0.1:8765/session',timeout=1).close();break
  except OSError:time.sleep(.3)
 else:raise RuntimeError('Runtime did not start')
 webbrowser.open('http://127.0.0.1:8765');process.wait()
except KeyboardInterrupt:process.terminate();process.wait()
except Exception:
 process.terminate();process.wait();raise
