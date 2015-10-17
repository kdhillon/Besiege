/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package kyle.game.besiege.geom;

/**
 * Point.java Function Date Jun 13, 2013
 *
 * @author Connor
 */
public class PointH {

   public static double distance(PointH _coord, PointH _coord0) {
        return Math.sqrt((_coord.x - _coord0.x) * (_coord.x - _coord0.x) + (_coord.y - _coord0.y) * (_coord.y - _coord0.y));
    }
    public float x, y;

    // this used to be double but I changed it
    // for drawing meshes
    public PointH(float x, float y) {
        this.x = x;
        this.y = y;
    }
    
    public PointH(double x, double y) {
        this.x = (float) x;
        this.y = (float) y;
    }
    
    public PointH() {
        this.x = 0;
        this.y = 0;
    }
    
    @Override
    public String toString(){
        return x + ", " + y;
    }

    public double l2() {
        return x * x + y * y;
    }

    public double length() {
        return Math.sqrt(x * x + y * y);
    }
    
    public PointH midpoint(PointH that) {
    	return new PointH((that.x + this.x) / 2, (that.y + this.y) / 2);
    }
}
