import pandas as pd
import argparse
import sys
import os

SCRIPT_PATH = os.path.dirname(os.path.realpath(__file__))

def str2bool(v):
    if isinstance(v, bool):
       return v
    if v.lower() in ('yes', 'true', 't', 'y', '1'):
        return True
    elif v.lower() in ('no', 'false', 'f', 'n', '0'):
        return False
    else:
        raise argparse.ArgumentTypeError('Boolean value expected.')

parser = argparse.ArgumentParser(description='Prints winning statistics of logs')
parser.add_argument("--pretty", type=str2bool, nargs='?',
                        const=True, default=True,
                        help="Pretty output mode")

args = parser.parse_args()

if args.pretty:
  print('\n### ROBOT WINS ###\n')

train_rounds = 0
test_rounds = 0

with open(SCRIPT_PATH + '/../logs/env.txt') as f:
  env = f.read().split('\n')
  train_rounds = int(env[0])
  test_rounds = int(env[1])

if args.pretty:
  print('Learning rounds:', train_rounds)
  print('Testing rounds:', test_rounds, '\n')

df = pd.read_csv(SCRIPT_PATH + '/../logs/energy.txt', sep=",", header=None)
df.columns = ["val"]

train_df = df[:train_rounds]
test_df = df[-test_rounds:]

win_rate_learn = int(100 * train_df[train_df.val > 0.0].count() / train_df.count())
win_rate_test = int(100 * test_df[test_df.val > 0.0].count() / test_df.count())

if args.pretty:
  print(' - during learning phase: ', str(win_rate_learn) + '%')
  print(' - during testing phase:  ', str(win_rate_test) + '%')
else:
  print(win_rate_learn)
  print(win_rate_test)
