import pandas as pd
import matplotlib.pyplot as plt
import argparse
import sys

parser = argparse.ArgumentParser(description='Prints winning statistics of logs')
parser.add_argument('num_learning_rounds', type=int,
                    help='number of learning rounds')
parser.add_argument('num_testing_rounds', type=int,
                    help='number of testing rounds')
parser.add_argument('--save_rewards', action='store', help='Filename to save reward charts')
parser.add_argument('--save_states', action='store', help='Filename to save states charts')
args = parser.parse_args()

train_rounds = args.num_learning_rounds
test_rounds = args.num_testing_rounds
ma_window = int(test_rounds/2)

print('Plotting rewards...')
rewards_df = pd.read_csv('../logs/rewards.txt', sep=",", header=None)
rewards_df.columns = ["val"]
plt.style.use('ggplot')
plt.plot(rewards_df['val'][:train_rounds].rolling(ma_window).mean(), label='Learning')
plt.plot(rewards_df['val'][-test_rounds:].rolling(ma_window).mean(), label='Testing')
plt.legend(loc='best')
plt.suptitle('')
plt.title('Robocode AI - MA of rewards\nWindow size: ' + str(ma_window) + ' rounds')
if args.save_rewards:
  plt.savefig(args.save_rewards)
else:
  plt.show()

print('Plotting states...')
states_df = pd.read_csv('../logs/states.txt', sep=",", header=None)
states_df.columns = ["val"]
plt.style.use('ggplot')
plt.plot(states_df['val'][:train_rounds], label='Learning')
plt.plot(states_df['val'][-test_rounds:], label='Testing')
plt.legend(loc='best')
plt.suptitle('')
plt.title('Robocode AI - number of explored states')
if args.save_states:
  plt.savefig(args.save_states)
else:
  plt.show()
