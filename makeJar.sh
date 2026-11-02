SCRIPT_DIR=$( cd -- "$( dirname -- "${BASH_SOURCE[0]}" )" &> /dev/null && pwd )

set -e
JVM_SCRIPTS=$SCRIPT_DIR/libraries/bowler-script-kernel/JCSG/jvm
bash $JVM_SCRIPTS/installJdk.sh
export JAVA_HOME=$HOME/bin/CaDoodle-ApplicationInstall/$(bash $JVM_SCRIPTS/getJdkArchiveName.sh)/
echo "Java home set to $JAVA_HOME"

./gradlew spotlessCheck shadowJar
