#!/bin/sh

set -x

D=$(date '+%Y-%m-%d %H:%M:%S')
COUNT=$(ps auxww | grep 'ssh -N -R 19998'| grep 'idm'|grep -v grep| wc -l)

if [ "$COUNT" -eq "0" ]; then
    echo "$D: epaper-tunnel down, restarting..."
    ssh -N -R 19998:localhost:5000 -i ~/.ssh/epaperproxy epaperproxy@docker02.testingmachine.eu -o ExitOnForwardFailure=True &
    if [ $? -ne 0 ]; then
        echo "$D: restarting the epaper-tunnel failed!"
    else
        echo "$D: epaper-tunnel up."
    fi
fi
