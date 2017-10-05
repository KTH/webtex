#!/bin/sh

set -e

if [ "$*" = "start" ]; then
  exec /usr/local/tomcat/bin/catalina.sh run
fi

exec $*