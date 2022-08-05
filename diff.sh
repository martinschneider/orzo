#/bin/bash
# Helper script to show the bytecode diff between a source file compiled with Orzo and javac
# TODO: support classes in non-default packages
# This sends all shell output to /dev/null. Change as needed.

TMP_DIR="/tmp"
DIFF_CMD="meld"

# sorts the constant pool entries in the output of javap -v by type and name
# this simplifies the diff between the javac and orzo output
sort_constant_pool () {
  grep '^\s*#' <$1 | awk '{print $3 $4"@"$0}' | sort | cut -d"@" -f2- |
  while read -r line <&3; do
    if [[ $line =~ ^\s*#.* ]]; then
	  read -r sorted
	  echo "$sorted"
	else 
	  echo "$line"
	fi
  done 3<$1 > $2
} 

java -jar ./target/orzo-0.0.1-SNAPSHOT.jar $1 -d $TMP_DIR >> /dev/null
JAVAC_CLASS=$TMP_DIR/$(basename $1 .java).class
ORZO_CLASS=$TMP_DIR/$(basename $1 .java)_orzo.class
mv $JAVAC_CLASS $ORZO_CLASS
javac -source 7 $1 -d $TMP_DIR >> /dev/null 2>/dev/null
javap -v $JAVAC_CLASS >> $TMP_DIR/javacbytes
javap -v $ORZO_CLASS >> $TMP_DIR/orzobytes
sort_constant_pool $TMP_DIR/javacbytes $TMP_DIR/javacbytes_sorted
sort_constant_pool $TMP_DIR/orzobytes $TMP_DIR/orzobytes_sorted
# when using a command line diff tool, remove >> /dev/null
$DIFF_CMD $TMP_DIR/javacbytes_sorted $TMP_DIR/orzobytes_sorted >> /dev/null
rm $JAVAC_CLASS $ORZO_CLASS $TMP_DIR/javacbytes* $TMP_DIR/orzobytes*
