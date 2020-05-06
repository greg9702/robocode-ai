import pandas as pd
import matplotlib.pyplot as plt
import sys

train_rounds = int(sys.argv[1])
test_rounds = int(sys.argv[2])
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
plt.show()
