# Selects the self-contained jtreg tests of javac: those that only compile and run (or are expected
# to fail compiling) the test files, without libraries, modules, build steps or special options
# (except -XDrawDiagnostics, which only changes javac's output format).
#   select.py <test root> <tests file>
import os
import re
import sys

root, tests_file = sys.argv[1:3]
selected = []
for d, _, files in os.walk(root):
    for f in sorted(files):
        if not f.endswith('.java'):
            continue
        path = os.path.join(d, f)
        src = open(path, encoding='utf-8', errors='replace').read()
        m = re.search(r'/\*\s*\n?\s*\*?\s*@test\b(.*?)\*/', src, re.S)
        if not m:
            continue
        tags = m.group(1)
        if re.search(r'@library|@modules|@build|@enablePreview|@requires|@ignore', tags):
            continue
        actions = ' '.join(l for l in tags.split('\n') if re.search(r'@(compile|run)', l))
        if set(re.findall(r'\s(-[\w:.=-]+)', actions)) - {'-XDrawDiagnostics'}:
            continue
        if re.search(r'@compile/fail', tags) or re.search(r'@run\s+main', tags) or re.search(r'@compile', tags):
            selected.append(os.path.abspath(path))
with open(tests_file, 'w') as out:
    out.write('\n'.join(sorted(selected)) + '\n')
print('selected %d tests' % len(selected))
