import pandas as pd
import argparse
import sys

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
parser.add_argument('num_learning_rounds', type=int,
                    help='number of learning rounds')
parser.add_argument('num_testing_rounds', type=int,
                    help='number of testing rounds')
parser.add_argument("--pretty", type=str2bool, nargs='?',
                        const=True, default=True,
                        help="Pretty output mode")

args = parser.parse_args()

train_rounds = args.num_learning_rounds
test_rounds = args.num_testing_rounds

if args.pretty:
  print('\n### ROBOT WINS ###\n')

df = pd.read_csv('../logs/energy.txt', sep=",", header=None)
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
