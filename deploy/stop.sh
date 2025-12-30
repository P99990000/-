#!/bin/bash

APP_NAME=dorm-system-0.0.1-SNAPSHOT.jar

PID=$(ps -ef | grep $APP_NAME | grep -v grep | awk '{print $2}')

if [ -z "$PID" ]; then
    echo "$APP_NAME is not running."
else
    echo "Stopping $APP_NAME (PID: $PID)..."
    kill $PID
    echo "Stopped."
fi
