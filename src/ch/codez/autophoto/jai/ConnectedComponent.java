package ch.codez.autophoto.jai;

import java.awt.Point;
import java.awt.Rectangle;

public class ConnectedComponent extends Object {

    /*
     * CONSTRUCTORS
     */

    /**
     * constructs a ConnectedComponent.
     * 
     * @param u the unique label for this connected component.
     */
    public ConnectedComponent(int u) {
        label = u;
        centroid = new Point(0, 0);
        boundingBox = new Rectangle(Integer.MAX_VALUE, Integer.MAX_VALUE, Integer.MIN_VALUE,
                Integer.MIN_VALUE);
    }

    /*
     * PUBLIC INTERFACE
     */

    /**
     * Finds connected components from an image containing uniquely labeled
     * regions (resulting from the ConnectedComponents op). A pixel value of 0
     * is assumed to be the background. Assumes 4-connectedness.
     * 
     * @param ccImage image containing uniquely labeled components.
     * @return an array of ConnectedComponents.
     */
    public static ConnectedComponent[] connectedComponents(Raster2 ccImage) {
        int numComponents = ccImage.maxGrey(), u, v;
        ConnectedComponent component[] = new ConnectedComponent[numComponents];
        for (u = 0; u < numComponents; u++) {
            component[u] = new ConnectedComponent(u + 1);
        }

        for (v = 0; v < ccImage.getHeight(); v++) {
            for (u = 0; u < ccImage.getWidth(); u++) {
                if (ccImage.grey(u, v) == 0)
                    continue;
                ConnectedComponent cc = component[ccImage.grey(u, v) - 1];
                if (cc == null)
                    System.out.println("cc null # " + (ccImage.grey(u, v) - 1));
                Point centroid = cc.centroid();
                int area = cc.area();
                Rectangle r = cc.boundingBox();
                centroid.x += u;
                centroid.y += v;
                area++;
                cc.setCentroid(centroid);
                cc.setArea(area);
                if (r.x > u) {
                    r.width = r.width + r.x - u;
                    r.x = u;
                }
                if (r.width < u - r.x + 1)
                    r.width = u - r.x + 1;
                if (r.y > v) {
                    r.height = r.height + r.y - v;
                    r.y = v;
                }
                if (r.height < v - r.y + 1)
                    r.height = v - r.y + 1;
                cc.setBoundingBox(r);
            }
        }
        for (u = 0; u < numComponents; u++) {
            ConnectedComponent cc = component[u];
            int area = cc.area();
            Point centroid = cc.centroid();
            centroid.x /= cc.area();
            centroid.y /= cc.area();
            cc.setCentroid(centroid);
        }

        return component;
    }

    /* Class Constants */

    /* Mutators */

    public void setCentroid(Point c) {
        centroid.x = c.x;
        centroid.y = c.y;
    }

    public void setBoundingBox(Rectangle r) {
        boundingBox.x = r.x;
        boundingBox.y = r.y;
        boundingBox.width = r.width;
        boundingBox.height = r.height;
    }

    public void setArea(int a) {
        area = a;
    }

    /* Accessors */

    public Point centroid() {
        return (Point) centroid.clone();
    }

    public Rectangle boundingBox() {
        return (Rectangle) boundingBox.clone();
    }

    public int area() {
        return area;
    }

    public int label() {
        return label;
    }

    /* Common Interface */

    public String toString() {
        return "Label: " + label + " Position: " + centroid.x + "," + centroid.y + " Area: " + area
                + " Bounding box: " + boundingBox.x + "," + boundingBox.y + "<->"
                + (boundingBox.x + boundingBox.width - 1) + ","
                + (boundingBox.y + boundingBox.height - 1) + " width: " + boundingBox.width
                + " height: " + boundingBox.height;
    }

    // public boolean equals(Object obj);
    // protected Object clone() throws CloneNotSupportedException;
    // protected void finalize() throws Throwable;

    /*
     * PRIVATE METHODS
     */

    /*
     * CLASS AND OBJECT ATTRIBUTES
     */

    private Point centroid;

    private Rectangle boundingBox;

    private int area;

    private Raster2 ccImage;

    private int label; // corresponding to the pixel label in ccImage

    /*
     * TEST METHODS
     */

    // public static void main(String arg[]);
}