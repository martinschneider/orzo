#/bin/bash

# This will re-compile all white-listed classes with Orzo.
# This should run after unit and before integration tests, so that the integration tests can verify the self-compilation functionality.
# After Orzo is able to compile a large enough percentage of itself, we can move from a whitelist to a blacklist approach. 

# TODO: this is platform-dependent. Ideally, we should use Java or a Groovy script instead of Bash  
echo "Recompiling $(cat whitelist.txt | wc -l | xargs)/$(find ./src/main/java -name "*.java" | wc -l | xargs) files with Orzo:"
cat whitelist.txt
java -jar target/orzo-0.0.1-SNAPSHOT.jar $(cat whitelist.txt | tr '\n' ' ') -d target/classes
jar cfe target/orzo-0.0.1-SNAPSHOT.jar io.github.martinschneider.orzo.Orzo -C target/classes .