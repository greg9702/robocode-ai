import pandas as pd
import sys

train_rounds = int(sys.argv[1])
test_rounds = int(sys.argv[2])

print('\n### ROBOT WINS ###\n')
df = pd.read_csv('../logs/energy.txt', sep=",", header=None)
df.columns = ["val"]

train_df = df[:train_rounds]
test_df = df[-test_rounds:]

print(' - during learning phase: ', str(int(100 * train_df[train_df.val > 0.0].count() / train_df.count())) + '%')
print(' - during testing phase:  ', str(int(100 * test_df[test_df.val > 0.0].count() / test_df.count())) + '%')
