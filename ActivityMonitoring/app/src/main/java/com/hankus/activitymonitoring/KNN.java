package com.hankus.activitymonitoring;

import android.util.Pair;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

//Based on: https://github.com/wihoho/KNN/tree/master/src

public class KNN {
    // Find K nearest neighbors of testRecord within trainingSet
    static ArrayList<Features> findKNearestNeighbors(ArrayList<Features> trainingSet, Features testRecord, int K){
        //check if trainingSet contains enough data to perform KNN
        if (trainingSet.size() <= K)
        {
            return null;
        }
        int NumOfTrainingSet = trainingSet.size();
        assert K <= NumOfTrainingSet : "K is larger than the length of trainingSet!";

        //Update KNN: take the case when testRecord has multiple neighbors with the same distance into consideration
        //Solution: Update the size of container holding the neighbors

        EuclideanDistance dist = new EuclideanDistance();

        for (Features i : trainingSet
             ) {
            i.distance = dist.getDistance(i.getFeaturesArray(), testRecord.getFeaturesArray());
        }

        Collections.sort(trainingSet);

        ArrayList<Features> neighbors = new ArrayList<Features>();

        for(int i = 0; i < K; i++)
        {
            neighbors.add(trainingSet.get(i));
        }

        return neighbors;
    }

    // Get the class label by using neighbors
    static Pair<String, HashMap<String, Double>> classify(ArrayList<Features> neighbors){
        //construct a HashMap to store <classLabel, weight>
        HashMap<String, Double> map = new HashMap<String, Double>();
        //construct a HashMap to store <classLabel, probability>
        HashMap<String, Double> probabilities = new HashMap<>();

        int num = neighbors.size();

        for(int index = 0;index < num; index ++){
            Features temp = neighbors.get(index);
            String key = temp.activity;

            //if this classLabel does not exist in the HashMap, put <key, 1/(temp.distance)> into the HashMap
            if(!map.containsKey(key))
               // map.put(key, 1);
                map.put(key, 1 / temp.distance);

                //else, update the HashMap by adding the weight associating with that key
            else{
                double value = map.get(key);
              //  value += 1;
                value += 1 / temp.distance;
                map.put(key, value);
            }
        }

        //Find the most likely label
        double maxSimilarity = 0;
        String returnLabel = "unkown";
        double sum = sumValues(map);

        Set<String> labelSet = map.keySet();
        Iterator<String> it = labelSet.iterator();

        //go through the HashMap by using keys
        //and find the key with the highest weights
        while(it.hasNext()){
            String label = it.next();
            double value = map.get(label);
            if(value > maxSimilarity){
                maxSimilarity = value;
                returnLabel = label;
            }
            //add the probabilities of each class
            probabilities.put(label, value / sum);
        }

        //return the predictec label and the probabilities of all classes
        Pair<String, HashMap<String, Double>> classification = new Pair<String, HashMap<String, Double>>(returnLabel, probabilities);

        return classification;
    }

    static double sumValues(HashMap<String, Double> map)
    {
        Set<String> labelSet = map.keySet();
        Iterator<String> it = labelSet.iterator();
        double sum = 0.0;

        while(it.hasNext()){
            String label = it.next();
            sum += map.get(label);
        }
        return sum;
    }
}
