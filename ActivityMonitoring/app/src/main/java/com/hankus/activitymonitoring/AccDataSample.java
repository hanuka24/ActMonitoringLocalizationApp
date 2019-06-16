package com.hankus.activitymonitoring;

public class AccDataSample
{
    public double mX;
    public double mY;
    public double mZ;
    public long mTimestamp;

    public AccDataSample(double x, double y, double z, long timestamp) {
        this.mX = x;
        this.mY = y;
        this.mZ = z;
        this.mTimestamp = timestamp;
    }

    public double getSum()
    {
        return Math.sqrt(mX * mX + mY * mY + mZ * mZ);
    }
}
