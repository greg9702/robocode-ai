#!/usr/bin/env python

import json
import os

SCRIPT_PATH = os.path.dirname(os.path.realpath(__file__))

def prettyPrint(d, indent=0):
  for key, value in d.items():
    if indent == 0:
      print(key)
      print('-' * len(key))
    else:
      print(str(key).rjust(13, ' ') + ': ', end='')
    if isinstance(value, dict):
      prettyPrint(value, indent+1)
      print('')
    else:
      print(value)

print('\n### DETAILS OF EXPLORED STATES ###\n')
stats = {}

with open(SCRIPT_PATH + "/../logs/table.txt", "r") as f:
  lines = f.read().split('\n')[1:-1]

  for line in lines:
    splitter = ": "
    key = line.split(splitter)[0]
    value = line.split(splitter)[1]
    # print('value:', value)
    splitter = ";"
    state = key.split(splitter)[0]
    action = key.split(splitter)[1]
    # print('action:', action)
    splitter = ",m_"
    state_params = state.split(splitter)

    for param in state_params:
      splitter = ","
      param_values = param.split(splitter)
      # print('param:', param_values)
      name = param_values[0]
      minx = param_values[1]
      maxx = param_values[2]
      buckets = int(param_values[3])
      value_q = param_values[4]

      if name not in stats:
        stats[name] = {
          'min': minx,
          'max': maxx,
          'buckets': int(buckets),
          'occurences': [0] * buckets,
          'nulls': 0,
          'TOTAL': 0
        }

      if value_q == 'NULL':
        stats[name]['nulls'] += 1
      else:
        stats[name]["occurences"][int(value_q)] += 1
      stats[name]['TOTAL'] += 1

prettyPrint(stats)
