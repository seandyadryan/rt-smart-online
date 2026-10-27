"""Update only this application's existing domain block in the shared Caddyfile."""
import re
import shutil
import subprocess
from pathlib import Path

target = Path('/home/ubuntu/AI-chat/Caddyfile')
fragment = Path(__file__).with_name('Caddyfile.fragment').read_text().strip()
original = target.read_text()
matches = list(re.finditer(r'^rtsmartpro\.amarlo\.online\s*\{', original, re.M))
if len(matches) != 1:
    raise SystemExit('Expected exactly one RT Smart PRO domain block; shared config was not changed.')
start = matches[0].start()
depth = 0
end = None
for index in range(original.index('{', start), len(original)):
    if original[index] == '{': depth += 1
    elif original[index] == '}':
        depth -= 1
        if depth == 0:
            end = index + 1
            break
if end is None:
    raise SystemExit('Incomplete RT Smart PRO domain block; shared config was not changed.')
updated = original[:start] + fragment + original[end:]
if updated != original:
    subprocess.run(['sudo','docker','exec','-i','ai-chat-caddy','caddy','validate',
                    '--adapter','caddyfile','--config','/dev/stdin'], input=updated, text=True, check=True)
    backup = target.with_name('Caddyfile.before-rtsmartpro-photo-upload')
    if not backup.exists(): shutil.copy2(target, backup)
    target.write_text(updated)
    subprocess.run(['sudo','docker','exec','ai-chat-caddy','caddy','reload',
                    '--config','/etc/caddy/Caddyfile'], check=True)
    print('RT Smart PRO upload limit updated; other domain blocks preserved.')
