package com.hankus.activitymonitoring;

import android.graphics.Point;
import android.util.Log;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Random;

public class ParticleSet {
    private String tag = "ParticleSet";

    public int NUM_PARTICLES = 6000;

    public ArrayList<Particle> mParticles;
    public ArrayList<Line> mWalls;
    public ArrayList<Point> mFloor;

    public float mScaleMeterX;
    public float mScaleMeterY;
    public ParticleFilter mParticleFilter;

    public int posX;
    public int posY;

    ParticleSet()
    {
        mWalls = new ArrayList<Line>();
        mFloor = new ArrayList<Point>();
        mParticles = new ArrayList<Particle>();
        mParticleFilter = new ParticleFilter(this);
        posX = 0;
        posY = 0;
    }

    public void initParticles()
    {
        mParticles.clear();
        for (int i = 0; i < NUM_PARTICLES; i++) {
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
        return new Particle(p, 1.0f / NUM_PARTICLES);
    }


    /**
     * Creates a random particle close to a particle that has a higher weight than it's
     * initial weight.
     * @return
     */
    public Particle createRandomValidParticle()
    {
        Random r = new Random();
        int random_int = 0;
        do
        {
            random_int = r.nextInt(mParticles.size() - 1);
        } while(mParticles.get(random_int).getWeight() == (1 / NUM_PARTICLES));

        Particle p =  mParticles.get(random_int);

        return new Particle(p);
    }



    public void addParticle(Particle particle)
    {
        //Log.wtf(tag, "Add Particle");
        mParticles.add(particle);
    }

    /**
     * Perform particle filter with current stepwidth
     * @param stepwidth
     * @param direction
     */
    public void doParticleFilter(int stepwidth, float direction)
    {
        mParticleFilter.moveParticles(stepwidth, direction);
        mParticleFilter.sense();
        mParticleFilter.resampling();
        mParticleFilter.positioning();
    }

    public void clear()
    {
        Log.wtf(tag, "Clear all particles");
        mParticles.clear();
    }
}
