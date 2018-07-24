package kyle.game.besiege.voronoi.nodename.as3delaunay;

import java.util.ArrayList;

import kyle.game.besiege.geom.PointH;

public final class Polygon {

    private ArrayList<PointH> _vertices;

    public Polygon(ArrayList<PointH> vertices) {
        _vertices = vertices;
    }

    public double area() {
        return Math.abs(signedDoubleArea() * 0.5);
    }

    public Winding winding() {
        double signedDoubleArea = signedDoubleArea();
        if (signedDoubleArea < 0) {
            return Winding.CLOCKWISE;
        }
        if (signedDoubleArea > 0) {
            return Winding.COUNTERCLOCKWISE;
        }
        return Winding.NONE;
    }

    private double signedDoubleArea() {
        int index, nextIndex;
        int n = _vertices.size();
        PointH pointH, next;
        double signedDoubleArea = 0;
        for (index = 0; index < n; ++index) {
            nextIndex = (index + 1) % n;
            pointH = _vertices.get(index);
            next = _vertices.get(nextIndex);
            signedDoubleArea += pointH.x * next.y - next.x * pointH.y;
        }
        return signedDoubleArea;
    }
}