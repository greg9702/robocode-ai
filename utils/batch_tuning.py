#!/usr/bin/env python

import matplotlib.pyplot as plt
import matplotlib.image as mpimg
import numpy as np
import math
import subprocess

LEARNING_ROUNDS = 1000
TESTING_ROUNDS = 1000

REPEAT_TIMES = 5

# Grid of params
min_alphas = [0.1, 0.2, 0.3, 0.5, 0.7]
min_epsilons = [-99999] # unused
gammas = [1, 0.9, 0.6, 0.3, 0.15]
learn_divisors = [50, 300, 800, 1000, 5000, 10000]
# learn_divisors = [10000, 20000, 40000, 80000]
explore_divisors = [-9999] #unused

# Store all results into one array
results = []

total_params = len(min_alphas) * len(min_epsilons) * len(gammas) * len(learn_divisors) * len(explore_divisors)
iteration = 1

print('Batch search [{} learning rounds, {} testing rounds, avg of {} runs]'.format(LEARNING_ROUNDS, TESTING_ROUNDS, REPEAT_TIMES))
print('=========================')

# Exploring parameters
for min_alpha in min_alphas:
  for min_epsilon in min_epsilons:
    for gamma in gammas:
      for learn_divisor in learn_divisors:
        for explore_divisor in explore_divisors:

          # Debug ouptut
          print('        ' + str(iteration) + '/' + str(total_params) + ': ', end='', flush=True)

          learning_wins = []
          testing_wins = []

          for i in range(REPEAT_TIMES):

            # Run robocode
            pr = subprocess.run(["../run.sh", "1", str(LEARNING_ROUNDS), str(TESTING_ROUNDS), str(learn_divisor), str(min_alpha), str(gamma), '0'], capture_output=True)
            if pr.returncode != 0:
              raise Exception('Error while running robocode:', pr.stdout, pr.stderr)
            xd = pr.stdout

            # Get win stats
            pr = subprocess.run(["python", "./win_stats.py", "--pretty", "False"], capture_output=True)
            if pr.returncode != 0:
              raise Exception('Error while running win_stats:', pr.stdout, pr.stderr)
            wins = pr.stdout.split(b'\n')
            learning_wins.append(int(wins[0]))
            testing_wins.append(int(wins[1]))

            print('.', end='', flush=True)


          avg_learning_wins = np.average(np.array(learning_wins))
          avg_testing_wins = np.average(np.array(testing_wins))
          print('\n{}% (from {}%) | min_α: {}, learn_λ: {}, γ: {}'.format(
            round(avg_testing_wins, 2), round(avg_learning_wins, 2), min_alpha, learn_divisor, gamma,
          ), flush=True)

          iteration += 1


