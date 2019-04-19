package com.hankus.activitymonitoring;

import java.util.ArrayList;

public class AccData {
    ArrayList<AccDataSample> accData;
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

    public void addSample(double x, double y, double z)
    {
        accData.add(new AccDataSample(x,y,z));
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

            accData.set(i, new AccDataSample(mean_x, mean_y, mean_z));
        }
    }
}
