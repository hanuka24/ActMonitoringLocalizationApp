package com.hankus.activitymonitoring;

import android.graphics.Point;
import android.util.Log;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Random;

public class ParticleSet {

    private String tag = "ParticleSet";
    private int NUM_PARTICLES = 1000;

    public ArrayList<Particle> mParticles;
    public ArrayList<Point> mWalls;
    public ArrayList<Point> mFloor;
    public int mMaxX;
    public int mMaxY;

    ParticleSet()
    {
        mParticles = new ArrayList<Particle>();
        mWalls = new ArrayList<Point>();
        mFloor = new ArrayList<Point>();
        mParticles = new ArrayList<Particle>();
    }

    public void initParticles()
    {
        Random r = new Random();
        clear();
        for (int i = 1; i <= NUM_PARTICLES; i++) {
            Point p =  mFloor.get(r.nextInt(mFloor.size() - 1));
            mParticles.add(new Particle(p.x, p.y, 0, 2));
        }
    }


    public void addParticle(Particle particle)
    {
        Log.wtf(tag, "Add Particle");
        mParticles.add(particle);
    }

    public void moveParticles(int stepwidth, float direction) //move particles and remove if they move on wall
    {
        for (int i = mParticles.size() - 1; i >= 0; i--) {

            int x = mParticles.get(i).x + (int)(stepwidth * Math.sin((double) direction));
            int y = mParticles.get(i).y + (int)(stepwidth * Math.cos((double) direction));

            if(mFloor.contains(new Point(x,y)) && !outOfBound(x,y))
                mParticles.set(i, new Particle(x,y, mParticles.get(i).orientation, mParticles.get(i).weight));
            else
                mParticles.remove(i);
        }
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
