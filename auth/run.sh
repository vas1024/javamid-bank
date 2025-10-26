#!/bin/bash
mvn clean package
docker build -t auth:latest .
kind load docker-image auth:latest 
helm upgrade --install auth ./chart
