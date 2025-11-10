package Quantization;

import java.util.ArrayList;
import java.util.List;

public class Cluster {
    private Pixel centroid;
    private ArrayList<Pixel> points = new ArrayList<>();

    public Cluster( Pixel centroid ) {
        this.centroid = centroid;
    }

    public void addPoint( Pixel point ) {
        this.points.add( point );
    }

    public void clearPoints() {
        points.clear();
    }

    public void setCentroid( Pixel centroid ) {
        this.centroid = centroid;
    }

    public Pixel getCentroid() { return centroid; }
    public List<Pixel> getPoints() { return points; }
}
