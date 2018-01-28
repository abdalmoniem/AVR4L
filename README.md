# AVR4L

![AVR4L Screenshot](https://raw.githubusercontent.com/abdalmoniem/AVR4L/master/assets/screenshot_8.png)

## About:
AVR4L aims to deliver an easy to use and user friendly Integrated Development Environment for developing Codes for microcontrollers based on Atmel's AVR families. AVR4L is a Creative coding / Integrated Development Environment for Linux operating systems intended for AVR beginners as well as professionals, it has many features from professional IDEs as well as the simplicity of editing.

## Installation and Running:
### Linux:
1. Make sure `Java JDK / JRE v1.8` is installed on your computer.
	1. How to install: [https://java.com/en/download/help/linux_x64_install.xml](https://java.com/en/download/help/linux_x64_install.xml)
	2. Set `JAVA_HOME` environment variable, by doing the following:
		1. Depending on where you installed your Java, you will need to provide the full path.
			For this example, I installed Oracle JDK 8 in the `/usr/lib/jvm/java-8-oracle` directory.
		2. Open the file `/etc/environment` and scroll to the end of the file and enter the following:
			
			`JAVA_HOME=/usr/lib/jvm/java-8-oracle`
		3. Save and source the file by doing:

			`. /etc/environment` or `source /etc/environment`
2. Clone/download this folder to your computer.
3. cd into the `Binaries/Linux` directory.
4. run `sudo -E ./install.sh` within this folder.
5. you can now type `avr4l` in any terminal session or find it in the dash if you have `Ubuntu` installed.

### Windows:
1. Make sure `Java JDK / JRE v1.8` is installed on your computer.
	1. How to install: [https://java.com/en/download/windows_offline.jsp](https://java.com/en/download/windows_offline.jsp)
3. Clone/download this folder to your computer.
4. cd into the `Binaries\Windows\installer project\AVR4L-SetupFiles` directory.
5. (recommended) install `AVR4L Setup.exe`
6. or run the `AVR4L.exe` directly from the `portable` folder.

	note: windows version is not fully tested and may be unstable, it is intended for cross platform compatability.
	
	note: if you want to use the portable version you must make sure that `portable\avr-tools\bin` and `portable\tools\bin` directories are added to the `PATH` system environment variable.

	The project's full focus is on the linux version as it aims to be an alternative to Atmel's Studio for linux users.

## Features:
1. Cross platform (Linux, Windows)
2. Creative coding environment with auto-complete, suggestions and many more features.
3. Two options for compiling and uploading your code:
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
	AVR4L requires super user permissions to upload sketches, you can either:
	1. `sudo avr4l` from a terminal
	2. or add your user to the `dialout` group, most programmers are listed in this group:

		`sudo adduser YOUR_USER dialout`

		note: replace `YOUR_USER` with your linux user

2. Upload fails with message `sudo no tty present and no askpass program specified`:
	AVR4L requires super user permissions to upload sketches, but has not been run as root (sudo),
	so when it tries to issue a command with sudo, there is no way for it to get the root password, so you can either:
	1. `sudo avr4l` from a terminal
	2. or prevent sudo commands from requesting passwords:
		
		in a terminal type:
		
		`sudo visudo`
		
		and add the following to the end of the file:

		`YOUR_USER ALL = NOPASSWD : ALL`

		note: replace `YOUR_USER` with your linux user