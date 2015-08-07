#!/usr/bin/env bash

#Sync with govtrack
sh /root/code/scripts/sync.sh

#Run Job
cd /root/code
lein run "/root/code/resources/config.edn"
