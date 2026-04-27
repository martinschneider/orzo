# Self-Compilation Workflow

How to extend orzo's self-compilation coverage by adding new files to `allowlist.txt`.

## Phase 1: Normal test suite

Always run this first. Never skip to Phase 2.

```
mvn clean verify
```

All 507+ tests must pass before attempting self-compilation.

## Phase 2: Self-compilation check

```
mvn clean verify -Pselfcompile
```

This invokes `selfcompile.sh`, which:
1. Copies source to `.tmp/`, strips comments and blank lines
2. Compiles all allowlisted files 3× with orzo (bootstrapping)
3. Measures size of orzo-compiled `.class` files vs javac-compiled
4. Writes LOC% to `progress` file (used by README badge)

## Adding a new file

1. Pick one candidate. Check it doesn't use unsupported constructs first:
   - generics in method bodies (partially supported)
   - multi-catch, try-with-resources (not supported)
   - lambda / streams (not supported)
   - string concatenation with `+` (not supported)
2. Add **one** line to `allowlist.txt` — never more than one at a time.
3. Test alone first:

   ```
   mvn clean package -q
   java -jar target/orzo.jar <path-to-file> -d /tmp/orzo-test/
   ```
4. Compare bytecode against javac output:

   ```
   javap -c /tmp/orzo-test/io/github/martinschneider/orzo/...class
   javap -c target/classes/io/github/martinschneider/orzo/...class
   ```
5. Run Phase 1: `mvn clean verify`
6. If Phase 1 passes, run Phase 2: `mvn clean verify -Pselfcompile`
7. Fix any errors before adding the next file.

## Diagnosing self-compilation failures

### Compile errors

Check `target/failsafe-reports/*.txt`:

```
grep -h "ERROR\|Exception\|error" target/failsafe-reports/*.txt | head -20
```

### Infinite loops during self-compilation

Use `find_loop.sh`:

```bash
chmod +x find_loop.sh
./find_loop.sh              # full run with build
./find_loop.sh --skip-build # skip mvn, reuse existing jar
```

The script binary-searches to find which file(s) trigger the hang, then pinpoints
interaction pairs. Check `/tmp/orzo_loop_out/latest.log` for thread dumps.

### Bytecode divergence

When orzo-compiled bytecode differs from javac:
1. Identify first divergent instruction in `javap -c` diff
2. Check the bug pattern list in `skills/codegen-bugs.md`
3. Add a unit test in `ExpressionGeneratorTest`, `BasicGeneratorTest`, or create a new
`*Test.java` + matching `.output` file under `src/test/java/io/github/martinschneider/orzo/tests/`
4. Fix, verify test passes, then re-run Phase 1 → Phase 2

## Files likely to compile cleanly (low risk)

- Simple data/production classes with only fields, constructors, getters
- Classes with only primitive types and `String`
- Classes with no generics in method bodies

## Files that need parser/codegen work first (high risk)

- Classes using `instanceof`
- Classes using `&&` / `||` in complex conditions
- Classes using string `+` concatenation
- Classes with try/catch/finally
- Classes with complex generic bounds
- Classes with lambdas or streams

## Measuring progress

After a successful Phase 2 run, `selfcompile.sh` prints:

```
Recompiling N/M files with Orzo (X% of files, Y% of LOC):
Pass #1: Orzo compiled by itself (... bytes) is Z% the size of Orzo compiled using javac
```

The LOC% (`Y`) is the meaningful metric — written to `progress` and shown in the README badge.
