#!/bin/bash
mvn clean package
docker build -t accounts:latest .
kind load docker-image accounts:latest 
helm upgrade --install accounts .
