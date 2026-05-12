# Commiting changes

- Make sure all tests and the self-compilation passes
- Clean up any unused code. If you tried multiple fixes, make sure not to leave anything unneeded in the code from unsuccessful fixes.
- Check for newly introduced dead code: after `mvn clean verify`, open `target/site/jacoco/index.html` and verify that any new methods/classes you added have test coverage. Methods with 0% coverage that are not part of a public API are candidates for removal.
- Confirm that all TODOs are up2date (including docs/TODO.md)
- Verify that README.md is up2date
- Perform a final mvn clean verify -Pselfcompile and ensure there are no errors
- Create a commit with a succinct commit message and clear summary of the changes
- Never push commits upstream yourself

