package com.hankus.activitymonitoring;

import android.graphics.Point;
import android.util.Log;

import java.util.ArrayList;
import java.util.Random;

public class ParticleFilter {

    private String tag = "ParticleFilter";

    private int num_particles;

    private ParticleSet mParticleSet;
    private MapView mMapView;

    public ParticleFilter(ParticleSet particleSet)
    {
        this.mParticleSet = particleSet;
    }

    public void initParticles()
    {
        Random r = new Random();
        clear();
        for (int i = 1; i <= mParticleSet.NUM_PARTICLES; i++) {
            Point p =  mParticleSet.mFloor.get(r.nextInt(mParticleSet.mFloor.size() - 1));
            mParticleSet.mParticles.add(new Particle(p.x, p.y, 0, 2));
        }
    }

    public void sense()
    {
        for (int i = 0; i < mParticleSet.mParticles.size(); i++)
        {
            Particle particle = mParticleSet.mParticles.get(i);
            if(wallCollision(particle.x, particle.y))
            {
                mParticleSet.mParticles.remove(i);
            }
            else
            {
                calculateProbability(i);
            }

        }
    }

    public void resampling()
    {

    }

    public void moveParticles(int stepwidth, float direction) //move particles and remove if they move on wall
    {
        for (int i = mParticleSet.mParticles.size() - 1; i >= 0; i--) {

            int x = mParticleSet.mParticles.get(i).x + (int)(stepwidth * Math.sin((double) direction));
            int y = mParticleSet.mParticles.get(i).y + (int)(stepwidth * Math.cos((double) direction));

            //if(mFloor.contains(new Point(x,y)) && !outOfBound(x,y))
            mParticleSet.mParticles.set(i, new Particle(x,y, mParticleSet.mParticles.get(i).orientation, mParticleSet.mParticles.get(i).weight));
//            else
//                mParticles.remove(i);
        }
    }

    public boolean wallCollision(int x, int y)
    {
        return false;
    }

    public void updateWeights(int i)
    {
        float sum = 0f;

        for (Particle particle : mParticleSet.mParticles) {
            sum += particle.probability;
        }

        mParticleSet.mParticles.get(i).weight = mParticleSet.mParticles.get(i).probability / sum;
    }

    public void calculateProbability(int i)
    {

    }

    public void killParticles()
    {

    }

    public void clear()
    {
        Log.wtf(tag, "Clear all particles");
        mParticleSet.mParticles.clear();
    }
}
