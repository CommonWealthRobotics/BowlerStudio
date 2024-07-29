SCRIPT_DIR=$( cd -- "$( dirname -- "${BASH_SOURCE[0]}" )" &> /dev/null && pwd )

export ARCH=x86_64
JVM=zulu8.78.0.19-ca-fx-jdk8.0.412-linux_x64
set -e
ZIP=$JVM.tar.gz
export JAVA_HOME=$HOME/bin/java8/
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

./gradlew clean shadowJar