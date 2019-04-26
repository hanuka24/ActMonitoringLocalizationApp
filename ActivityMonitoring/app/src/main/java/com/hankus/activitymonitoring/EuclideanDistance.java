package com.hankus.activitymonitoring;

//Based on: https://github.com/wihoho/KNN/tree/master/src

import java.util.ArrayList;

public class EuclideanDistance{

    public double getDistance(ArrayList<Double> s, ArrayList<Double> e) {

        int numOfAttributes = s.size();
        double sum2 = 0;

        for(int i = 0; i < numOfAttributes; i++)
            sum2 += Math.pow(s.get(i) - e.get(i), 2);

        return Math.sqrt(sum2);
    }

}