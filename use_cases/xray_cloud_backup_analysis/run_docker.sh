#!/bin/bash

# check if arguments are provided or else print usage
if [ $# -ne 4 ]; then
    echo "Usage: ./run_docker.sh <path_to_metadata> <path_to_attachments> <client_id> <client_secret>"
    exit 1
fi

DATA_DIR=$1
ATTACH_DIR=$2
CLIENT_ID=$3
CLIENT_SECRET=$4
docker rm my-xray-script > /dev/null 2>&1
docker run -it --rm --name my-xray-script -v $DATA_DIR:/usr/src/app/backup_metadata -v $ATTACH_DIR:/usr/src/app/backup_attachments xray-cloud-data-analysis -m /usr/src/app/backup_metadata  -a /usr/src/app/backup_attachments -i $CLIENT_ID -s $CLIENT_SECRET