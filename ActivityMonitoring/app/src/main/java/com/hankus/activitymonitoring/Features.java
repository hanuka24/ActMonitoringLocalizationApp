package com.hankus.activitymonitoring;

public class Features implements Comparable< Features >{
    public double mean;
    public double min;
    public double max;
    public double index_max;
    public String activity;
    double distance;
    public int numFeatures;

    public Features(double mean, double min, double max, double index_max, String activity)
    {
        this.numFeatures = 4;
        this.mean = mean;
        this.max = max;
        this.min = min;
        this.index_max = index_max;
        this.activity = activity;
    }

    public double getDistance() {
        return distance;
    }

    @Override
    public int compareTo(Features o) {
        return new Double(distance).compareTo( o.distance);
    }
    @Override
    public String toString() {
        return String.valueOf(distance);
    }
}
