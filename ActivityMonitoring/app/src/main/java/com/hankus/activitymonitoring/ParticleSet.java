package com.hankus.activitymonitoring;

import android.graphics.Point;
import android.util.Log;

import java.util.ArrayList;
import java.util.Random;

public class ParticleSet {
    private String mTag = "ParticleSet";

    public int mNumParticles = 6000;

    public ArrayList<Particle> mParticles;
    public ArrayList<Line> mWalls;
    public ArrayList<Point> mFloor;

    public float mScaleMeterX;
    public float mScaleMeterY;
    public ParticleFilter mParticleFilter;

    public int mPosX;
    public int mPosY;

    ParticleSet()
    {
        mWalls = new ArrayList<Line>();
        mFloor = new ArrayList<Point>();
        mParticles = new ArrayList<Particle>();
        mParticleFilter = new ParticleFilter(this);
        mPosX = 0;
        mPosY = 0;
    }

    public void initParticles()
    {
        mParticles.clear();
        for (int i = 0; i < mNumParticles; i++) {
            addParticle(createRandomParticle());
        }
    }


    /**
     * Creates a random particle on the floor.
     * @return
     */
    public Particle createRandomParticle()
    {
        Random r = new Random();
        Point p =  mFloor.get(r.nextInt(mFloor.size() - 1));
        return new Particle(p, 1.0f / mNumParticles);
    }


    /**
     * Creates a random particle close to a particle that has a higher weight than it's
     * initial weight.
     * @return
     */
    public Particle createRandomValidParticle()
    {
        Random r = new Random();
        Particle p;
        //select only particles, which have survived
        do
        {
            p = mParticles.get(r.nextInt(mParticles.size() - 1));
        } while(p.getWeight() == (1 / mNumParticles));

        return new Particle(p);
    }



    public void addParticle(Particle particle)
    {
        //Log.wtf(mTag, "Add Particle");
        mParticles.add(particle);
    }

    /**
     * Perform particle filter with current number of steps
     *
     * @param steps
     * @param direction
     */
    public void doParticleFilter(int steps, float direction)
    {
        mParticleFilter.moveParticles(steps, direction);
        mParticleFilter.sense();
        mParticleFilter.resampling();
        mParticleFilter.positioning();
    }

    public void clear()
    {
        Log.wtf(mTag, "Clear all particles");
        mParticles.clear();
    }
}
