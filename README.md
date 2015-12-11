BowlerStudio
==========

#[Download Latest](https://github.com/NeuronRobotics/BowlerStudio/releases)

#What is BowlerStudio?

BowlerStudio assists you in every step of a robotics project from concept to completion. Tools enable users to:
* Interface with motors, sensors, and other electronics hardware.
* Create 3d models for fabrication, and for simulating the motions of your project.
* Give your robot sight with image processing on camera feeds and Kinect data.
* Operate 3d printers and other CNC machines.
* Create custom graphical user interfaces to control yours robots.
* Create and control animations.

==========

##The Nitty-Gritty Version

BowlerStudio Robotics development IDE is based on
* [JCSG](https://github.com/miho/JCSG)
* [Java-Bowler](https://github.com/NeuronRobotics/java-bowler)
* [OpenCV](http://opencv.org/)
* [JavaCV](https://github.com/bytedeco/javacv) which provides
* [FFmpeg](http://ffmpeg.org/)
* [libdc1394](http://damien.douxchamps.net/ieee1394/libdc1394/)
* [PGR FlyCapture](http://www.ptgrey.com/products/pgrflycapture/)
* [OpenKinect](http://openkinect.org/)
* [videoInput](http://muonics.net/school/spring05/videoInput/)
* [ARToolKitPlus](http://studierstube.icg.tugraz.at/handheld_ar/artoolkitplus.php)
* [flandmark](http://cmp.felk.cvut.cz/~uricamic/flandmark/))
* [CHDK-PTP-Java](https://github.com/acamilo/CHDK-PTP-Java)
* [Jinput](https://github.com/jinput/jinput)
* [motej](http://motej.sourceforge.net/)
* [Usb4Java](https://github.com/usb4java/usb4java)
* [NrJavaSerial](https://github.com/NeuronRobotics/nrjavaserial)
* [BlueCove](https://github.com/hcarver/bluecove) and the new JavaFX 8 3d engine. 

BowlerStudio is a device manager, scripting engine, CAD package, and simulation tool all in one application. A user can develop the kinematic of an robot arm using the D-H parameters-based automatic kinematics engine. With this kinematics model, the user can then generate the CAD for new unique parts to match the kinematic model. The user can then export the model to an STL, and connect a Bowler 3d printer to BowlerStudio. The printer can print out the part (using the newly generated STL) while the user connects a DyIO and begins testing the servos with the kinematics model. When the print is done, the user can assemble the arm with the tested servos and run the model again to control the arm with Cartesian instructions. Once this is complete, the user can then attach a wiimote to train the robot arm through a set of tasks, recording them with the animation framework built into BowlerStudio. To be sure the arm is moving to the right place, the user can attach a webcam to the end and use OpenCV to verify the arm's position, or use the arm (in conjunction with the webcam with OpenCV enabled) to track and grab objects (IE "eye-in-hand" tracking). 

Every step of this task can be performed from within BowlerStudio!

Let's go through the main features:

# Scripting With Gist
### About scripts and Gist
   Scripts are bits of code that BowlerStudio can load and run. BowlerStudio allows you to open a local file and run it, but BowlerStudio is most powerful when the code lives on Github Gist (a code snippet hosting service from Github). Simply give BowlerStudio the URL for a Gist you want to load and execute. Gists can be selected and edited using the built in browser, or inline in another script using the Gist ID.   
### Java and Groovy
   BowlerStudio can load and run scripts written in Java, Groovy, and Python. Which parser is used is determined by the file extension. Files that end in .java or .groovy will be run through the Groovy compiler. These Groovy scripts are compiled fully and run directly in the JVM. This means they will execute at full speed, just like a regular application.  
   
### Python
   Python, on the other hand, by virtue of its structure, will generally execute much slower then Java. With the reduction in speed you get lots of flexibility and a clean and easy to understand syntax. The python code can also create and return objects to BowlerStudio (such as CAD CSG objects, or UI Tabs).  

### Return Objects
   A script can return a few object types that will be handled by BowlerStudio:
   Objects of type "CSG" and "MeshView" will be added to the 3d display. If a transform is added to either of these and updated by a script the user can move an object in the 3d view. 
   Objects of type "Tab" will be added to the Tabmanager and displayed in BowlerStudio. This is an easy way to make control panels or state displays and monitors. 
   Objects of type "BowlerAbstractDevice" (or any subclass) will be added to the connections manager and made available to all other scripts. These can be external devices or virtual communication bus devices. A bowler Server/Client pair is the preferred mechanism for communication between scripts. 
### Device Access
   All scripts are passed all connected devices by name when the script is run. The name associated with the device in the connections tab is the name to use in the script to access that device. A script can also create and return a device (EG to connect to a specific device in order to give that device a specific name). The device returned will be added to the list of available devices and be available to other scripts. A user can define their own devices to facilitate communication between scripts. 
   
# Bowler Devices
   BowlerDevices (such as the Neuron Robotics DyIO) are devices that implement the Bowler Communication System. BowlerDevices are servers of features to applications. The DyIO, for example, is a server of microcontroller features. These devices implement a micro domain-specific language as a protocol. This language synchronizes the device with the application by building the communication system at runtime using a namespace/RPC system. 
   As such, the device is treated as a collection of namespaces. Each namespace has a set of RPCs for communication, some synchronous (IE they are application initiated) and some asynchronous (IE device initiated). In addition, each RPC has full method introspection. This means that all parameters and datatypes (including how to pack and interpret all packets) are able to be queried over the communication system. A Library need only implement the core packet parser and every device will assemble its own communication layer live.  

# Cameras
   Cameras can be connected to Bowler Studio using one of 3 supported drivers:
   OpenCV's native Java bindings are provided by installing OpenCV using your OS specific installer (unfortunately not available for Mac at this time). 
   JavaCV is a meta-library that adds support for a wide range of camera device integrations and image processing options. This is a big project and integrated now with a full scripting system. 
   CHDK-PTP-Java is a Java library that adds support for SLR Cannon cameras. CHDK is a camera OS that makes the cameras features available over USB, and the Java library makes those images and controls available to Java and or scripting engine. 

# Image processing
   Image processing is provided be a variety of libraries included in this application. OpenCV and ARToolkit are some of the most widely used image recognition libraries available, and now you can use them directly from our scripting environment! 
   
# Kinematics Engine
   The Bowler Kinematics engine is based on [D-H parameters](https://www.youtube.com/embed/rA9tm0gTln8)- the standard mathematical definition of kinematics chains. This standard simplifies the calculation process and allows us to run forward kinematics equations for arbitrary defined chains in real time. For inverse kinematics, a collection of kinematic engines are available for optimizing for speed and accuracy. 

# Real-Time validated
   For applications where real time is required, there is no need to leave the Bowler OS. The Bowler Java stack has been validated as real-time capable when run on JamaicaVM (the real-time Java implementation). The Bowler Kinematics engine is run in a real-time loop for neurosurgery applications (Bowler and Java are fast and reliable enough for brain surgery!)
   
# 3D CAD
   Users can write scripts using Java, Groovy or Python to generate CSG style CAD. This programmatic CAD engine JCSG was inspired by OpenSCAD, but implemented in pure Java with JavaFX visualizations. JCSG implements all basic shape generation and manipulation, using Java's library packaging and distribution for libraries of parts. Gist hosting of parts can also simplify sharing and loading of dependent libraries. 
   
# Virtualization
### Virtual links
   Virtual PID devices allow users to make applications that can be simulated with virtual links before ever connecting a real device. 
   Users can also use the PIDLab to learn about designing and implementing PID controllers with a built in motor physics simulation. 
### Virtual Camera
   Coming soon! Soon users will be able to interact with the 3D environment camera from their code just like a real camera. Users will be able to manipulate it just like it was another 3d object. 
   
### Virtual Sensors
   Coming soon! Soon we will be able to provide virtual sensor devices, simulating real world sensors within the 3d environment. 

# Make A Contribution
BowlerStudio is an open source project and is always looking for help with both the application code and the tutorials content. 

### Java Contributions

If you are a Java developmer, skip ahead to [The Build Instructions](#command-line). The application is a light plugin framework for UI, 3D and Device interaction. You can look at this repository for issues. 

### Adding Tutorials

All of the content for BowlerStudio Tutorials is housed on our [Neuronrobotics.github.io](https://github.com/NeuronRobotics/NeuronRobotics.github.io) web page. Fork that repository and make contributions based on the README.md file in the root of the repository. To merge the changes into the main website, send a pull request with your changes to official repository. 

Examples of tutorials that need to be added are [A simple Java Programming Introduction](https://github.com/NeuronRobotics/NeuronRobotics.github.io/issues/59). This tutorial set would go through the basic syntax of java and what all of the symbols mean and how to use them. 

Another example of a tutorial that could be added is one for [JavaCad Cheatsheet](https://github.com/NeuronRobotics/NeuronRobotics.github.io/issues/58) where you would add a 'cheat sheet' of commands to use in the JavaCad system. 

If a tutorial is missing and not described as needed by an issue, feel free to add additional issues. 

## How to Build BowlerStudio

### Requirements

- Java >= 1.8
- Internet connection (dependencies are downloaded automatically)
- IDE: [Gradle](http://www.gradle.org/) Plugin (not necessary for command line usage)
- OpenCV 2.4.9 installed and configured in the library paths (OS installers preferred) 

### IDE

Open the `BowlerStudio` [Gradle](http://www.gradle.org/) project in your favorite IDE (tested with NetBeans 7.4) and build 
by calling the `assemble` task.

### Command Line
Navigate to the [Gradle](http://www.gradle.org/) project (e.g., `path/to/BowlerStudio`) and enter the following command:

#### Bash (Linux/OS X/Cygwin/other Unix-like shell)

#####Ubuntu Dependancies

    sudo add-apt-repository ppa:webupd8team/java
   
    sudo apt-get update
   
    sudo apt-get install git gradle oracle-java8-installer oracle-java8-set-default libopencv2.4-java libopencv2.4-jni
   
#####All Unix  
   
    git clone https://github.com/NeuronRobotics/BowlerStudio.git
   
    cd BowlerStudio
    
    git submodule init
    
    git submodule update
    
    ./gradlew jar
    
    java -jar build/libs/BowlerStudio.jar
    
Now you can use the Eclipse Marketplace to install the Gradle Plugin
    
#### Windows (CMD)

    gradlew jar
