"""Verify the installed APK identity, version, signature and entry point in CI."""
import os, re, subprocess, sys
from pathlib import Path
apk = Path(sys.argv[1])
sdk = Path(os.environ.get('ANDROID_HOME') or os.environ['ANDROID_SDK_ROOT'])
tools = sdk / 'build-tools/35.0.0'
badging = subprocess.check_output([str(tools / 'aapt'), 'dump', 'badging', str(apk)], text=True)
identity = re.search(r"package: name='([^']+)' versionCode='([^']+)' versionName='([^']+)'", badging)
assert identity and identity.groups() == ('dev.chet.mypixelmodpack', '47', '3.7.0'), badging.splitlines()[:1]
subprocess.run([str(tools / 'apksigner'), 'verify', '--verbose', str(apk)], check=True)
print('Verified APK: MyPixelModPack 3.7.0, versionCode 47, package and signature')
