
import pandas as pd
import math
import numpy as np
import os
import operator
import random

# General structure inspired by: https://medium.com/@curiousily/human-activity-recognition-using-lstms-on-android-tensorflow-for-hackers-part-vi-492da5adef64
# knn inspired by: https://machinelearningmastery.com/tutorial-to-implement-k-nearest-neighbors-in-python-from-scratch/

def main():

    SPLIT = 0.8
    trainingSet = []
    testSet = []

    #features = getFeaturesFromRawData('./../Data/RawData', 60)
    features = getFeaturesFromFile('./../Data/Trainingsdata1.txt')

    accuracy_sum = 0

    #Split data into test- and trainings data 100 times and test accuracy.
    for x in range(0, 100, 1):
        for feat in features:
            if (random.random() < SPLIT):
                trainingSet.append(feat)
            else:
                testSet.append(feat)

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
        accuracy_sum = accuracy_sum + accuracy

        #clear trainingsSet and testSet
        trainingSet = []
        testSet = []
    print('=== TEST FINISHED ===')
    print('Overall accuracy: ' + repr(accuracy_sum/100) + '%')

def getFeaturesFromFile(file):
    columns = ['min', 'max', 'mean_x', 'mean_y', 'mean_z', 'fft', 'activity']
    df = pd.read_csv(file, header=None, names=columns, delimiter=';')

    features = []
    for index, row in df.iterrows():
        features.append([row['min'], row['max'], row['mean_x'], row['mean_y'], row['mean_z'], row['activity']])

    return features


def getFeaturesFromRawData(folder, NUM_SAMPLES):
    features = []
    for filename in os.listdir(folder):
        if filename.endswith(".txt"):
            #   print "get samples of " + filename
            activity = filename.split('_')
            if len(activity) == 2:
                activity = activity[0]
            else:
                activity = activity[0] + activity[1]

            columns = ['timestamp', 'x', 'y', 'z', 'sum']
            df = pd.read_csv(folder + '/' + filename, header=None, names=columns, delimiter=';')

            #
            df = df[:NUM_SAMPLES]

            # do signal processing
            df = computeSum(df)  # compute squared average of all 3 axes
            # linearize(df)
            smooth(df)

            features.append([df['sum'].min(), df['sum'].max(),
                             df['x'].mean(), df['y'].mean(), df['z'].mean(), activity])
            continue
        else:
            continue
    return features

def computeSum(data):
    data['sum'] = np.sqrt(data['x'] ** 2 + data['y'] ** 2 + data['z'] ** 2)
    return data

def smooth(data):
    for i in range (2, len(data) - 2, 1):
        data['x'].values[i] =  (data['x'].values[i-1] +  data['x'].values[i] +  data['x'].values[i+1])/3
        data['y'].values[i] =  (data['y'].values[i-1] +  data['y'].values[i] +  data['y'].values[i+1])/3
        data['z'].values[i] =  (data['z'].values[i-1] +  data['z'].values[i] +  data['z'].values[i+1])/3
        data['sum'].values[i] =  (data['sum'].values[i-1] +  data['sum'].values[i] +  data['sum'].values[i+1])/3
    return data

def getSampleBefore(data, timestamp):
    i = 0
    while(data['timestamp'].values[i] < timestamp and i < len(data)):
        i =  i + 1
    if(i == 0):
        return [data['sum'].values[0], data['timestamp'].values[0]]
    return [data['sum'].values[i - 1], data['timestamp'].values[i - 1]]

def getSampleAfter(data, timestamp):
    i = 0
    while(data['timestamp'].values[i] < timestamp and i < len(data)):
        i =  i + 1
    return [data['sum'].values[i],  data['timestamp'].values[i]]

def linearize(data):
    window_length = data['timestamp'].values[len(data) - 1] - data['timestamp'].values[0]
    step_length = window_length/len(data)

    starttime = data['timestamp'].values[0]

    print("windowlength (ms): " + str(window_length))
    print("step_length (ms): " + str(step_length))

    data['timestamp'] = data['timestamp'] - starttime

    print("New Timestamps:")
    print(data['timestamp'])

    for i in range(0, len(data), 1):
        time = i * step_length
        start = getSampleBefore(data, time)
        end = getSampleAfter(data, time)
        print("Interpolate " + str(time))
        print(start)
        print(end)
        if((end[1] - start[1]) != 0):
            data['sum'].values[i] = (start[0] + ((i * step_length - start[1]) * (end[0] - start[0]))/   (end[1] - start[1]))
        print data['sum'].values[i]
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

