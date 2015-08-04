#!/usr/bin/env bash

#Sync with govtrack
sh ./sync.sh

#Run Job
cd /root/code
lein run "../resources/config.edn"
