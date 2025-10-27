#!/bin/bash
SERVICE_NAME=$1
if [ -z "$SERVICE_NAME" ]; then
    echo "Usage: $0 <service-name>"
    echo "Available services: accounts, front, auth, transfer"
    exit 1
fi

echo "processing with  $SERVICE_NAME"

cd $SERVICE_NAME
mvn clean package -DskipTest
docker build -t $SERVICE_NAME:latest .
kind load docker-image $SERVICE_NAME:latest
helm upgrade --install $SERVICE_NAME ./chart
cd ..