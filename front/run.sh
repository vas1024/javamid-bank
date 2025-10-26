#!/bin/bash
mvn clean package
docker build -t front:latest .
kind load docker-image front:latest 
helm upgrade --install front ./chart
