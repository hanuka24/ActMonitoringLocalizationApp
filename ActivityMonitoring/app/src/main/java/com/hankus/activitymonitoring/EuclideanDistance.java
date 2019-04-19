package com.hankus.activitymonitoring;

import android.icu.text.AlphabeticIndex;

//This class implements Metric interface and is used to calculate EuclideanDistance
public class EuclideanDistance{

    public double getDistance(Features s, Features e) {

        int numOfAttributes = s.numFeatures;
        double sum2 = 0;


        sum2 += Math.pow(s.min - e.min, 2);
        sum2 += Math.pow(s.max - e.max, 2);
        sum2 += Math.pow(s.index_max - e.index_max, 2);
        sum2 += Math.pow(s.mean - e.mean, 2);


        return Math.sqrt(sum2);
    }

}