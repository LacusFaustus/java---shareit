#!/bin/sh
# Этот скрипт запустится перед docker build в CI
if [ -f target/placeholder ]; then
    cp target/placeholder target/app.jar
fi
