#!/usr/bin/env bash
set -e

#################
# user settings #
#################

if [ "$#" -ne 7 ]; then
  echo "Illegal number of parameters."
  echo
  echo "Usage: $0 <fresh_qtable=0/1> <train_rounds> <test_rounds> <alpha_divisor> <min_alpha> <gamma> <enable_display=0/1>"
  echo "Example: $0 1 10000 500 222 0.1 0.9 0"
  exit 1
fi
FRESH_QTABLE="$1"
TRAIN_ROUNDS="$2"
TEST_ROUNDS="$3"
ALPHA_DIVISOR="$4"
MIN_ALPHA="$5"
GAMMA="$6"
ENABLE_DISPLAY="$7"

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

# clean old files
rm -f ${SCRIPT_PATH}/logs/*
if [[ $FRESH_QTABLE -eq "1" ]]; then
  rm -f ${SCRIPT_PATH}/robocode/robots/iwium/QLearningRobot.data/qtable.bin
fi

# JVM arguments
JVM_ARGS=""
JVM_ARGS="${JVM_ARGS} -Xmx10G"
JVM_ARGS="${JVM_ARGS} -DNOSECURITY=true"
JVM_ARGS="${JVM_ARGS} -Dlog4j.configurationFile=${SCRIPT_PATH}/log4j2.xml -DlogPath=${SCRIPT_PATH}/logs"
JVM_ARGS="${JVM_ARGS} -DtrainRounds=${TRAIN_ROUNDS}"
JVM_ARGS="${JVM_ARGS} -DalphaDivisor=${ALPHA_DIVISOR}"
JVM_ARGS="${JVM_ARGS} -DminAlpha=${MIN_ALPHA}"
JVM_ARGS="${JVM_ARGS} -Dgamma=${GAMMA}"

# Robocode arguments

ROBOCODE_ARGS=""

BATTLE_PATH=$(mktemp --suffix .battle)
ROUNDS_NUM=$(($TRAIN_ROUNDS + $TEST_ROUNDS))
printf "robocode.battle.numRounds=${ROUNDS_NUM}\n" >> ${BATTLE_PATH}
printf "robocode.battle.selectedRobots=iwium.QLearningRobot*,sample.SpinBot\n" >> ${BATTLE_PATH}
#printf "robocode.battle.initialPositions=(224,240,60)" >> ${BATTLE_PATH}
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

(
  cd ${SCRIPT_PATH}/utils
  pipenv run python states_hist.py
  pipenv run python plot_stats.py  $TRAIN_ROUNDS $TEST_ROUNDS
  pipenv run python win_stats.py  $TRAIN_ROUNDS $TEST_ROUNDS
)
