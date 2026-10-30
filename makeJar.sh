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
	wget $(bash $JVM_SCRIPTS/getJdkUrl.sh) 
	case $ZIP in
	*.zip) unzip -o -q $ZIP -d $JAVA_HOME ;;
	*) tar -xvzf $ZIP -C $JAVA_HOME ;;
	esac
	mv $JAVA_HOME/$JVM/* $JAVA_HOME/
fi
echo "Java home set to $JAVA_HOME"

./gradlew spotlessCheck shadowJar
