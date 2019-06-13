package com.hankus.activitymonitoring;

import android.graphics.Point;
import android.util.Log;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Random;

public class ParticleSet {
    private String tag = "ParticleSet";
    public int NUM_PARTICLES = 3500;

    public ArrayList<Particle> mParticles;
    public ArrayList<Line> mWalls;
    public ArrayList<Point> mFloor;
    public int mMaxX;
    public int mMaxY;
    public float mScaleMeter;
    private ParticleFilter mParticleFilter;

    ParticleSet()
    {
        mWalls = new ArrayList<Line>();
        mFloor = new ArrayList<Point>();
        mParticles = new ArrayList<Particle>();
        mParticleFilter = new ParticleFilter(this);
    }

    public void initParticles()
    {
        mParticles.clear();
        for (int i = 0; i < NUM_PARTICLES; i++) {
            addParticle(createRandomParticle());
        }
    }


    public Particle createRandomParticle()
    {
        Random r = new Random();
        Point p =  mFloor.get(r.nextInt(mFloor.size() - 1));
        return new Particle(p, 1.0f / NUM_PARTICLES);
    }


    public void addParticle(Particle particle)
    {
        //Log.wtf(tag, "Add Particle");
        mParticles.add(particle);
    }

    public void doParticleFilter(int stepwidth, float direction)
    {
        mParticleFilter.moveParticles(stepwidth, direction);
        mParticleFilter.updateWeight();
        mParticleFilter.resampling();
    }

    public void clear()
    {
        Log.wtf(tag, "Clear all particles");
        mParticles.clear();
    }
}
