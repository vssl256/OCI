package Quantization;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class KMeans {
    private ArrayList<Pixel> allPixels;
    private ArrayList<Cluster> clusters = new ArrayList<>();
    private int k = 16;

    public KMeans( ArrayList<Pixel> allPixels ) {
        this.allPixels = allPixels;
    }

    public List<Pixel> run( int maxIterations ) {
        List<Pixel> palette = new ArrayList<>();
        //Random rand = new Random(  );
        //for ( int i = 0; i < k; i ++ ) {
        //    int randPixel = rand.nextInt(0, allPixels.size() );
        //    Cluster cluster = new Cluster( allPixels.get( randPixel ) );
        //    clusters.add( cluster );
        //}
        evenDist();

        for ( int i = 0; i < maxIterations; i++ ) {
            assignPoints();
            boolean changed = updateCentroids();
            if ( !changed ) {
                System.out.println("Done at i = " + i);
                break;
            }
        }
        for ( Cluster cluster : clusters ) {
            Pixel c = cluster.getCentroid();
            palette.add( c );
        }
        return palette;
    }

    private void evenDist() {
        clusters.clear();

        double minL = Double.MAX_VALUE, maxL = Double.MIN_VALUE;
        double minA = Double.MAX_VALUE, maxA = Double.MIN_VALUE;
        double minB = Double.MAX_VALUE, maxB = Double.MIN_VALUE;

        for ( Pixel p : allPixels ) {
            minL = Math.min(minL, p.L); maxL = Math.max(maxL, p.L);
            minA = Math.min(minA, p.A); maxA = Math.max(maxA, p.A);
            minB = Math.min(minB, p.B); maxB = Math.max(maxB, p.B);
        }

        for ( int i = 0; i < k; i++ ) {
            double L = minL + i * (maxL - minL) / (k - 1);
            double A = minA + i * (maxA - minA) / (k - 1);
            double B = minB + i * (maxB - minB) / (k - 1);

            Pixel centroid = Pixel.fromLAB(L, A, B);
            clusters.add(new Cluster(centroid));
        }
    }

    private void assignPoints() {
        for ( Cluster cluster : clusters ) {
            if ( cluster.getPoints() == null ) continue;
            cluster.clearPoints();
        }

        for ( Pixel pixel : allPixels ) {
            Cluster closestCluster = null;
            double minDist = Double.MAX_VALUE;
            for ( Cluster cluster : clusters ) {
                double dist = Pixel.distance( pixel, cluster.getCentroid() );
                if ( dist < minDist ) {
                    minDist = dist;
                    closestCluster = cluster;
                }
            }
            closestCluster.addPoint( pixel );
        }
    }
    Double prevPrevSum = null;
    Double prevSum = null;

    public boolean updateCentroids() {
        boolean changed = false;
        int i = 1;
        double sum = 0.0;
        for ( Cluster cluster : clusters ) {
            double sumL = 0, sumA = 0, sumB = 0;
            for ( Pixel pixel : cluster.getPoints() ) {
                sumL += pixel.L;
                sumA += pixel.A;
                sumB += pixel.B;
            }

            int n = cluster.getPoints().size();
            if ( n == 0 ) continue;

            double newL = sumL / n;
            double newA = sumA / n;
            double newB = sumB / n;

            Pixel centroid = cluster.getCentroid();
            double threshold = 0.2;
            double dist = Math.sqrt(
                    Math.pow( centroid.L - newL, 2 ) +
                    Math.pow( centroid.A - newA, 2 ) +
                    Math.pow( centroid.B - newB, 2 )
            );

            if ( dist > threshold ) {
                centroid = Pixel.fromLAB( newL, newA, newB );
                cluster.setCentroid( centroid );
                changed = true;
            }

            sum += dist;
            i++;
        }
        sum /= clusters.size();

        if ( prevSum != null && prevPrevSum != null ) {
            if ( Math.abs( prevPrevSum - sum ) < 0.0001 ) {
                changed = false;
                System.out.printf("Accuracy = %.2f ", sum );
            }
        }

        prevPrevSum = prevSum;
        prevSum = sum;

        return changed;
    }
}
