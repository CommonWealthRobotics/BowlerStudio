BowlerStudio
==========

[![Build Status](https://travis-ci.org/miho/JFXScad.svg?branch=master)](https://travis-ci.org/miho/JFXScad)

BowlerStudio Robotics development IDE based on [JCSG](https://github.com/miho/JCSG) and [Java-Bowler](https://github.com/NeuronRobotics/java-bowler)

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
    
#### Windows (CMD)

    gradlew assemble
