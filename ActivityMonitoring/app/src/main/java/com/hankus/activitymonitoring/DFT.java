package com.hankus.activitymonitoring;
/*
 * Discrete Fourier transform based on the
 * Project by Nayuki, 2017. Public domain.
 * https://www.nayuki.io/page/how-to-implement-the-discrete-fourier-transform
 */


import java.util.ArrayList;

public class DFT {

    /*
     * Computes the discrete Fourier transform (DFT) of the given complex vector.
     * All the array arguments must be non-null and have the same length.
     */
    public static void computeDft(ArrayList<Double> inreal, ArrayList<Double> inimag, ArrayList<Double> outreal, ArrayList<Double> outimag) {
        int n = inreal.size();
        for (int k = 0; k < n; k++) {  // For each output element
            double sumreal = 0;
            double sumimag = 0;
            for (int t = 0; t < n; t++) {  // For each input element
                double angle = 2 * Math.PI * t * k / n;
                sumreal +=  inreal.get(t) * Math.cos(angle) + inimag.get(t) * Math.sin(angle);
                sumimag += -inreal.get(t) * Math.sin(angle) + inimag.get(t) * Math.cos(angle);
            }
            outreal.add(sumreal);
            outimag.add(Math.abs(sumimag));
        }
    }

}