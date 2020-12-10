#!/bin/bash

docker build -t epaper-proxy -f infrastructure/docker/proxy.dockerfile .
docker run -it epaper-proxy sh

echo "API_URL=https://api.epaper.opendatahub.testingmachine.eu" > .env
touch local-tunnel.log
