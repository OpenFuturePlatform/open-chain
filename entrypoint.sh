#!/bin/sh

echo  "$KEY" | tr -d '\r' >> /root/config.json

exec "$@"
