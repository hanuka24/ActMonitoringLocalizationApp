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
import statistics
from scipy import stats
import tensorflow as tf
import seaborn as sns
from pylab import rcParams
from sklearn import metrics
from sklearn.model_selection import train_test_split
import os
import operator
import random
#inspired by: https://medium.com/@curiousily/human-activity-recognition-using-lstms-on-android-tensorflow-for-hackers-part-vi-492da5adef64


def main():

    NUM_SAMPLES = 70 #50 samples per frame
    WINDOW_WIDTH = 20 #ms per frame
    SPLIT = 0.8

    trainingSet = []
    testSet = []

    for filename in os.listdir('./../Data/'):
        if filename.endswith(".txt") and filename != "Trainingsdata.txt":
            print "Get Samples of " + filename
            activity = filename.split('_')
            if len(activity) == 2:
                activity = activity[0]
            else:
                activity = activity[0] + activity[1]

            columns = ['timestamp', 'x', 'y', 'z', 'sum']
            df = pd.read_csv('./../Data/' + filename, header=None, names=columns, delimiter=';')

            df['x'] = df['x'].astype('float64')
            df['y'] = df['y'].astype('float64')
            df['z'] = df['z'].astype('float64')

            #            print tabulate(df)

            plt.plot(df['timestamp'], df['x'], color='0.75')
            plt.plot(df['timestamp'], df['y'], color='0.75')
            plt.plot(df['timestamp'], df['z'], color='0.75')

            # do signal processing
            linearize(df)
            smooth(df)
            df = computeSum(df)  # compute squared average of all 3 axes

            plt.plot(df['timestamp'], df['x'], 'r')
            plt.plot(df['timestamp'], df['y'], 'b')
            plt.plot(df['timestamp'], df['z'], 'g')
            plt.plot(df['timestamp'], df['sum'], 'y')
            plt.title(activity)
            #  plt.show()

            # Extract features

            list_mean = [statistics.mean(df['x']), statistics.mean(df['y']), statistics.mean(df['z']),
                         statistics.mean(df['sum'])]
            list_max = []
            list_min = [min(df['x']), min(df['y']), min(df['z']), min(df['sum'])]
            list_diff = [min(df['x']) - max(df['x']), min(df['y']) - max(df['y']), min(df['z']) - max(df['z']),
                         min(df['sum']) - max(df['sum']), ]
            # print(df['sum'].min().index)
            df[['sum']].idxmin()
            print(activity)
            # wuant = df.quantile(.8)
            # print(wuant)
            print(df['sum'].mean())
            #  print(df.mean())
            #  print(df.max())
            #  print(df.min())

            if (random.random() < SPLIT):
                trainingSet.append([df['sum'].mean(), df['sum'].min(), df['sum'].max(), df['sum'].quantile(0.8),
                                    df['sum'].quantile(0.15), df[['sum']].idxmax(), activity])
            #  trainingSet.append([df['sum'].mean(), df['sum'].min(), df['sum'].max(), df[['sum']].idxmax(), activity])
            else:
                # testSet.append([df['sum'].mean(), df['sum'].min(), df['sum'].max(),df[['sum']].idxmax(), activity])
                testSet.append([df['sum'].mean(), df['sum'].min(), df['sum'].max(), df['sum'].quantile(0.8),
                                df['sum'].quantile(0.15), df[['sum']].idxmax(), activity])

            continue
        else:
            continue

    print 'Train set: ' + repr(len(trainingSet))
    print 'Test set: ' + repr(len(testSet))
    # generate predictions
    predictions = []
    k = 3
    for x in range(len(testSet)):
        neighbors = getNeighbors(trainingSet, testSet[x], k)
        result = getResponse(neighbors)
        predictions.append(result)
        print('> predicted=' + repr(result) + ', actual=' + repr(testSet[x][-1]))
    accuracy = getAccuracy(testSet, predictions)
    print('Accuracy: ' + repr(accuracy) + '%')

    #then we should have a set of labels with corresponding features which is feed into the knn algorithm

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
    for i in range (3, len(data) - 3, 1):
        data['x'].values[i] =  (data['x'].values[i-1] +  data['x'].values[i] +  data['x'].values[i+1])/3
        data['y'].values[i] =  (data['y'].values[i-1] +  data['y'].values[i] +  data['y'].values[i+1])/3
        data['z'].values[i] =  (data['z'].values[i-1] +  data['z'].values[i] +  data['z'].values[i+1])/3
        data['sum'].values[i] =  (data['sum'].values[i-1] +  data['sum'].values[i] +  data['sum'].values[i+1])/3

    return data

def linearize(data):
   # window_length = data['timestamp'].values[69] - data['timestamp'].values[0]
   # step_length = window_length/70

   # print("windowlength (ms): " + str(window_length))
   # print("step_length (ms): " + str(step_length))

    return data

def euclideanDistance(instance1, instance2, length):
	distance = 0
	for x in range(length):
		distance += pow((instance1[x] - instance2[x]), 2)
	return math.sqrt(distance)

def getNeighbors(trainingSet, testInstance, k):
	distances = []
	length = len(testInstance)-1
	for x in range(len(trainingSet)):
		dist = euclideanDistance(testInstance, trainingSet[x], length)
		distances.append((trainingSet[x], dist))
	distances.sort(key=operator.itemgetter(1))
	neighbors = []
	for x in range(k):
		neighbors.append(distances[x][0])
	return neighbors

def getResponse(neighbors):
	classVotes = {}
	for x in range(len(neighbors)):
		response = neighbors[x][-1]
		if response in classVotes:
			classVotes[response] += 1
		else:
			classVotes[response] = 1
	sortedVotes = sorted(classVotes.iteritems(), key=operator.itemgetter(1), reverse=True)
	return sortedVotes[0][0]

def getAccuracy(testSet, predictions):
	correct = 0
	for x in range(len(testSet)):
		if testSet[x][-1] == predictions[x]:
			correct += 1
	return (correct/float(len(testSet))) * 100.0

if __name__ == '__main__':
    main()
