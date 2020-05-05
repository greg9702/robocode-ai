#!/usr/bin/env bash
set -e

#################
# user settings #
#################

ENABLE_DISPLAY=0
ROUNDS=100


########
# core #
########

# determine script path
pushd . > /dev/null
SCRIPT_PATH="${BASH_SOURCE[0]}"
if ([ -h "${SCRIPT_PATH}" ]); then
  while([ -h "${SCRIPT_PATH}" ]); do cd `dirname "$SCRIPT_PATH"`;
  SCRIPT_PATH=`readlink "${SCRIPT_PATH}"`; done
fi
cd `dirname ${SCRIPT_PATH}` > /dev/null
SCRIPT_PATH=`pwd`;
popd  > /dev/null

# JVM arguments
JVM_ARGS="-Xmx1024m -DNOSECURITY=true -Dlog4j.configurationFile=${SCRIPT_PATH}/log4j2.xml"

# Robocode arguments

ROBOCODE_ARGS=""

BATTLE_PATH=$(mktemp --suffix .battle)
printf "robocode.battle.numRounds=${ROUNDS}\n" >> ${BATTLE_PATH}
printf "robocode.battle.selectedRobots=iwium.QLearningRobot*,sample.SpinBot\n" >> ${BATTLE_PATH}
ROBOCODE_ARGS="${ROBOCODE_ARGS} -battle ${BATTLE_PATH}"

if [[ $ENABLE_DISPLAY -eq "0" ]]; then
  ROBOCODE_ARGS="${ROBOCODE_ARGS} -nodisplay"
fi

export _JAVA_AWT_WM_NONREPARENTING=1
(
  cd ${SCRIPT_PATH}/robocode
  java ${JVM_ARGS} -cp ./libs/robocode.jar:${SCRIPT_PATH}/log4j/log4j-api-2.13.2.jar:${SCRIPT_PATH}/log4j/log4j-core-2.13.2.jar robocode.Robocode ${ROBOCODE_ARGS}
)


###########
# results #
###########

# gnuplot -p -e 'plot "/tmp/tes"'
