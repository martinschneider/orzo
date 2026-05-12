# Testing Rules

- For any bug found in the lexer, parser or code generator, add a unit test to reproduce it and ensure that the test passes after a fix.
- Run integration tests in two phases: "mvn clean verify" and "mvn clean verify -Pselfcompile". Only run the self compilation check once everything else passes.
- The clean phase is important, do not skip it to avoid corrupt class files from previous runs affecting tests.
- When a new file fails to self-compile, add it to skiplist.txt first. Fix the issue and then remove it from the skiplist. Remove files from the skiplist one-by-one, never multiple in one edit.

