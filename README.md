BowlerStudio
==========

BowlerStudio Robotics development IDE based on [JCSG](https://github.com/miho/JCSG),  [Java-Bowler](https://github.com/NeuronRobotics/java-bowler), [OpenCV](http://opencv.org/), [JavaCV](https://github.com/bytedeco/javacv), [CHDK-PTP-Java](https://github.com/acamilo/CHDK-PTP-Java) [Jinput](https://github.com/jinput/jinput), [motej](http://motej.sourceforge.net/), [Usb4Java](https://github.com/usb4java/usb4java), [NrJavaSerial](https://github.com/NeuronRobotics/nrjavaserial), [BlueCove](https://github.com/hcarver/bluecove) and the new JavaFX 8 3d engine. 

BowlerStudio is a device manager, scripting engine, CAD package and simulation tool all in one application. A user can develop the kinematic of an robot arm using the D-H parameters based automatic kinematics engine. With that kinematics model the user can generate the CAD for new unique parts that would match that kinematic model. Then the user can export the model to an STL and connect a Bowler 3d printer to BowlerStudio. The printer can print out the part while the user connects a DyIO and begins testing the servos with the kinematics model. When the print is done, the user can assemble the arm with tose servos and run the model again to control the arm with cartesian instructions. The user can then attach a wiimote to train the robot arm through a set of tasks, recording them with the anamation framework built into BowlerStudio. TO be sure the arm is moving to the right place, the user could attach a webcam to the end and use OpenCV to verify the arm is in the right place, or use it to track and grab objects. 

Every step of this can be performed from within BowlerStudio!

Lets go through the main features:

# Scripting With Gist
### About scripts and Gist
   Scripts are bits of code that BowlerStudio and load and run. YOu can open a local file and run it, but BowlerStudio is most powerful when the code libes in Github Gist. Gist is a code snippet hosting service from Github. BowlerStudio allows you to simply give it the URl for a Gist, and it can load and execute that code. Gists can be selected and edited using the built in browser, or inline in another script using the Gist ID.   
### Java and Groovy
   BowlerStudio can load and run scripts written in Java, Groovy, and Python. Which parser gets used is determined by the file extention. Files that end in .java or .groovy will be run through the Groovy compiler. These Groovy scripts are compiled fully and run directly in the JVM, meaning they run at full speed as if it were a regular application.  
   
### Python
   Python on the other hand is very slow, in general 300-400 times slower then Java. With the reduction in speed you get lots of flexibility and a clean and easy to understand syntax. The python code can also create and return objects to BowlerStudio such as CAD csg objects, or UI Tabs.  

### Return Objects
   A scrit can return a few object tuypes that will be handled by BowlerStudio.
   Objects of type CSG and MeshView will be added to the 3d display. If a transform is added to either of these and updated by a script the user can move an object in the 3d view. 
   Objects of type Tab will be added to the Tabmanager and displayed in BowlerStudio. This is an easy way to make control panels or state displays and moniters. 
   Objects of type BowlerAbstractDevice (or any subclass) will be added to teh connections manager and made availible to all other scripts. These can be external devices or virtual communication bus devices. A bowler Server/Client pair is the prefered mechanism for communication between scripts. 
### Device Access
   All scripts are passed all connected devices when the script is run. The name associated with the device in the connections tab is the name to use in the script to access that device. A script can also create and return a device from a script (to connect to a specific device to give a device a specific name) . The device returned will be added to the list oc availible devices and be availible to other scripts. A user can define thier own devices to facilitate communication between scripts. 
   
# Bowler Devices

# Cameras

# Image processing


![](/resources/img/screenshot-03.png)

![](http://thingiverse-production.s3.amazonaws.com/renders/0c/a0/c0/dc/53/IMG_20140329_201814_preview_featured.jpg)

## How to Build BowlerStudio

### Requirements

- Java >= 1.8
- Internet connection (dependencies are downloaded automatically)
- IDE: [Gradle](http://www.gradle.org/) Plugin (not necessary for command line usage)

### IDE

Open the `BowlerStudio` [Gradle](http://www.gradle.org/) project in your favourite IDE (tested with NetBeans 7.4) and build it
by calling the `assemble` task.

### Command Line
Navigate to the [Gradle](http://www.gradle.org/) project (e.g., `path/to/BowlerStudio`) and enter the following command

#### Bash (Linux/OS X/Cygwin/other Unix-like shell)
    
    git submodule init
    
    git submodule update
    
    bash gradlew assemble
    
    java -jar build/libs/BowlerStudio.jar
    
then you can use the Eclipse Gradle plugin to import the project.

http://marketplace.eclipse.org/content/gradle-integration-eclipse-44

    
#### Windows (CMD)

    gradlew assemble
