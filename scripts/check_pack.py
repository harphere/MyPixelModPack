from pathlib import Path
import re
import xml.etree.ElementTree as ET
import zipfile
import sys

root = Path(__file__).resolve().parents[1]
src = root / 'app/src/main'
java = src / 'java'
entry = (src / 'assets/xposed_init').read_text().splitlines()
assert entry == ['dev.chet.mypixelmodpack.PackDispatcher'], entry
assert not (src / 'resources/META-INF/xposed/java_init.list').exists()
for file in java.rglob('*.java'):
    text = file.read_text()
    assert 'XSharedPreferences' not in text, file
    assert 'FeatureGate.' not in text, file
    assert 'io.github.libxposed' not in text, file
    assert 'invokeOriginalMethod' not in text, file
    package = re.search(r'^package ([\w.]+);', text, re.M)
    assert package and package.group(1).replace('.', '/') == str(file.parent.relative_to(java)), file

components = {
    'battery': 'dev.chet.batterygradient.BatteryModule',
    'cursor': 'dev.chet.gboardcursorkeys.CursorModule',
    'mobile': 'dev.chet.mobiletypeframe.MobileTypeFrame',
    'vo': 'com.chet.voserviceframe.VoServiceFrameModule',
    'opa': 'com.chet.pixelopahome.OpaModule',
    'dots': 'com.chet.navdotstyle.NavDotModule',
    'match': 'com.chet.navbarmatch.NavBarModule',
    'network': 'com.chet.networkactivity.NetworkActivityModule',
    'wake': 'com.pixeldt2w.module.DoubleTapWakeHook',
    'speaker': 'dev.chet.speakercontrol.SpeakerHook',
    'pie': 'dev.chet.chromepiestatusmatch.ChromePie',
}
dispatcher = (java / 'dev/chet/mypixelmodpack/PackDispatcher.java').read_text()
home = (java / 'dev/chet/mypixelmodpack/HomeActivity.java').read_text()
for feature, cls in components.items():
    assert (java / (cls.replace('.', '/') + '.java')).is_file(), cls
    assert f'install("{feature}", switches' in dispatcher, feature
    assert f'{{"{feature}",' in home, feature
    assert f'new {cls}()' in dispatcher, cls

# Independent appearance stores, read by settings UI and hooks.
for package, store in [('navdotstyle', 'nav_icons_v2'), ('navbarmatch', 'nav_match_v2')]:
    for name in ['MainActivity.java', 'NavDotModule.java' if package == 'navdotstyle' else 'NavBarModule.java']:
        assert f'"{store}"' in (java / f'com/chet/{package}/{name}').read_text()
assert '"features_v2"' in dispatcher and '"features_v2"' in home
assert 'Application.class, "attach", Context.class' in dispatcher
assert 'Instrumentation.class, "callApplicationOnCreate", Application.class' in dispatcher
assert 'AndroidAppHelper.currentApplication()' in dispatcher
assert 'startup.run(key,' in dispatcher
assert 'catch (Throwable error)' in dispatcher

android = '{http://schemas.android.com/apk/res/android}'
manifest = ET.parse(src / 'AndroidManifest.xml').getroot()
providers = [p.get(android + 'authorities') for p in manifest.iter('provider')]
assert len(providers) == len(set(providers)) == 4, providers
assert all(p.startswith('dev.chet.mypixelmodpack.') for p in providers)
activities = {p.get(android + 'name') for p in manifest.iter('activity')}
for name in re.findall(r'\{"\w+", "[^"]+", "([^"]+)"\}', home): assert name in activities, name
for file in (src / 'res').rglob('*.xml'): ET.parse(file)
assert (root / 'app/pack-development.jks').is_file()

if len(sys.argv) > 1:
    with zipfile.ZipFile(sys.argv[1]) as apk:
        assert apk.read('assets/xposed_init').decode().splitlines() == entry
        assert 'META-INF/xposed/java_init.list' not in apk.namelist()
assert manifest.find('application').get(android + 'name') == 'dev.chet.mypixelmodpack.UiApplication'
assert 'install("settings_entry", switches' in dispatcher
assert '"com.android.settings".equals(pkg)' in dispatcher
assert '"settings_entry"' in home
assert 'my_pixel_mod_pack_shortcut' in (java / 'dev/chet/mypixelmodpack/SettingsShortcutModule.java').read_text()
print('Verified single legacy dispatcher, eleven components, optional Settings shortcut, UI, XML and independent providers')

pack_provider = next(p for p in manifest.iter('provider') if p.get(android + 'authorities') == 'dev.chet.mypixelmodpack.settings.v2')
assert pack_provider.get(android + 'grantUriPermissions') == 'true'
assert 'takePersistableUriPermission' in (java / 'dev/chet/mypixelmodpack/PackRuntime.java').read_text()
assert 'Refresh app access' in home and 'access_diagnostics_v2' in home
assert 'ProviderVisibility.refresh' in (java / 'dev/chet/mypixelmodpack/AccessRefresh.java').read_text()
assert 'module_enabled=' in home and 'blacklist=' in home
print('Verified preserved 2.0.2 provider-access fix and diagnostics in refreshed UI')
