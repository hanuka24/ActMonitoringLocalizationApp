package com.hankus.activitymonitoring;

import android.graphics.Point;
import android.util.Log;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Random;

public class ParticleSet {
    private String tag = "ParticleSet";
    public int NUM_PARTICLES = 1000;

    public ArrayList<Particle> mParticles;
    public ArrayList<Point> mWalls;
    public ArrayList<Point> mFloor;
    public int mMaxX;
    public int mMaxY;
    public int mMapHeight;
    public int mMapWidth;
    private ParticleFilter mParticleFilter;

    ParticleSet()
    {
        mParticles = new ArrayList<Particle>();
        mWalls = new ArrayList<Point>();
        mFloor = new ArrayList<Point>();
        mParticles = new ArrayList<Particle>();
        mParticleFilter = new ParticleFilter(this);
    }

    public void initParticles()
    {
        mParticleFilter.initParticles();
    }

    public void addParticle(Particle particle)
    {
        Log.wtf(tag, "Add Particle");
        mParticles.add(particle);
    }

    public void moveParticles(int stepwidth, float direction) //move particles and remove if they move on wall
    {
        mParticleFilter.moveParticles(stepwidth, direction);
    }

    private boolean outOfBound(int x, int y)
    {
        if(x < 0 || x >= mMaxX || y < 0 || y >= mMaxY)
            return true;

        return false;
    }

    public void removeParticle()
    {
        Log.wtf(tag, "Remove Particle");
        mParticles.remove(0);
    }

    public void clear()
    {
        Log.wtf(tag, "Clear all particles");
        mParticles.clear();
    }
}
