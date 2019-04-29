package com.hankus.activitymonitoring;

import android.util.Log;
import android.view.animation.LinearInterpolator;

import java.util.ArrayList;
import java.util.Arrays;

public class AccData {
    ArrayList<AccDataSample> accData;

    private int NUM_SAMPLES = 60;

    public Features features;
    public DFT dft;

    public AccData()
    {
        accData = new ArrayList<AccDataSample>();
        features = new Features(0,0,0,0,0,0,"unknown");
    }

    public int getNumberOfSamples(){return NUM_SAMPLES;}

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
        double sum_x = 0;
        double sum_y = 0;
        double sum_z = 0;
        double sample;
        double min = 100;
        double max = 0;
        double frequency = 0.0;
        double mean_x;
        double mean_y;
        double mean_z;
        ArrayList<Double> imaginary = new ArrayList<>();
        ArrayList<Double> real = new ArrayList<>();

        smoothData();

//        linearizeData();

        for(int i = 0; i < accData.size(); i++) {

            sample = accData.get(i).getSum();
            imaginary.add(sample);
            real.add((double)i);

            sum_x += accData.get(i).x;
            sum_y += accData.get(i).y;
            sum_z += accData.get(i).z;

            if(sample < min)
                min = sample;

            if(sample > max)
                max = sample;
        }

        mean_x = sum_x/accData.size();
        mean_y = sum_y/accData.size();
        mean_z = sum_z/accData.size();
        frequency = getFrequency(imaginary, real);

        features.x_mean = mean_x;
        features.y_mean = mean_y;
        features.z_mean = mean_z;
        features.max = max;
        features.frequency = frequency;
        features.min = min;
    }

    private void smoothData()
    {
        for(int i = 1; i < accData.size() - 1; i++) {

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

    private double getFrequency(ArrayList<Double> imaginary, ArrayList<Double> real)
    {
        ArrayList<Double> out_real = new ArrayList<>();
        ArrayList<Double> out_imag = new ArrayList<>();
        dft.computeDft(real, imaginary, out_imag, out_real);
        double max1 = 0.0;
        double max2 = 0.0;
        double max3 = 0.0;
        int index1 = 0;
        int index2 = 0;
        int index3 = 0;
        double frequency = 0.0;

        for (int i = 0; i < out_imag.size() - 1; i++) {
            double amount = out_imag.get(i);
            double f = out_real.get(i);
            if(amount > max1 && f < 700)
            {
                max3 = max2;
                max2 = max1;
                max1 = amount;

                index3 = index2;
                index2 = index1;
                index1 = i;
            }
            else if(amount > max2 && f < 700)
            {
                max3 = max2;
                max2 = amount;

                index3 = index2;
                index2 = i;
            }
            else if(amount > max3 && f < 700)
            {
                max3 = amount;

                index3 = i;
            }
        }

        frequency = (out_real.get(index1) + out_real.get(index2) + out_real.get(index3)) / 3;

        return frequency;
    }
}
