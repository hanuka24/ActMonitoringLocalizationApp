package com.hankus.activitymonitoring;

import java.util.ArrayList;
import java.util.Arrays;

public class Features implements Comparable< Features >{
    public double mMeanX;
    public double mMeanY;
    public double mMeanZ;
    public double mMin;
    public double mMax;
    public double mFrequency;
    public String mActivity;
    double mDistance;
    public int mNumFeatures;

    public Features(double x_mean, double y_mean, double z_mean, double min, double max, double frequency, String activity)
    {
        this.mNumFeatures = 4;
        this.mMeanX = x_mean;
        this.mMeanY = y_mean;
        this.mMeanZ = z_mean;
        this.mMax = max;
        this.mMin = min;
        this.mFrequency = frequency;
        this.mActivity = activity;
    }

    public double getmDistance() {
        return mDistance;
    }

    public ArrayList<Double> getFeaturesArray()
    {
        return new ArrayList<Double>(Arrays.asList(mMeanX, mMeanY, mMeanZ, mMin, mMax));
    }

    @Override
    public int compareTo(Features o) {
        return new Double(mDistance).compareTo( o.mDistance);
    }

    @Override
    public String toString() {
        return String.valueOf(mDistance);
    }
}
