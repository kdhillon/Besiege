/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package test;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import javax.swing.JFrame;
import javax.swing.WindowConstants;

import kyle.game.besiege.geom.PointH;
import kyle.game.besiege.geom.Rectangle;
import kyle.game.besiege.utils.MyRandom;
import kyle.game.besiege.voronoi.VoronoiGraph;
import kyle.game.besiege.voronoi.nodename.as3delaunay.Voronoi;

/**
 * TestDriver.java Function Date Jun 14, 2013
 *
 * @author Connor
 */
public class TestDriver {

    public static void main(String[] args) {
        final int width = 1000;
        final int height = 1000;
        final int numSites = 8000;
        final ArrayList<PointH> pointHs = new ArrayList();
        final long seed = System.nanoTime();
        final MyRandom r = new MyRandom(seed);
        System.out.println("seed: " + seed);

        //let's create a bunch of random points
        for (int i = 0; i < numSites; i++) {
            pointHs.add(new PointH(r.nextDouble(0, width), r.nextDouble(0, height)));
        }
        
        //now make the intial underlying voronoi structure
        final Voronoi v = new Voronoi(pointHs, null, new Rectangle(0, 0, width, height));
        
        //assemble the voronoi strucutre into a usable graph object representing a map
        final VoronoiGraph graph = new VoronoiGraph(v, 2, r);

        final BufferedImage img = new BufferedImage(width, height, BufferedImage.TYPE_4BYTE_ABGR);
        Graphics2D g = img.createGraphics();
        g.setColor(Color.BLACK);
        g.fillRect(0, 0, width, height);

//        graph.paint(g);

        final JFrame frame = new JFrame() {
            @Override
            public void paint(Graphics g) {
                g.drawImage(img, 25, 35, null);
            }
        };
        frame.setTitle("java fortune");
        frame.setVisible(true);
        frame.setSize(width + 50, height + 50);
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
    }
}
