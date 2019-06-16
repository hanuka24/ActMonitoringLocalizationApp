package com.hankus.activitymonitoring;

import java.util.ArrayList;

public class AccData {
    ArrayList<AccDataSample> mAccData;

    private int mNumSamples = 60;

    public Features mFeatures;
    public DFT mDft;

    public AccData()
    {
        mAccData = new ArrayList<AccDataSample>();
        mFeatures = new Features(0,0,0,0,0,0,"unknown");
    }

    public int getNumberOfSamples(){return mNumSamples;}

    public int getSize()
    {
        return mAccData.size();
    }

    public void clear()
    {
        mAccData.clear();
    }

    public void addSample(AccDataSample sample)
    {
        mAccData.add(sample);
    }

    public void addSample(double x, double y, double z, long timestamp)
    {
        mAccData.add(new AccDataSample(x,y,z,timestamp));
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

        for(int i = 0; i < mAccData.size(); i++) {

            sample = mAccData.get(i).getSum();
            imaginary.add(sample);
            real.add((double)i);

            sum_x += mAccData.get(i).mX;
            sum_y += mAccData.get(i).mY;
            sum_z += mAccData.get(i).mZ;

            if(sample < min)
                min = sample;

            if(sample > max)
                max = sample;
        }

        mean_x = sum_x/ mAccData.size();
        mean_y = sum_y/ mAccData.size();
        mean_z = sum_z/ mAccData.size();
        frequency = getFrequency(imaginary, real);

        mFeatures.mMeanX = mean_x;
        mFeatures.mMeanY = mean_y;
        mFeatures.mMeanZ = mean_z;
        mFeatures.mMax = max;
        mFeatures.mFrequency = frequency;
        mFeatures.mMin = min;
    }

    private void smoothData()
    {
        for(int i = 1; i < mAccData.size() - 1; i++) {

            double mean_x = (mAccData.get(i - 1).mX + mAccData.get(i).mX + mAccData.get(i+1).mX) / 3.0;
            double mean_y = (mAccData.get(i - 1).mY + mAccData.get(i).mY + mAccData.get(i+1).mY) / 3.0;
            double mean_z = (mAccData.get(i - 1).mZ + mAccData.get(i).mZ + mAccData.get(i+1).mZ) / 3.0;

            mAccData.set(i, new AccDataSample(mean_x, mean_y, mean_z, mAccData.get(i).mTimestamp));
        }
    }

    private AccDataSample getSampleBefore(long timestamp)
    {
        int i = 0;
        while(mAccData.get(i).mTimestamp < timestamp)
            i++;
        if(i == 0)
            return mAccData.get(0);

        return mAccData.get(i - 1);
    }

    private AccDataSample getSampleAfter(long timestamp)
    {
        int i = 0;
        while(mAccData.get(i).mTimestamp < timestamp)
            i++;
        if(i == mAccData.size())
            return mAccData.get(i - 1);

        return mAccData.get(i);
    }

    private AccDataSample interpolate(long time)
    {
        AccDataSample start = getSampleBefore(time);
        AccDataSample end = getSampleAfter(time);

        double x = start.mX + (time - start.mTimestamp) * (end.mX - start.mX);
        double y = start.mY + (time - start.mTimestamp) * (end.mY - start.mY);
        double z = start.mZ + (time - start.mTimestamp) * (end.mZ - start.mZ);

        return new AccDataSample(x,y,z, time);
    }

    private void linearizeData()
    {
        long start = mAccData.get(0).mTimestamp;
        long end = mAccData.get(mAccData.size() - 1).mTimestamp;
        long step_width = (end - start) / mNumSamples; //fixed length??

        //start from time = 0
        int count = 0;
        for (AccDataSample i: mAccData) {
            i.mTimestamp = i.mTimestamp - start;
            mAccData.set(count, i);
            count++;
        }

        for(int i = 0; i < mNumSamples; i++)
            mAccData.set(i, interpolate(i*step_width));

    }

    private double getFrequency(ArrayList<Double> imaginary, ArrayList<Double> real)
    {
        ArrayList<Double> out_real = new ArrayList<>();
        ArrayList<Double> out_imag = new ArrayList<>();
        mDft.computeDft(real, imaginary, out_imag, out_real);
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
