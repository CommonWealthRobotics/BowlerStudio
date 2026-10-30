SCRIPT_DIR=$( cd -- "$( dirname -- "${BASH_SOURCE[0]}" )" &> /dev/null && pwd )

set -e
JVM_SCRIPTS=$SCRIPT_DIR/libraries/bowler-script-kernel/JCSG/jvm
JVM=$(bash $JVM_SCRIPTS/getJdkArchiveName.sh)
ZIP=$(bash $JVM_SCRIPTS/getJdkArchiveFile.sh)
export JAVA_HOME=$HOME/bin/java21/
if test -d $JAVA_HOME/$JVM/; then
  echo "$JAVA_HOME exists."
else
	rm -rf $JAVA_HOME
	mkdir -p $JAVA_HOME
	wget https://cdn.azul.com/zulu/bin/$ZIP 
	tar -xvzf $ZIP -C $JAVA_HOME
	mv $JAVA_HOME/$JVM/* $JAVA_HOME/
fi
echo "Java home set to $JAVA_HOME"

./gradlew spotlessCheck shadowJar
