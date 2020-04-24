#!/usr/bin/env bash

pushd . > /dev/null
SCRIPT_PATH="${BASH_SOURCE[0]}"
if ([ -h "${SCRIPT_PATH}" ]); then
  while([ -h "${SCRIPT_PATH}" ]); do cd `dirname "$SCRIPT_PATH"`;
  SCRIPT_PATH=`readlink "${SCRIPT_PATH}"`; done
fi
cd `dirname ${SCRIPT_PATH}` > /dev/null
SCRIPT_PATH=`pwd`;
popd  > /dev/null

export JAVA_HOME=/usr/lib/jvm/java-8-openjdk
export CLASSPATH=/usr/lib/jvm/java-8-openjdk/lib

echo "Compiling robot..."
javac -classpath ${SCRIPT_PATH}/robocode/libs/robocode.jar ${SCRIPT_PATH}/my-robot/QLearningRobot.java

echo "Copying robot..."
mkdir -p ${SCRIPT_PATH}/robocode/robots/iwium
cp ${SCRIPT_PATH}/my-robot/QLearningRobot.class ${SCRIPT_PATH}/robocode/robots/iwium/

echo "Done!"
