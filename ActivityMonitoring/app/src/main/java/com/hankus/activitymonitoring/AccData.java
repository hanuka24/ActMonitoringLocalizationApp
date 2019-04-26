package com.hankus.activitymonitoring;

import java.util.ArrayList;

public class AccData {
    ArrayList<AccDataSample> accData;
    public Features features;
    public DFT dft;

    public AccData()
    {
        accData = new ArrayList<AccDataSample>();
        features = new Features(0,0,0,0, 0, "unknown");
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
        double frequency = 0.0;
        double mean;
        ArrayList<Double> imaginary = new ArrayList<>();
        ArrayList<Double> real = new ArrayList<>();

        smoothData();

        for(int i = 0; i < accData.size(); i++) {

            sample = accData.get(i).getSum();
            imaginary.add(sample);
            real.add((double)i);

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
        frequency = getFrequency(imaginary, real);

        features.mean = mean;
        features.max = max;
        features.index_max = index_max;
        features.frequency = frequency;
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
            if(amount > max1)
            {
                max3 = max2;
                max2 = max1;
                max1 = amount;

                index3 = index2;
                index2 = index1;
                index1 = i;
            }
            else if(amount > max2)
            {
                max3 = max2;
                max2 = amount;

                index3 = index2;
                index2 = i;
            }
            else if(amount > max3)
            {
                max3 = amount;

                index3 = i;
            }
        }

        frequency = (out_real.get(index1) + out_real.get(index2) + out_real.get(index3)) / 3;

        return frequency;
    }
}
