#!/bin/bash

#################################################################
#	Filename: install.h														 #
#	Modification Date: Sun, Oct  9 2016 01:00:04						 #
#	Creator: hifnawy_moniem@hotmail.com									 #
#	Description: installs AVR-Studio for linux and all its		 #
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

echo "Installing..."

# install avr toolchain
apt-get install gcc-avr binutils-avr avr-libc
apt-get install gdb-avr
apt-get install avrdude

# check if the system already contains
# the program folder and delete it to
# start a fresh installation
if [ -d "/usr/share/avr-studio" ]
then
	rm -rf /usr/share/avr-studio
fi

# copy AVR-Studio core files and folders
mkdir /usr/share/avr-studio
cp -r ./bin/AVR-Studio.jar /usr/share/avr-studio
cp -r ./bin/lib /usr/share/avr-studio
cp -r ./bin/dependencies/RXTXcomm.jar $JAVA_HOME/jre/lib/ext/RXTXcomm.jar
cp -r ./bin/dependencies/librxtxSerial.so $JAVA_HOME/jre/lib/amd64/librxtxSerial.so
cp -r ./bin/dependencies/librxtxParallel.so $JAVA_HOME/jre/lib/amd64/librxtxParallel.so

cp -r ./bin/icon.png /usr/share/avr-studio
cp -r ./bin/avr-studio-about.png /usr/share/avr-studio
cp -r ./bin/avr-studio.desktop /usr/share/applications/avr-studio.desktop

chmod a+rw -R /usr/share/avr-studio/*
chmod a+x -R /usr/share/avr-studio/lib
chmod a+x /usr/share/applications/avr-studio.desktop

# check if an alias already exists
# if not add it
if [ -f "$HOME/.bash_aliases" ]
then
	if ! grep -Fxq "alias avr-studio=\"java -jar -splash:/usr/share/avr-studio/avr-studio-about.png /usr/share/avr-studio/AVR-Studio.jar\"" $HOME/.bash_aliases
	then
		echo "alias avr-studio=\"java -jar -splash:/usr/share/avr-studio/avr-studio-about.png /usr/share/avr-studio/AVR-Studio.jar\"" >> $HOME/.bash_aliases
		printf "\nAdded alias to $HOME/.bash_aliases\n"
	else
		printf "\nAlias already in $HOME/.bash_aliases\n"
	fi
fi

# update the system environment variables by
# sourcing the bash files
source $HOME/.bash_aliases
source $HOME/.bashrc

# check if installation has gone all the way down
if [ -f "/usr/share/avr-studio/AVR-Studio.jar" ]
then
	if [ -d "/usr/share/avr-studio/lib" ]
	then
		if [ -f "/usr/share/avr-studio/icon.png" ]
		then
			if [ -f "/usr/share/applications/avr-studio.desktop" ]
			then
				GREEN='\033[0;32m'
				NC='\033[0m'
				printf "${GREEN}\nInstalled Successfully !!!${NC}\n"
				printf "\nPLEASE RESTART TERMINAL !!!\n"
			fi
		fi
	fi
else
	RED='\033[0;31m'
	NC='\033[0m'
	printf "${RED}\nProblems occured during installation !!!${NC}\n"
	printf "${RED}Please consider running the script with sudo${NC}\n"
fi
