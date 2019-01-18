package kyle.game.besiege.voronoi.nodename.as3delaunay;

import kyle.game.besiege.geom.PointH;

public final class Circle extends Object {

    public PointH center;
    public double radius;

    public Circle(double centerX, double centerY, double radius) {
        super();
        this.center = new PointH(centerX, centerY);
        this.radius = radius;
    }

    public String toString() {
        return "Circle (center: " + center + "; radius: " + radius + ")";
    }
}