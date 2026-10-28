"""Build the wide-screen APK using an installed Android SDK and JDK (no Gradle)."""
from pathlib import Path
import argparse
import os
import subprocess
import zipfile

parser = argparse.ArgumentParser()
parser.add_argument('--sdk', default=os.environ.get('ANDROID_SDK_ROOT', r'D:\SDKFiles'))
parser.add_argument('--jdk', default=os.environ.get('JAVA_HOME'))
args = parser.parse_args()
if not args.jdk:
    parser.error('Set JAVA_HOME or pass --jdk pointing to JDK 17.')
root = Path(__file__).resolve().parent
sdk, jdk = Path(args.sdk), Path(args.jdk)
tools = sdk / 'build-tools/34.0.0'
android = sdk / 'platforms/android-34/android.jar'
build = root / 'build'
classes = build / 'classes'
classes.mkdir(parents=True, exist_ok=True)
env = dict(os.environ, JAVA_HOME=str(jdk))
env['PATH'] = str(jdk / 'bin') + os.pathsep + env.get('PATH', '')

def run(*command):
    subprocess.run([str(x) for x in command], env=env, check=True, cwd=build)

run(jdk / 'bin/javac.exe', '-encoding', 'UTF-8', '-source', '8', '-target', '8',
    '-classpath', android, '-d', classes, *sorted((root / 'src').rglob('*.java')))
class_jar = build / 'classes.jar'
with zipfile.ZipFile(class_jar, 'w') as jar:
    for file in sorted(classes.rglob('*.class')):
        jar.write(file, file.relative_to(classes).as_posix())
run(jdk / 'bin/java.exe', '-cp', tools / 'lib/d8.jar', 'com.android.tools.r8.D8',
    '--min-api', '28', '--lib', android, '--output', build, class_jar)
unsigned = build / 'unsigned.apk'
run(tools / 'aapt.exe', 'package', '-f', '-M', root / 'AndroidManifest.xml',
    '-I', android, '-S', root / 'res', '-F', unsigned)
with zipfile.ZipFile(unsigned, 'a', compression=zipfile.ZIP_DEFLATED) as apk:
    apk.write(build / 'classes.dex', 'classes.dex')
aligned = build / 'aligned.apk'
run(tools / 'zipalign.exe', '-f', '4', unsigned, aligned)
keystore = build / 'wide-debug.keystore'
if not keystore.exists():
    run(jdk / 'bin/keytool.exe', '-genkeypair', '-keystore', keystore,
        '-storepass', 'android', '-keypass', 'android', '-alias', 'wide-debug',
        '-dname', 'CN=CarKaraoke Wide Debug', '-keyalg', 'RSA', '-keysize', '2048', '-validity', '10000')
output = build / 'CarKaraoke-2.0.1-wide.apk'
run(jdk / 'bin/java.exe', '-jar', tools / 'lib/apksigner.jar', 'sign', '--ks', keystore,
    '--ks-pass', 'pass:android', '--key-pass', 'pass:android', '--out', output, aligned)
run(jdk / 'bin/java.exe', '-jar', tools / 'lib/apksigner.jar', 'verify', '--verbose', output)
print(output)
