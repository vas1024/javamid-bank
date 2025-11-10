#!/bin/bash

SERVICES=("accounts" "auth" "blocker" "cash"  "exchange" "exgen" "front" "notify" "transfer")

for SERVICE in "${SERVICES[@]}"; do
    echo "Building $SERVICE..."
    cd $SERVICE
    mvn clean package -DskipTests
    docker build -t $SERVICE:latest .
    kind load docker-image $SERVICE:latest
    kind load docker-image $SERVICE:latest
    helm upgrade --install $SERVICE ./chart --force
    kubectl rollout restart deployment/$SERVICE
    kubectl rollout restart statefulset/$SERVICE
    cd ..
done

