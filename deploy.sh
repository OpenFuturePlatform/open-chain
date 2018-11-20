#!/bin/bash

# GRADLE
./gradlew clean build -x test

# CREATE IMAGE APP
docker build -t open-node:latest -f docker/Dockerfile .

for i in 1 2 3 4 5 6 7 8 9
do
    # ENVIRONMENT VARIABLES
    RPC_PORT=808$i
    NODE_PORT=919$i
    if [ ${NODE_PORT} != 9191 ]
    then
        SEED_NODE=127.0.0.1:9191
    else
        SEED_NODE=127.0.0.1:9192
    fi

    # RUN APP
    docker create \
        --name node$i \
        --network host \
        -e RPC_PORT=${RPC_PORT} \
        -e NODE_PORT=${NODE_PORT} \
        -e SEED_NODE=${SEED_NODE} \
        open-node:latest

    # COPY  KEYS TO CONTAINERS
    docker cp docker/keys/node$i/config.json node$i:/root

    # START CONTAINER
    docker start node$i
done