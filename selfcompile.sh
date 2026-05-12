#!/bin/bash

# This will re-compile all classes with Orzo (multiple times), except those listed in skiplist.txt.
# This should run after the unit and before the integration tests, so that the integration tests can verify the self-compilation functionality.

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

total=$(find .tmp/java -name "*.java" | wc -l | xargs)
denied=$(grep -v -E '^#|^$' skiplist.txt | wc -l | xargs)
done=$((total - denied))

while read -r file ; do ((totalLOC+=$(cat "$file" | wc -l))); done < <(find .tmp/java -name "*.java")

while read -r file ; do
    clean_file=$(echo "$file" | sed 's|src/main/java|.tmp/java|')
    if [ -f "$clean_file" ]; then
        ((deniedLOC+=$(cat "$clean_file" | wc -l)))
    fi
done < <(grep -v -E '^#|^$' skiplist.txt)

doneLOC=$((totalLOC - deniedLOC))

percentage=$(printf %0.2f $(echo "100* $done/$total" | bc -l))
percentageLOC=$(printf %0.2f $(echo "100* $doneLOC/$totalLOC" | bc -l))
javacSize=$(du -sb target/classes | awk '{print $1}')
echo "Recompiling $done/$total files with Orzo ($percentage% of files, $percentageLOC% of LOC):"
echo $percentageLOC > progress

# Build list of files to compile (all except skiplisted)
compile_files=()
while read -r file; do
    rel_file=$(echo "$file" | sed 's|.tmp/java/|src/main/java/|')
    if ! grep -qxF "$rel_file" skiplist.txt; then
        compile_files+=("$file")
    fi
done < <(find .tmp/java -name "*.java")

printf '%s\n' "${compile_files[@]}"

for i in {1..3}
do
  java -jar target/orzo.jar "${compile_files[@]}" -d target/classes
  jar cfe target/orzo.jar io.github.martinschneider.orzo.Orzo -C target/classes .
  orzoSize=$(du -sb target/classes | awk '{print $1}')
  percentage=$(printf %0.2f $(echo "100* $orzoSize/$javacSize" | bc -l))
  echo "Pass #$i: Orzo compiled by itself ($orzoSize bytes) is $percentage% the size of Orzo compiled using javac ($javacSize bytes)."
done

echo "Cleaning up .tmp directory..."
rm -rf .tmp
