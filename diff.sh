#/bin/bash
# Helper script to show the bytecode diff between a source file compiled with Orzo and javac
# TODO: support classes in non-default packages
TMP_DIR="/tmp"
DIFF_CMD="meld"
java -jar ./target/orzo-0.0.1-SNAPSHOT.jar $1 -d $TMP_DIR -v
JAVAC_CLASS=$TMP_DIR/$(basename $1 .java).class
ORZO_CLASS=$TMP_DIR/$(basename $1 .java)_orzo.class
mv $JAVAC_CLASS $ORZO_CLASS
javac $1 -d $TMP_DIR
diff -y <(javap -v $JAVAC_CLASS) <(javap -v $ORZO_CLASS)
javap -v $JAVAC_CLASS >> $TMP_DIR/javacbytes
javap -v $ORZO_CLASS >> $TMP_DIR/orzobytes
$DIFF_CMD $TMP_DIR/javacbytes $TMP_DIR/orzobytes >> /dev/null
rm $JAVAC_CLASS $ORZO_CLASS $TMP_DIR/javacbytes $TMP_DIR/orzobytes
