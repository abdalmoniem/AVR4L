#!/bin/bash

#################################################################
#	Filename: install.h														 #
#	Modification Date: Sun, Oct  9 2016 01:00:04						 #
#	Creator: hifnawy_moniem@hotmail.com									 #
#	Description: installs AVR4L for linux and all its		 #
#					 dependencies and creates an alias for it.		 #
#################################################################

# check if user is root
if [[ $(id -u) -ne 0 ]]
then
	echo "This script installs and modifies some system files, please run as root or sudo."
	exit 1
fi

if [ -z $JAVA_HOME ]
then
	echo "You called the script without passing the environment variables, please run as sudo with -E switch."
	exit 1
fi

echo "Starting AVR4L installation..."

# install avr toolchain
apt-get install gcc-avr binutils-avr avr-libc
apt-get install gdb-avr
apt-get install avrdude

# check if the system already contains
# the program folder and delete it to
# start a fresh installation
if [ -d "/usr/share/avr4l" ]
then
	rm -rf /usr/share/avr4l
fi

# copy AVR4L core files and folders
mkdir /usr/share/avr4l
cp -r ./AVR4L.jar /usr/share/avr4l
cp -r ./lib /usr/share/avr4l
cp -r ./dependencies/RXTXcomm.jar $JAVA_HOME/jre/lib/ext/RXTXcomm.jar
cp -r ./dependencies/librxtxSerial.so $JAVA_HOME/jre/lib/amd64/librxtxSerial.so
cp -r ./dependencies/librxtxParallel.so $JAVA_HOME/jre/lib/amd64/librxtxParallel.so

cp -r ./icon.png /usr/share/avr4l
cp -r ./avr4l-about.png /usr/share/avr4l
cp -r ./avr4l.desktop /usr/share/applications/avr4l.desktop
cp -r ./avr4l /usr/bin

chmod a+rw -R /usr/share/avr4l/*
chmod a+x -R /usr/share/avr4l/lib
chmod a+x /usr/share/applications/avr4l.desktop
chmod a+x /usr/bin/avr4l

# check if an alias already exists
# if not add it
# if [ -f "$HOME/.bash_aliases" ]
# then
# 	if ! grep -Fxq "alias avr4l=\"java -jar -splash:/usr/share/avr4l/avr4l-about.png /usr/share/avr4l/AVR4L.jar\"" $HOME/.bash_aliases
# 	then
# 		echo "alias avr4l=\"java -jar -splash:/usr/share/avr4l/avr4l-about.png /usr/share/avr4l/AVR4L.jar\"" >> $HOME/.bash_aliases
# 		printf "\nAdded alias to $HOME/.bash_aliases\n"
# 	else
# 		printf "\nAlias already in $HOME/.bash_aliases\n"
# 	fi
# else
# 	echo "alias avr4l=\"java -jar -splash:/usr/share/avr4l/avr4l-about.png /usr/share/avr4l/AVR4L.jar\"" >> $HOME/.bash_aliases
# fi

# update the system environment variables by
# sourcing the bash files
source $HOME/.bash_aliases
source $HOME/.bashrc

# check if installation has gone all the way down
if [ -f "/usr/share/avr4l/AVR4L.jar" ]
then
	if [ -d "/usr/share/avr4l/lib" ]
	then
		if [ -f "/usr/share/avr4l/icon.png" ]
		then
			if [ -f "/usr/share/applications/avr4l.desktop" ]
			then
				GREEN='\033[0;32m'
				NC='\033[0m'
				printf "${GREEN}\nAVR4L was installed successfully !!!${NC}\n\n"
				# printf "\nPLEASE RESTART TERMINAL !!!\n"
			fi
		fi
	fi
else
	RED='\033[0;31m'
	NC='\033[0m'
	printf "${RED}\nProblems occured during installation !!!${NC}\n"
	printf "${RED}Please consider running the script with sudo${NC}\n"
fi