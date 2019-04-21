package com.hankus.activitymonitoring;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

public class KNN {
    // Find K nearest neighbors of testRecord within trainingSet
    static ArrayList<Features> findKNearestNeighbors(ArrayList<Features> trainingSet, Features testRecord, int K){
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
            i.distance = dist.getDistance(i, testRecord);
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
    static String classify(ArrayList<Features> neighbors){
        //construct a HashMap to store <classLabel, weight>
        HashMap<String, Double> map = new HashMap<String, Double>();
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
        }

        return returnLabel;
    }
}
