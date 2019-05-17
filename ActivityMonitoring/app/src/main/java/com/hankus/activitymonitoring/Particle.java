package com.hankus.activitymonitoring;

public class Particle {

    public int x;
    public int y;
    public float weight;
    // angle in degrees
    public float orientation;

    Particle(int x, int y, float orientation, float weight)
    {
        this.x = x;
        this.y = y;
        this.orientation = orientation;
        this.weight = weight;
    }

}
