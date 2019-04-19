package com.hankus.activitymonitoring;

public class AccDataSample
{
    public double x;
    public double y;
    public double z;

    public AccDataSample(double x, double y, double z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public double getSum()
    {
        return Math.sqrt(x*x + y*y + z*z);
    }
}
