#!/usr/bin/env python

import os
import numpy as np
import pandas as pd
from matplotlib import pyplot as plt

SCRIPT_PATH = os.path.dirname(os.path.realpath(__file__))
FILE_PATH = "/../robocode/robots/iwium/MiddleRobot.data/middleQtable.bin"

NO_HEADING_BINS = 8
NO_X_BINS = 8
NO_Y_BINS = 8

X = pd.read_csv(SCRIPT_PATH + FILE_PATH, sep=" ", header=None, skiprows=2, usecols=range(5))

q_of_all_actions: np.ndarray = X.sum(axis=1).values

q_of_all_headings = [sum(q_of_all_actions[current: current + NO_HEADING_BINS]) for current in
                     range(0, len(q_of_all_actions), NO_HEADING_BINS)]

Q = np.ndarray(shape=(NO_X_BINS, NO_Y_BINS))

for y in range(NO_Y_BINS):
    for x in range(NO_X_BINS):
        Q[x, y] = q_of_all_headings[x + y * NO_X_BINS]

print(Q)

plt.figure(figsize=(16, 8))
im = plt.imshow(Q.T, vmin=Q.min(), vmax=Q.max(), origin='lower', cmap='PuRd')
plt.xticks(np.linspace(0, NO_X_BINS - 1, NO_X_BINS), range(NO_X_BINS))
plt.yticks(np.linspace(0, NO_Y_BINS - 1, NO_Y_BINS), range(NO_Y_BINS))
plt.xlabel('X', fontsize=16)
plt.ylabel('Y', fontsize=16)
plt.colorbar(im)
plt.show()

######################################################################################
