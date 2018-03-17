#!/bin/bash

# Argument: The jar file to deploy
APPSRCPATH=$1

# Argument: application name, no spaces please, used as folder name under /var
APPNAME=$2

# Argument: the user to use when running the application, may exist, created if not exists
APPUSER=$3

# Help text
USAGE="
Usage: sudo $0 <jar-file> <app-name> <runtime-user>
If an app with the name <app-name> already exist, it is stopped and deleted.
If the <runtime-user> does not already exist, it is created.
"

# Check that we are root
if [ ! "root" = "$(whoami)" ]; then
    echo "Must be root. Please use e.g. sudo"
    echo "$USAGE"
    exit
fi

# Check arguments
if [ "$#" -ne 3 -o ${#APPSRCPATH} = 0 -o ${#APPNAME} = 0 -o ${#APPUSER} = 0 ]; then
    echo "Incorrect number of parameters."
    echo "$USAGE"
    exit
fi

if [ ! -f $APPSRCPATH ]; then
    echo "Can't find jar file $APPSRCPATH"
    echo "$USAGE"
    exit
fi

# Infered values
APPFILENAME=$(basename $APPSRCPATH)
APPFOLDER=/var/javaapps/$APPNAME
APPDESTPATH=$APPFOLDER/$APPFILENAME

# Stop the service if it already exist and is running
systemctl stop $APPNAME >/dev/null 2>&1

# Create the app folder, deleting any previous content
rm -f $APPFOLDER/*.jar
mkdir -p $APPFOLDER

# Create the user if it does not exist
if id "$APPUSER" >/dev/null 2>&1; then
    echo "Using existing user $APPUSER"
else
    adduser --disabled-password --gecos "" $APPUSER
    echo "Created user $APPUSER"
fi

# Place app in app folder, setting owner and rights
cp $APPSRCPATH $APPDESTPATH
chown $APPUSER $APPDESTPATH
chown $APPUSER $APPFOLDER
chmod 500 $APPDESTPATH
echo "Added or updated the $APPDESTPATH file"

# Create the .service file used by systemd
echo "
[Unit]
Description=$APPNAME
After=syslog.target
Wants=network-online.target
After=network-online.target
[Service]
User=$APPUSER
ExecStart=/usr/bin/java -jar $APPDESTPATH
SuccessExitStatus=143
WorkingDirectory=$APPFOLDER
[Install]
WantedBy=multi-user.target
" > /etc/systemd/system/$APPNAME.service
echo "Created the /etc/systemd/system/$APPNAME.service file"

# Reload the daemon
systemctl daemon-reload
sudo systemctl enable $APPNAME.service