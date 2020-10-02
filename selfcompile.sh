#/bin/bash

# This will re-compile all white-listed classes with Orzo.
# This should run after unit and before integration tests, so that the integration tests can verify the self-compilation functionality.
# After Orzo is able to compile a large enough percentage of itself, we can move from a whitelist to a blacklist approach. 

# TODO: this is platform-dependent. Ideally, we should use Java or a Groovy script instead of Bash  
done=$(cat whitelist.txt | wc -l | xargs)
total=$(find ./src/main/java -name "*.java" | wc -l | xargs)
percentage=$(printf %0.2f $(echo "100* $done/$total" | bc -l))
javacSize=$(du -sb target/classes | awk '{print $1}')
echo "Recompiling $done/$total files with Orzo ($percentage%):"
cat whitelist.txt
java -jar target/orzo-0.0.1-SNAPSHOT.jar $(cat whitelist.txt | tr '\n' ' ') -d target/classes
jar cfe target/orzo-0.0.1-SNAPSHOT.jar io.github.martinschneider.orzo.Orzo -C target/classes .
orzoSize=$(du -sb target/classes | awk '{print $1}')
percentage=$(printf %0.2f $(echo "100* $orzoSize/$javacSize" | bc -l))
echo "Orzo compiled by itself is $percentage% the size of Orzo compiled using javac."