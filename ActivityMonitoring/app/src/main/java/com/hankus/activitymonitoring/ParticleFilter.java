package com.hankus.activitymonitoring;

import android.graphics.Point;
import android.util.Log;

import java.util.ArrayList;
import java.util.Random;

public class ParticleFilter {
    private ParticleSet mParticleSet;

    public ParticleFilter(ParticleSet particleSet)
    {
        this.mParticleSet = particleSet;
    }

    public void initParticles()
    {
        Random r = new Random();
        mParticleSet.clear();
        for (int i = 1; i <= mParticleSet.NUM_PARTICLES; i++) {
            Point p =  mParticleSet.mFloor.get(r.nextInt(mParticleSet.mFloor.size() - 1));
            mParticleSet.mParticles.add(new Particle(p.x, p.y, 0, 2));
            mParticleSet.mOldParticles.add(new Particle(p.x, p.y, 0, 2));
        }
    }

    public void sense()
    {
        for (int i = mParticleSet.mParticles.size() - 1; i >= 0; i--)
        {
            Particle particle = mParticleSet.mParticles.get(i);
            if(wallCollision(particle))
            {
                mParticleSet.mParticles.remove(i);
            }
        }
    }

    public void resampling()
    {
        int num_new_particle = mParticleSet.NUM_PARTICLES - mParticleSet.mParticles.size();
        int num_old_particles = mParticleSet.mParticles.size();

        for(int i = 0; i < num_new_particle; i++) {
            Random r = new Random();
            Particle p = mParticleSet.mParticles.get(r.nextInt(num_old_particles - 1));
            p.x += r.nextInt(3);
            p.y += r.nextInt(3);
            mParticleSet.addParticle(new Particle(p.x, p.y, p.orientation, p.weight));
        }
    }

    public void moveParticles(int stepwidth, float direction) //move particles and remove if they move on wall
    {
        for (int i = mParticleSet.mParticles.size() - 1; i >= 0; i--) {

            int x = mParticleSet.mParticles.get(i).x + (int)(stepwidth * Math.sin((double) direction));
            int y = mParticleSet.mParticles.get(i).y + (int)(stepwidth * Math.cos((double) direction));

            mParticleSet.mParticles.set(i, new Particle(x,y, mParticleSet.mParticles.get(i).orientation, mParticleSet.mParticles.get(i).weight));
        }
    }

    public boolean wallCollision(Particle particle)
    {
        Point p = new Point(particle.x, particle.y);
        if(mParticleSet.mFloor.contains(p) && !outOfBound(particle.x, particle.y))
            return false;
        else
            return true;
    }

    private boolean crossedWall(Particle new_particle, Particle old_particle)
    {

//        for(Point point : mParticleSet.mWalls)
//        {
//            if(old_particle.x > new_particle.x)
//            {
//                if(point.x < old_particle.x && point.x > new_particle.x
//                    && point.y < old_particle.y && point.y > new_particle)
//                {
//                    return true;
//                }
//            }
//        }
        return false;
    }

    private boolean outOfBound(int x, int y)
    {
        if(x < 0 || x >= mParticleSet.mMaxX || y < 0 || y >= mParticleSet.mMaxY)
            return true;

        return false;
    }

    public void updateWeights(int i)
    {
        mParticleSet.mParticles.get(i).weight = 1 / mParticleSet.mParticles.size();
    }
}
