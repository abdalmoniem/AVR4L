# AVR-Studio

![AVR-Studio Screenshot](https://raw.githubusercontent.com/abdalmoniem/AVR-Studio/master/assets/screenshot_5.png)

## About:
AVR-Studio aims to deliver an easy to use and user friendly Integrated Development Environment for developing Codes for microcontrollers based on Atmel's AVR families. AVR-Studio is a Creative coding / Integrated Development Environment for Linux operating systems intended for AVR beginners as well as professionals, it has many features from professional IDEs as well as the simplicity of editing.

## Installation and Running:
### Linux:
1. Make sure `Java JDK / JRE v1.8` is installed on your computer.
	1. How to install: [https://java.com/en/download/help/linux_x64_install.xml](https://java.com/en/download/help/linux_x64_install.xml)
2. Clone/download this folder to your computer.
3. cd into the `Binaries` directory.
4. run `sudo -E ./install.sh` within this folder.
5. restart your terminal session in order to activate the program's alias.
6. you can now type `avr-studio` in any terminal session or find it in the dash if you have `Ubuntu` installed.

### Windows:
1. Make sure `Java JDK / JRE v1.8` is installed on your computer.
2. Make sure that you have the `AVR Toolchain` installed and in the environment path.

[Install from here](http://www.atmel.com/tools/atmelavrtoolchainforwindows.aspx)

3. Clone/download this folder to your computer.
4. cd into the Binaries/bin directory.
5. run `AVR-Studio.jar`

note: windows version is not fully tested and may be unstable,
it is intended for cross platform compatability.

The project's full focus is on the linux version as it aims to be an alternative to Atmel's Studio for linux users.


### Mac OSX:
further development and testing is needed.

## Features:
1. Cross platform (Linux, Mac OSX, Windows)
2. Creative coding environment with auto-complete, suggestions and other features.
3. Two options for compiling your code:
	1. standard mode.
	2. makefile mode.
4. Compiling .c files of various atmega parts or microcontrollers.
5. Uploading .hex files to various atmega parts or microcontrollers.
6. Supports many programmers:
	1. AVR-ISP (avrisp)
	2. Atmel STK500 Version 1.x firmware (stk500v1)
	3. Arduino (arduino)
	4. USB-ASP (usbasp)
7. Console area shows results of compilation and uploading.

## Known Issues:
1. Upload fails with message `permission denied`:
	AVR-Studio requires super user permissions to upload sketches, you can either:
	1. `sudo avr-studio` from a terminal
	2. or add your user to the `dialout` group, most programmers are listed in this group:

		`sudo adduser YOUR_USER dialout`

		note: replace `YOUR_USER` with your linux user

2. Upload fails with message `sudo no tty present and no askpass program specified`:
	AVR-Studio requires super user permissions to upload sketches, but has not been run as root (sudo),
	so when it tries to issue a command with sudo, there is no way for it to get the root password, so you can either:
	1. `sudo avr-studio` from a terminal
	2. or prevent sudo commands from requesting passwords:
		
		in a terminal type:
		
		`sudo visudo`
		
		and add the following to the end of the file:

		`YOUR_USER ALL = NOPASSWD : ALL`

		note: replace `YOUR_USER` with your linux user