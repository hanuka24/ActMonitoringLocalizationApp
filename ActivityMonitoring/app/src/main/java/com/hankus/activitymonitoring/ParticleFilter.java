package com.hankus.activitymonitoring;

import java.util.ArrayList;
import java.util.Random;

public class ParticleFilter {

    private ArrayList<Particle> particles = new ArrayList<>();
    private int particle_count = 100;
    private int height;
    private int width;

    public ParticleFilter(int height, int width)
    {
        this.height = height;
        this.width = width;
    }

    public void initialBelief()
    {
        for(int i = 0; i <  particle_count; i++)
        {
            int x = new Random().nextInt(width + 1);
            int y = new Random().nextInt(height + 1);
            Particle particle = new Particle(x, y, 0, 0);

            particles.add(particle);
        }
    }

    public void sense()
    {
        for (int i = 0; i < particles.size(); i++)
        {
            Particle particle = particles.get(i);
            if(wallCollision(particle.x, particle.y))
            {
                particles.remove(i);
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

    public void move()
    {

    }

    public boolean wallCollision(int x, int y)
    {
        return false;
    }

    public void updateWeights(int i)
    {
        float sum = 0f;

        for (Particle particle : particles) {
            sum += particle.probability;
        }

        particles.get(i).weight = particles.get(i).probability / sum;
    }

    public void calculateProbability(int i)
    {

    }

    public void killParticles()
    {

    }
}
