# Compares the jtreg results of javac (the reference) and Orzo:
#   analyze.py <tests file> <javac work dir> <orzo work dir> <test root> <result file> <openjdk commit>
# A test counts if it passes with javac. A negative test (@compile/fail) counts as passed by Orzo
# if Orzo rejects the program (its diagnostics can't match javac's reference output).
import collections
import datetime
import os
import re
import sys

tests_file, javac_work, orzo_work, test_root, result_file, commit = sys.argv[1:7]
tests = [l.strip() for l in open(tests_file) if l.strip()]


def jtr(work, test):
    rel = os.path.relpath(test, test_root)
    path = os.path.join(work, rel[:-len('.java')] + '.jtr')
    if not os.path.exists(path):
        return None
    return open(path, encoding='utf-8', errors='replace').read()


def result(text):
    m = re.search(r'^test result: (\w+)\.?(.*)$', text, re.M)
    return (m.group(1), m.group(2).strip()) if m else ('Missing', '')


def first_error(text):
    m = re.search(r'^\S+\.java:(?:\d+:\d+:)? error: (.*)$', text, re.M)
    if not m:
        return None
    return re.sub(r"'[^']*'|\b\d+\b", '_', m.group(1))[:80]


by_kind = collections.defaultdict(collections.Counter)
errors = collections.Counter()
for t in tests:
    j, o = jtr(javac_work, t), jtr(orzo_work, t)
    if j is None or result(j)[0] != 'Passed':
        continue
    kind = 'negative' if '@compile/fail' in open(t, errors='replace').read() else 'positive'
    res, why = result(o) if o else ('Missing', '')
    if res == 'Passed' or (kind == 'negative' and 'does not match reference' in why):
        outcome = 'pass'
    elif o and 'internal compiler error' in o:
        outcome = 'crash'
    elif 'passed unexpectedly' in why:
        outcome = 'accepted-invalid'
    elif 'ompilation failed' in why:
        outcome = 'compile-error'
        errors[first_error(o) or why[:80]] += 1
    elif o and 'VerifyError' in o:
        outcome = 'verify-error'
    elif 'timed out' in why.lower():
        outcome = 'timeout'
    else:
        outcome = 'failure'
    by_kind[kind][outcome] += 1

total = sum(sum(c.values()) for c in by_kind.values())
passed = sum(c['pass'] for c in by_kind.values())
print('tests passing with javac: %d, passing with Orzo: %d (%.1f%%)' % (total, passed, 100.0 * passed / max(total, 1)))
for kind in ('positive', 'negative'):
    print('  %-8s %s' % (kind, dict(by_kind[kind].most_common())))
print('most common compile errors:')
for msg, n in errors.most_common(15):
    print('  %4d %s' % (n, msg))
with open(result_file, 'w') as out:
    out.write('# written by compliance/jtreg.sh\n')
    out.write('openjdk_commit=%s\n' % commit)
    out.write('date=%s\n' % datetime.date.today().isoformat())
    out.write('total=%d\npassed=%d\n' % (total, passed))
    for kind in ('positive', 'negative'):
        out.write('%s_total=%d\n%s_passed=%d\n' % (kind, sum(by_kind[kind].values()), kind, by_kind[kind]['pass']))
