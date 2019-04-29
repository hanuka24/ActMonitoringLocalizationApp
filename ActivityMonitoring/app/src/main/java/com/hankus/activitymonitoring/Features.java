package com.hankus.activitymonitoring;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;

public class Features implements Comparable< Features >{
    public double x_mean;
    public double y_mean;
    public double z_mean;
    public double min;
    public double max;
    public double frequency;
    public String activity;
    double distance;
    public int numFeatures;

    public Features(double x_mean, double y_mean, double z_mean, double min, double max, double frequency, String activity)
    {
        this.numFeatures = 4;
        this.x_mean = x_mean;
        this.y_mean = y_mean;
        this.z_mean = z_mean;
        this.max = max;
        this.min = min;
        this.frequency = frequency;
        this.activity = activity;
    }

    public double getDistance() {
        return distance;
    }

    public ArrayList<Double> getFeaturesArray()
    {
        return new ArrayList<Double>(Arrays.asList(x_mean, y_mean, z_mean, min, max, frequency));
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
