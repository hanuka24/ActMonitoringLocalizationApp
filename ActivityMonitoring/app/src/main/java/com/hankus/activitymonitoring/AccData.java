package com.hankus.activitymonitoring;

import android.util.Log;
import android.view.animation.LinearInterpolator;

import java.util.ArrayList;
import java.util.Arrays;

public class AccData {
    ArrayList<AccDataSample> accData;

    private int NUM_SAMPLES = 120;

    public Features features;

    public AccData()
    {
        accData = new ArrayList<AccDataSample>();
        features = new Features(0,0,0,0, "unkown");
    }

    public int getSize()
    {
        return accData.size();
    }

    public void clear()
    {
        accData.clear();
    }

    public void addSample(AccDataSample sample)
    {
        accData.add(sample);
    }

    public void addSample(double x, double y, double z, long timestamp)
    {
        accData.add(new AccDataSample(x,y,z,timestamp));
    }

    public void extractFeatures()
    {
        double sum = 0;
        double sample;
        double min = 100;
        double max = 0;
        double index_max = 0;
        double mean;

        smoothData();

        linearizeData();

        for(int i = 0; i < accData.size(); i++) {

            sample = accData.get(i).getSum();

            sum += sample;

            if(sample < min)
                min = sample;

            if(sample > max)
            {
                max = sample;
                index_max = i;
            }

        }
        mean = sum/accData.size();

        features.mean = mean;
        features.max = max;
        features.index_max = index_max;
        features.min = min;
    }

    private void smoothData()
    {
        for(int i = 2; i < accData.size() - 2; i++) {

            double mean_x = (accData.get(i - 1).x + accData.get(i).x + accData.get(i+1).x) / 3.0;
            double mean_y = (accData.get(i - 1).y + accData.get(i).y + accData.get(i+1).y) / 3.0;
            double mean_z = (accData.get(i - 1).z + accData.get(i).z + accData.get(i+1).z) / 3.0;

            accData.set(i, new AccDataSample(mean_x, mean_y, mean_z, accData.get(i).timestamp));
        }
    }

    private AccDataSample getSampleBefore(long timestamp)
    {
        int i = 0;
        while(accData.get(i).timestamp < timestamp)
            i++;
        if(i == 0)
            return accData.get(0);

        return accData.get(i - 1);
    }

    private AccDataSample getSampleAfter(long timestamp)
    {
        int i = 0;
        while(accData.get(i).timestamp < timestamp)
            i++;
        if(i == accData.size())
            return accData.get(i - 1);

        return accData.get(i);
    }

    private AccDataSample interpolate(long time)
    {
        AccDataSample start = getSampleBefore(time);
        AccDataSample end = getSampleAfter(time);

        double x = start.x + (time - start.timestamp) * (end.x - start.x);
        double y = start.y + (time - start.timestamp) * (end.y - start.y);
        double z = start.z + (time - start.timestamp) * (end.z - start.z);

        return new AccDataSample(x,y,z, time);
    }

    private void linearizeData()
    {
        long start = accData.get(0).timestamp;
        long end = accData.get(accData.size() - 1).timestamp;
        long step_width = (end - start) / NUM_SAMPLES; //fixed length??

        //start from time = 0
        int count = 0;
        for (AccDataSample i: accData ) {
            i.timestamp = i.timestamp - start;
            accData.set(count, i);
            count++;
        }

        for(int i = 0; i < NUM_SAMPLES; i++)
            accData.set(i, interpolate(i*step_width));

    }

}
