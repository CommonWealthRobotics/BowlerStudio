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
	curl -L $(bash $JVM_SCRIPTS/getJdkUrl.sh) -o $ZIP
	case $ZIP in
	*.zip) unzip -o -q $ZIP -d $JAVA_HOME ;;
	*) tar -xvzf $ZIP -C $JAVA_HOME ;;
	esac
	mv $JAVA_HOME/$JVM/* $JAVA_HOME/
fi
if test -d $JAVA_HOME/Contents/Home; then
	export JAVA_HOME=$JAVA_HOME/Contents/Home
fi
echo "Java home set to $JAVA_HOME"
 ./gradlew --stop
#rm -rf ~/.gradle/caches/
#rm -rf ~/.gradle/daemon/
./gradlew clean build --refresh-dependencies
./gradlew shadowJar

$JAVA_HOME/bin/java \
	-Dprism.forceGPU=true \
    -XX:MaxRAMPercentage=95 \
    --add-exports javafx.graphics/com.sun.javafx.css=ALL-UNNAMED \
	--add-exports javafx.controls/com.sun.javafx.scene.control.behavior=ALL-UNNAMED \
	--add-exports javafx.controls/com.sun.javafx.scene.control=ALL-UNNAMED \
	--add-exports javafx.base/com.sun.javafx.event=ALL-UNNAMED \
	--add-exports javafx.controls/com.sun.javafx.scene.control.skin.resources=ALL-UNNAMED \
	--add-exports javafx.graphics/com.sun.javafx.util=ALL-UNNAMED \
	--add-exports javafx.graphics/com.sun.javafx.scene.input=ALL-UNNAMED \
	--add-opens javafx.graphics/javafx.scene=ALL-UNNAMED \
	-jar build/libs/BowlerStudio.jar