#!/bin/bash

docker build -t epaper-proxy -f infrastructure/docker/proxy.dockerfile .
docker run -it epaper-proxy sh
