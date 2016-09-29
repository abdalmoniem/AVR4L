#!/bin/bash

echo "Installing..."

#apt-get update
apt-get install gcc-avr binutils-avr avr-libc
apt-get install gdb-avr
apt-get install avrdude

if [ -d "/usr/share/avr-studio" ]
then
	rm -rf /usr/share/avr-studio
fi

mkdir /usr/share/avr-studio
cp -r ./bin/AVR-Studio.jar /usr/share/avr-studio
cp -r ./bin/icon.png /usr/share/avr-studio
cp -r ./bin/avr-studio.desktop /usr/share/applications/avr-studio.desktop
chmod 777 /usr/share/applications/avr-studio.desktop

if [ -f "$HOME/.bash_aliases" ]
then
	if ! grep -Fxq "alias avr-studio=\"java -jar /usr/share/avr-studio/AVR-Studio.jar\"" $HOME/.bash_aliases
	then
		echo "alias avr-studio=\"java -jar /usr/share/avr-studio/AVR-Studio.jar\"" >> $HOME/.bash_aliases
		echo "added alias to $HOME/.bash_aliases"
	else
		echo "alias already in $HOME/.bash_aliases"
	fi
fi
source $HOME/.bash_aliases
source $HOME/.bashrc

if [ -f "/usr/share/avr-studio/AVR-Studio.jar" ]
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
	
else
	RED='\033[0;31m'
	NC='\033[0m'
	printf "${RED}\nProblems occured during installation !!!${NC}\n"
	printf "${RED}Please consider running the script with sudo${NC}\n"
fi
