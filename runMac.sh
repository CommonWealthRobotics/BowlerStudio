SCRIPT_DIR=$( cd -- "$( dirname -- "${BASH_SOURCE[0]}" )" &> /dev/null && pwd )

export ARCH=x86_64
JVM=zulu17.50.19-ca-fx-jdk17.0.11-macosx_x64
set -e
ZIP=$JVM.tar.gz
export JAVA_HOME=$HOME/bin/java17/
if test -d $JAVA_HOME/$JVM/; then
  echo "$JAVA_HOME exists."
else
	rm -rf $JAVA_HOME
	mkdir -p $JAVA_HOME
	curl -L https://cdn.azul.com/zulu/bin/$ZIP -o $ZIP
	tar -xvzf $ZIP -C $JAVA_HOME
	mv $JAVA_HOME/$JVM/* $JAVA_HOME/
fi
echo "Java home set to $JAVA_HOME"

./gradlew build shadowJar

$JAVA_HOME/bin/java -Dprism.forceGPU=true --add-exports \
	javafx.graphics/com.sun.javafx.css=ALL-UNNAMED \
	--add-exports \
	javafx.controls/com.sun.javafx.scene.control.behavior=ALL-UNNAMED \
	--add-exports \
	javafx.controls/com.sun.javafx.scene.control=ALL-UNNAMED \
	--add-exports \
	javafx.base/com.sun.javafx.event=ALL-UNNAMED \
	--add-exports \
	javafx.controls/com.sun.javafx.scene.control.skin.resources=ALL-UNNAMED \
	--add-exports \
	javafx.graphics/com.sun.javafx.util=ALL-UNNAMED \
	--add-exports \
	javafx.graphics/com.sun.javafx.scene.input=ALL-UNNAMED \
	--add-opens \
	javafx.graphics/javafx.scene=ALL-UNNAMED \
	-jar build/libs/BowlerStudio.jar