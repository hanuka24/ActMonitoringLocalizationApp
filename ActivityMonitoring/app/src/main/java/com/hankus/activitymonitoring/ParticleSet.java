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
    private ParticleFilter mParticleFilter;
    private boolean mInit;

    ParticleSet()
    {
        mWalls = new ArrayList<Point>();
        mFloor = new ArrayList<Point>();
        mParticles = new ArrayList<Particle>();
        mParticleFilter = new ParticleFilter(this);
        mInit = true;
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

    public void doParticleFilter(int stepwidth, float direction)
    {
        if(mInit){
            mParticleFilter.initParticles();
            mParticleFilter.moveParticles(stepwidth, direction);
            mInit = false;
        }
        else
        {
            mParticleFilter.sense();
            mParticleFilter.resampling();
            mParticleFilter.moveParticles(stepwidth, direction);
        }


    }

    public void clear()
    {
        Log.wtf(tag, "Clear all particles");
        mParticles.clear();
    }
}
