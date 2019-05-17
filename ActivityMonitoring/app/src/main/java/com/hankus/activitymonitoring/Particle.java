package com.hankus.activitymonitoring;

public class Particle {
    private float x;
    private float y;
    private float weight;
    // angle in degrees
    private float orientation;

    Particle(float x, float y, float orientation)
    {
        this.x = x;
        this.y = y;
        this.orientation = orientation;
    }
}
