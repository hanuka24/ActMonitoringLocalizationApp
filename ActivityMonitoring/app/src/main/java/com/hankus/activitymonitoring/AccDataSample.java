package com.hankus.activitymonitoring;

public class AccDataSample
{
    public double x;
    public double y;
    public double z;
    public long timestamp;

    public AccDataSample(double x, double y, double z, long timestamp) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.timestamp = timestamp;
    }

    public double getSum()
    {
        return Math.sqrt(x*x + y*y + z*z);
    }
}
