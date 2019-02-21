# AVR4L
![AVR4L Screenshot](https://raw.githubusercontent.com/abdalmoniem/AVR4L/master/assets/screenshot_8.png)

## About:
AVR4L aims to deliver an easy to use and user friendly Integrated Development Environment for developing Codes for microcontrollers based on Atmel's AVR families. AVR4L is a Creative coding / Integrated Development Environment for Linux operating systems intended for AVR beginners as well as professionals, it has many features from professional IDEs as well as the simplicity of editing.

## Requirements
- gcc-avr, binutils-avr, avr0libc
- gdb-avr
- avrdude

## Installation and Running:

AVR4L works on any system that supports Openjdk 8 and Gradle.
The 'fatJar' task will build an executable jar that should run on any system.
There is script to install it onto any linux system, Windows users can run the Jar 
directly.

### Linux Installation

```
$ git clone https://github.com/abdalmoniem/AVR4L
$ cd AVR4L
$ ./gradlew fatJar
$ cd assets
$ ./install-linux.sh
```

### Windows Installation
- run the gradle task launch4j
- collect portable .exe from build/launch4j folder
