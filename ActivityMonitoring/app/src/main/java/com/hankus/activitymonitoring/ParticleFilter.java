package com.hankus.activitymonitoring;

import android.graphics.Point;
import android.util.Log;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Random;

public class ParticleFilter {
    private String mTag = "ParticleFilter";
    private ParticleSet mParticleSet;

    public int mOrientationVariance = 20; //degrees
    public double mStepwidthVariance = 0.2; //m
    public double mStepwidth = 0.6; //m


    public ParticleFilter(ParticleSet particleSet)
    {
        this.mParticleSet = particleSet;
    }

    /**
     * Removes all particles that run into walls (weight = 0).
     */
    public void sense()
    {
        for (int i = mParticleSet.mParticles.size() - 1; i >= 0; i--)
        {
            if( mParticleSet.mParticles.get(i).getWeight() == 0)
            {
                removeParticle(i);
            }
        }
    }

    /**
     * Resampling replaces the amount of particles that ran into a wall.
     * A small chance is randomly placed on the floor.
     * The majority is randomly placed around particles that have survived at least one round (higher weight).
     */
    public void resampling()
    {
        if(! mParticleSet.mParticles.isEmpty())
        {
            int num_new_particle = mParticleSet.mNumParticles - mParticleSet.mParticles.size();
            updateWeight();

            for(int i = 0; i < num_new_particle; i++) {
                Random r = new Random();
                if(r.nextFloat() < 0.001f)
                    mParticleSet.addParticle(mParticleSet.createRandomParticle());
                else
                    mParticleSet.addParticle(mParticleSet.createRandomValidParticle());
            }
        }
    }

    /**
     * Increase weight of surviving particles
     */
    public void updateWeight()
    {
        float sum_weight = 0.0f;
        for(Particle p : mParticleSet.mParticles)
        {
            sum_weight += p.getWeight();
        }

        for(Particle p : mParticleSet.mParticles)
        {
            p.setWeight(p.getWeight()/sum_weight);
        }
    }

    /**
     * Particles are moved if they do not cross walls, if they do, their weight is set to 0.
     *
     * @param steps number of measured steps
     * @param direction the median orientation
     */
    public void moveParticles(int steps, float direction) //move particles and remove if they move on wall
    {
        for(Particle p : mParticleSet.mParticles)
        {
            double orientation_var = (new Random().nextInt(mOrientationVariance) * (new Random().nextBoolean() ? 1 : -1) * Math.PI / 180f);
            double stepwidth_var = (new Random().nextDouble() * mStepwidthVariance * (new Random().nextBoolean() ? 1 : -1));

            int x = p.getX() + (int)(steps * (mStepwidth + stepwidth_var)*mParticleSet.mScaleMeterX * Math.sin((double) direction + orientation_var));
            int y = p.getY() + (int)(steps * (mStepwidth + stepwidth_var)*mParticleSet.mScaleMeterY * Math.cos((double) direction + orientation_var));

            //set Weight to zero, if physical constraints are violated
            //if(!mParticleSet.mFloor.contains(p.getPosition())) //slow approach with walls as pixels
            if(crossedWall(new Point(x,y), p.getPosition()))
                p.setWeight(0.0f);
            else
                p.setPosition(x , y);

        }
        Log.wtf(mTag, "Number of Particles: " + mParticleSet.mParticles.size());
    }

    /**
     * Compute position by computing the median of mX/mY coordinates of all particles
     * with a higher weight than the initial weight.
     */
//TODO: count particles at same position, compute centroid of most dense particles or take highest
    public void positioning()
    {
        if(mParticleSet.mParticles.isEmpty())
            return;

        ArrayList<Integer> x = new ArrayList<>(mParticleSet.mNumParticles);
        ArrayList<Integer> y = new ArrayList<>(mParticleSet.mNumParticles);


        for (Particle p: mParticleSet.mParticles)
        {
            x.add(p.getX());
            y.add(p.getY());
        }

        Collections.sort(x);
        Collections.sort(y);

        mParticleSet.mPosX = x.get(mParticleSet.mNumParticles / 2);
        mParticleSet.mPosY = y.get(mParticleSet.mNumParticles / 2);

    }

    /**
     * Checks if a particle moves through a wall by drawing a line between the new position
     * and the old one and tries to find an intersection with the wall (line).
     *
     * @param new_point position the particle would move to next
     * @param old_point last position of particle
     * @return
     */
    private boolean crossedWall(Point new_point, Point old_point)
    {
        for (Line wall : mParticleSet.mWalls)
        {
            if (wall.intersectsWithLine(new_point, old_point))
                return true;
        }
        return false;
    }

    /**
     * Removes particles from the list.
     *
     * @param i index of particle
     */
    public void removeParticle(int i)
    {
        //Log.wtf(mTag, "Remove Particle");
        mParticleSet.mParticles.remove(i);
    }
}
