SCRIPT_DIR=$( cd -- "$( dirname -- "${BASH_SOURCE[0]}" )" &> /dev/null && pwd )
name=$(jq -r '."Linux-x64".name' jvm.json)
type=$(jq -r '."Linux-x64".type' jvm.json)
url=$(jq -r '."Linux-x64".url' jvm.json)

# Verify the variables were set correctly
echo "Name: $name"
echo "Type: $type"
echo "URL: $url"

export ARCH=x86_64
JVM=$name
set -e
ZIP=$JVM.$type
export JAVA_HOME=$HOME/bin/java17/
if test -d $JAVA_HOME/$JVM/; then
  echo "$JAVA_HOME exists."
else
	rm -rf $JAVA_HOME
	mkdir -p $JAVA_HOME
	wget $url$ZIP 
	tar -xvzf $ZIP -C $JAVA_HOME
	mv $JAVA_HOME/$JVM/* $JAVA_HOME/
fi
echo "Java home set to $JAVA_HOME"

./gradlew clean shadowJar