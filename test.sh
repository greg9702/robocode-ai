#!/usr/bin/env bash
set -e

pushd . > /dev/null
SCRIPT_PATH="${BASH_SOURCE[0]}"
if ([ -h "${SCRIPT_PATH}" ]); then
  while([ -h "${SCRIPT_PATH}" ]); do cd `dirname "$SCRIPT_PATH"`;
  SCRIPT_PATH=`readlink "${SCRIPT_PATH}"`; done
fi
cd `dirname ${SCRIPT_PATH}` > /dev/null
SCRIPT_PATH=`pwd`;
popd  > /dev/null

java -classpath ${SCRIPT_PATH}/robocode/robots/:${SCRIPT_PATH}/log4j/log4j-api-2.13.2.jar:${SCRIPT_PATH}/log4j/log4j-core-2.13.2.jar -ea iwium.Test
