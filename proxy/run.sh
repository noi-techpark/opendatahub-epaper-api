#!/bin/bash

lt -p 5000 > local-tunnel.log &
sleep 3
echo "local-tunnel started"
python3 proxy.py > proxy.log &
echo "proxy started"
