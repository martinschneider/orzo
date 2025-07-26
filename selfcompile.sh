#!/bin/bash

# This will re-compile all white-listed classes with Orzo (multiple times).
# This should run after the unit and before the integration tests, so that the integration tests can verify the self-compilation functionality.
# After Orzo is able to compile a large enough percentage of itself, we can move from a whitelist to a blacklist approach. 

# Function to remove comments and empty lines from Java files
clean_java_file() {
    local input_file="$1"
    local output_file="$2"
    
    # Remove single-line comments (//), multi-line comments (/* */), and empty lines
    sed -e '/\/\*/,/\*\//d' \
        -e 's#//.*##' \
        -e '/^[[:space:]]*$/d' \
        "$input_file" > "$output_file"
}

rm progress

# Create clean copy of source code in .tmp directory
echo "Creating copy of source code in .tmp..."
rm -rf .tmp
mkdir -p .tmp
cp -r src/main/java .tmp/

echo "Removing comments and empty lines from source code..."
find .tmp/java -name "*.java" | while read -r file; do
    temp_file="${file}.tmp"
    clean_java_file "$file" "$temp_file"
    mv "$temp_file" "$file"
done

done=$(grep -v -E '^#|^$' whitelist.txt | wc -l | xargs)
total=$(find .tmp/java -name "*.java" | wc -l | xargs)

while read -r file ; do 
    clean_file=$(echo "$file" | sed 's|src/main/java|.tmp/java|')
    if [ -f "$clean_file" ]; then
        ((doneLOC+=$(cat "$clean_file" | wc -l)))
    fi
done < <(grep -v -E '^#|^$' whitelist.txt)

while read -r file ; do ((totalLOC+=$(cat "$file" | wc -l))); done < <(find .tmp/java -name "*.java")

percentage=$(printf %0.2f $(echo "100* $done/$total" | bc -l))
percentageLOC=$(printf %0.2f $(echo "100* $doneLOC/$totalLOC" | bc -l))
javacSize=$(du -sb target/classes | awk '{print $1}')
echo "Recompiling $done/$total files with Orzo ($percentage% of files, $percentageLOC% of LOC):"
echo $percentageLOC > progress

grep -v -E '^#|^$' whitelist.txt | sed 's|src/main/java|.tmp/java|'
for i in {1..3}
do
  java -jar target/orzo.jar $(grep -v -E '^#|^$' whitelist.txt | sed 's|src/main/java|.tmp/java|' | tr '\n' ' ') -d target/classes
  jar cfe target/orzo.jar io.github.martinschneider.orzo.Orzo -C target/classes .
  orzoSize=$(du -sb target/classes | awk '{print $1}')
  percentage=$(printf %0.2f $(echo "100* $orzoSize/$javacSize" | bc -l))
  echo "Pass #$i: Orzo compiled by itself ($orzoSize bytes) is $percentage% the size of Orzo compiled using javac ($javacSize bytes)."
done

echo "Cleaning up .tmp directory..."
rm -rf .tmp