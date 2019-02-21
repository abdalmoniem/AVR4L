#!/bin/bash

# check folder
if [ -d /usr/share/AVR4L ]; then
	sudo rm -r /usr/share/AVR4L
	
	if [ -f /usr/share/applications/AVR4L.desktop ]; then
		sudo rm /usr/share/applications/AVR4L.desktop
	fi

	if [ -f /usr/bin/avr4l ]; then
		sudo rm /usr/bin/avr4l
	fi
fi

# move files over
sudo mkdir /usr/share/AVR4L
sudo cp -r ../build/libs /usr/share/AVR4L
sudo cp -r ../build/resources /usr/share/AVR4L
sudo cp AVR4L.desktop /usr/share/applications/
sudo cp avr4l /usr/bin

