#!/bin/sh

echo "$KEY" | sed -e "s/'/\"/g" > /root/config.json

exec "$@"
