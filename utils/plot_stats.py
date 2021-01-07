#!/usr/bin/env python

import matplotlib.pyplot as plt
import numpy as np
import pandas as pd
import os

SCRIPT_PATH = os.path.dirname(os.path.realpath(__file__))
MA_WINDOW = 50

def loadEnv():

  # learnRounds = 0
  # testRounds = 0
  #
  # with open(SCRIPT_PATH + '/../logs/env.txt') as f:
  #   content = f.read()
  #   lines = content.split('\n')
  #   learnRounds = int(lines[0])
  #   testRounds = int(lines[1])
  # return (learnRounds, testRounds)
  return (1000, 0)

def loadRewards():

  rewardsDf = pd.read_csv(SCRIPT_PATH + '/../logs/rewards.txt', sep=",", header=None)
  rewardsDf.columns = ["val"]

  return rewardsDf

def loadWins():

  energyDf = pd.read_csv(SCRIPT_PATH + '/../logs/energy.txt', sep=",", header=None)
  energyDf.columns = ["val"]
  energyDf[energyDf != 0] = 100 # replace energy with perecentage win

  return energyDf

def loadExploredStates():

  statesDf = pd.read_csv(SCRIPT_PATH + '/../logs/states.txt', sep=",", header=None)
  statesDf.columns = ["val"]

  return statesDf

def loadHyperparams():

  alphas = []
  epsilons = []
  gammas = []

  with open(SCRIPT_PATH + '/../logs/hyperparams.txt') as f:
    content = f.read()
    lines = content.split('\n')
    lines = list(filter(None, lines))
    for line in lines:
      parts = line.split('|')
      alpha = float(parts[0])
      alphas.append(alpha)
      epsilon = float(parts[1])
      epsilons.append(epsilon)
      gamma = float(parts[2])
      gammas.append(gamma)

  return (alphas, epsilons, gammas)

def makePlots():

  # prepare data
  learnRounds, testRounds = loadEnv()

  # calculate data
  totalRounds = learnRounds + testRounds
  x_vals = np.arange(1, totalRounds+1)

  # prepare subplots
  fig = plt.figure()
  plt.style.use('ggplot')
  fig.subplots_adjust(hspace=1.0)
  ax1 = fig.add_subplot(6, 1, 1)
  ax2 = fig.add_subplot(6, 1, 2)
  ax3 = fig.add_subplot(6, 1, 3)
  ax4 = fig.add_subplot(6, 1, 4)
  ax5 = fig.add_subplot(6, 1, 5)
  ax6 = fig.add_subplot(6, 1, 6)

  rewardsDf = loadRewards()

  ax1.title.set_text('MA of rewards\nwindows size: ' + str(MA_WINDOW))
  ax1.plot(rewardsDf.rolling(MA_WINDOW).mean())
  ax1.axvline(x=learnRounds, color="black", linestyle=":", linewidth=3.0)

  # winsDf = loadWins()
  #
  # ax2.title.set_text('MA of wins percentage\nwindows size: ' + str(MA_WINDOW))
  # ax2.plot(winsDf.rolling(MA_WINDOW).mean())
  # ax2.axvline(x=learnRounds, color="black", linestyle=":", linewidth=3.0)
  #
  # statesDf = loadExploredStates()
  #
  # ax3.title.set_text('Number of explored states')
  # ax3.plot(statesDf, color="orange")
  # ax3.axvline(x=learnRounds, color="black", linestyle=":", linewidth=3.0)
  #
  # alphas, epsilons, gammas = loadHyperparams()
  #
  # ax4.title.set_text('α hyperparameter - learning rate')
  # ax4.set_ylim(0-0.2, 1+0.2)
  # ax4.set_yticks([0.0, 1.0])
  # ax4.set_yticks([0.0, 0.1, 0.2, 0.3, 0.4, 0.5, 0.6, 0.7, 0.8, 0.9, 1.0], minor=True)
  # ax4.grid(which='major', alpha=1)
  # ax4.grid(which='minor', alpha=0.3)
  # ax4.plot(x_vals, alphas, color="red")
  # ax4.axvline(x=learnRounds, color="black", linestyle=":", linewidth=3.0)
  #
  # ax5.title.set_text('ε hyperparameter - experiment rate')
  # ax5.set_ylim(0-0.2, 1+0.2)
  # ax5.set_yticks([0.0, 1.0])
  # ax5.set_yticks([0.0, 0.1, 0.2, 0.3, 0.4, 0.5, 0.6, 0.7, 0.8, 0.9, 1.0], minor=True)
  # ax5.grid(which='major', alpha=1)
  # ax5.grid(which='minor', alpha=0.3)
  # ax5.plot(x_vals, epsilons, color="green")
  # ax5.axvline(x=learnRounds, color="black", linestyle=":", linewidth=3.0)
  #
  # ax6.title.set_text('γ hyperparameter - discount factor')
  # ax6.set_ylim(0-0.2, 1+0.2)
  # ax6.set_yticks([0.0, 1.0])
  # ax6.set_yticks([0.0, 0.1, 0.2, 0.3, 0.4, 0.5, 0.6, 0.7, 0.8, 0.9, 1.0], minor=True)
  # ax6.grid(which='major', alpha=1)
  # ax6.grid(which='minor', alpha=0.3)
  # ax6.plot(x_vals, gammas, color="blue")
  # ax6.axvline(x=learnRounds, color="black", linestyle=":", linewidth=3.0)

  return



makePlots()
plt.show()
