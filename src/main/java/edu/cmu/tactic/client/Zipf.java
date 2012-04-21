package edu.cmu.tactic.client;

import java.util.Random;

// From http://diveintodata.org/2009/09/13/zipf-distribution-generator-in-java/
public class Zipf {
    private Random rnd = new Random(System.currentTimeMillis());
    private int size;
    private double skew;
    private double bottom = 0;

    public Zipf(int size, double skew) {
        this.size = size;
        this.skew = skew;

        for (int i = 1; i < size; i++) {
            this.bottom += (1 / Math.pow(i, this.skew));
        }
    }
    
    public int getSize() {
		return size;
	}

    public void setSeed(long seed) {
        rnd = new Random(seed);
    }

    // the next() method returns an rank id. The frequency of returned rank ids
    // are follows Zipf distribution.
    public int nextInt() {
        int rank;
        double friquency = 0;
        double dice;

        rank = rnd.nextInt(size);
        friquency = (1.0d / Math.pow(rank, this.skew)) / this.bottom;
        dice = rnd.nextDouble();

        while (!(dice < friquency)) {
            rank = rnd.nextInt(size);
            friquency = (1.0d / Math.pow(rank, this.skew))
                    / this.bottom;
            dice = rnd.nextDouble();
        }

        return rank;
    }

    // This method returns a probability that the given rank occurs.
    public double getProbability(int rank) {
        return (1.0d / Math.pow(rank, this.skew)) / this.bottom;
    }
}