BowlerStudio
==========
![](/resources/img/screenshot-03.png)

BowlerStudio Robotics development IDE based on [JCSG](https://github.com/miho/JCSG),  [Java-Bowler](https://github.com/NeuronRobotics/java-bowler), [OpenCV](http://opencv.org/), [JavaCV](https://github.com/bytedeco/javacv) which provides [FFmpeg](http://ffmpeg.org/), [libdc1394](http://damien.douxchamps.net/ieee1394/libdc1394/), [PGR FlyCapture](http://www.ptgrey.com/products/pgrflycapture/), [OpenKinect](http://openkinect.org/), [videoInput](http://muonics.net/school/spring05/videoInput/), [ARToolKitPlus](http://studierstube.icg.tugraz.at/handheld_ar/artoolkitplus.php), and [flandmark](http://cmp.felk.cvut.cz/~uricamic/flandmark/)), [CHDK-PTP-Java](https://github.com/acamilo/CHDK-PTP-Java) [Jinput](https://github.com/jinput/jinput), [motej](http://motej.sourceforge.net/), [Usb4Java](https://github.com/usb4java/usb4java), [NrJavaSerial](https://github.com/NeuronRobotics/nrjavaserial), [BlueCove](https://github.com/hcarver/bluecove) and the new JavaFX 8 3d engine. 

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
   BowlerDevices such as the Neuron Robotics DyIO are devices that implement the Bowler Communication System. Bowler devices are servers of fratures to applications. In the case of the DYIO, it is a server of microcontroller features. These devices implement a micro Domain Specific Langauge as a protocol. This lanagauge suynchronizes the device with the application by building at runtime the communication system. 
   This is achived using a namespace/RPC system. The device is a collection of namespaces. Each namespace has a set of RPC's for communication, some synchronus (meaning they are application initiated) and some are asynchronus (meaning they are device initiated). Each RPC has full method introspection, meaning all parameters and datatypes, how to pack and intrpret all packets, is queriable over the communication suystem. A Library need only implement the core packet parser, and every device will assemble its own communication layer live.  

# Cameras
   Camers can be connected to Bowler Studio using one of 3 supported drivers.
   OpenCV's native Java bindings are provide by installing OpenCV using your OS specific installer (sorry mac users, hope you like compiling things yourself). 
   JavaCV is a meta-library adds suport for a wide range of camera device integrations and image processing options. This is a big project and integrated now with a full scripting system. 
   CHDK-PTP-Java is a Java library to add support for SLR Cannon cameras. CHDK is a camera OS that makes the cameras features availible over USB, and the Java library makes those images and controls availible to Java and or scripting engine. 

# Image processing
   Image processing is provided be a variety of libraries included in this applicaion. OpenCV and ARToolkit are some of the most widly used image recognition libraries availible, and now you can use them from our scripting environment. 
   
# Kinematics Engine
   The Bowler Kinematics engine is based on [D-H parameters](https://www.youtube.com/embed/rA9tm0gTln8), the standard mathmatical definition of kinematics chains. This standard simplifies the calculation process and allows us to run forward kinematics equations for arbitrary defined chains in real time. For inverse kinematics, a collection of kinematic engines are availible for optimising for speed and accuracy. 

# Real-Time validated
   For applications where real time is requred, there is no need to leave the Bowler echosystem. the Bowler Java stack has been validated as real-time capible when run on JamaicaVM, the real-time java implementation. The Bowler Kinematices engine is run in the real time loop for neuro-surgery applications. Bowler and java are fast and reliable enough for brain surgery. 
   
# 3D CAD
   Users can write scrits using Java, Groovy or Python that generate CSG style CAD. This progromatic CAD engine JCSG was inspired by OpenSCAD, but implemented in pure Java with JavaFX providing visualization. JCSG implements all basic shape generation and manipulation, and Java's library packaging and distribution is used for libraries of parts. Gist hosting of parts also simplifiues sharing and loading of dependant libraries. 
   
# Virtualization
### Virtual links
   Virtual PID devices allow useres to make applications that can be simulated with virtual links before ever connecting a real device. 
   Users can also use the PIDLab to lear about designing and implementing PID controllers with a built in motor physics simulation. 
### Virtual Camera
   Coming soon users will be able to load the camera for the 3d environment as a BowlerStudio camera that can be piped into the image processors as if it was a real camera. Users will be able to manipulate it just like it was another 3d object. 
   
### Virtual Sensors
   Coming soon there will be virtual sensor devices that can simulate real world sensors but extract thier information from the 3d environment. 

![](http://thingiverse-production.s3.amazonaws.com/renders/0c/a0/c0/dc/53/IMG_20140329_201814_preview_featured.jpg)

## How to Build BowlerStudio

### Requirements

- Java >= 1.8
- Internet connection (dependencies are downloaded automatically)
- IDE: [Gradle](http://www.gradle.org/) Plugin (not necessary for command line usage)
- OpenCV 2.4.9 installed and configured in the library paths (OS installers preffered) 

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
