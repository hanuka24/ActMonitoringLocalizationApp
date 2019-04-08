import matplotlib.pyplot as plt
import numpy as np
import pandas as pd
import scipy.signal as signal
import pandas as pd
import math
import numpy as np
import pickle
import matplotlib.pyplot as plt
from tabulate import tabulate
from array import *

from scipy import stats
import tensorflow as tf
import seaborn as sns
from pylab import rcParams
from sklearn import metrics
from sklearn.model_selection import train_test_split
import os

#inspired by: https://medium.com/@curiousily/human-activity-recognition-using-lstms-on-android-tensorflow-for-hackers-part-vi-492da5adef64


def main():

    NUM_SAMPLES = 5 #5 samples per frame
    WINDOW_WIDTH = 200 #ms per frame

    frames = []
    labels = list()

    for filename in os.listdir('./../Data/'):
        if filename.endswith(".txt"):
            print "Get Smaples of " + filename
            activity = filename.split('_')[0]
            columns = ['timestamp', 'x', 'y', 'z', 'sum']
            df = pd.read_csv('./../Data/' + filename, header=None, names=columns, delimiter=';')

            df['x'] = df['x'].astype('float64')
            df['y'] = df['y'].astype('float64')
            df['z'] = df['z'].astype('float64')

            # compute squared average of all 3 axes
            df = computeSum(df)
            print tabulate(df)

            # do signal processing
            linearize(df, WINDOW_WIDTH)
            smooth(df)

            # split into frames
            for i in range(0, len(df) - (len(df) % NUM_SAMPLES), NUM_SAMPLES):
                xs = df['x'].values[i: i + NUM_SAMPLES]
                ys = df['y'].values[i: i + NUM_SAMPLES]
                zs = df['z'].values[i: i + NUM_SAMPLES]
                sum = df['sum'].values[i: i + NUM_SAMPLES]
                frames.append([xs, ys, zs, sum])
                labels.append(activity)

            print np.array(frames).shape
            print len(labels)

            continue
        else:
            continue

    print tabulate(frames)
    print labels

    #Extract features

    #then we should have a set of labels with corresponding features which is feed into the knn algorithm

  #  file = open('./data/walking_0.txt', 'r')
  #  value = getData(file)
  #  print value
 #   plt.show()
  #  n = list(np.linspace(0, len(value), len(value)))

  #  plt.plot(value[0], value[1])
  #  plt.title("Activity Signal (Euclidean distance)")
   # plt.xlabel("sample number")
 #   plt.ylabel("Euclidean distance of x, y, z")
  #  plt.show()

def getData(file):
    value =  []
    for line in file:
        data = line.split(';')
        print data[0]
        value.append(data)
    return value

def computeSum(data):
    data['sum'] = np.sqrt(data['x'] ** 2 + data['y'] ** 2 + data['z'] ** 2)
    return data

def smooth(data):
    return data

def linearize(data, win_width):
    return data


if __name__ == '__main__':
    main()
