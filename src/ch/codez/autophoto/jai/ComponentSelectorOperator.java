package ch.codez.autophoto.jai;

import java.awt.Rectangle;
import java.awt.image.Raster;
import java.awt.image.RenderedImage;
import java.awt.image.WritableRaster;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.media.jai.ImageLayout;
import javax.media.jai.UntiledOpImage;

public class ComponentSelectorOperator extends UntiledOpImage {

    private ConnectedComponent[] components;

    private int minPercent;

    private int[] selectedLabels;

    public ComponentSelectorOperator(RenderedImage source, ImageLayout layout,
            ConnectedComponent[] components, int minPercent) {
        super(source, null, layout);
        this.components = components;
        this.minPercent = minPercent;
        this.initParams();
    }

    /**
     * Computes the connected components (4-connected).
     * 
     * @param src the source raster.
     * @param dst the resultant connected component image.
     * @param destRect the rectangle within the OpImage to be computed
     */
    protected void computeImage(Raster[] srcarr, WritableRaster dst, Rectangle destRect) {
        Raster src = srcarr[0];
        Raster2 source = new Raster2(src);
        int width = source.getWidth(), height = source.getHeight();

        /* Implement op here */
        for (int v = 0; v < height; v++) {
            for (int u = 0; u < width; u++) {
                int label = source.grey(u, v);
                if (Arrays.binarySearch(this.selectedLabels, label) > -1) {
                    dst.setSample(u, v, 0, 255);
                } else {
                    dst.setSample(u, v, 0, 0);
                }
            }
        }
    }

    private void initParams() {
        int maxArea = 0;
        for (ConnectedComponent c : components) {
            maxArea = Math.max(maxArea, c.area());
        }

        Rectangle rect = null;

        int minArea = maxArea * minPercent / 100;

        List<Integer> labels = new ArrayList<Integer>();
        for (ConnectedComponent c : components) {
            if (c.area() >= minArea) {
                labels.add(c.label());

                if (rect == null) {
                    rect = new Rectangle(c.boundingBox());
                } else {
                    Rectangle bounds = new Rectangle(rect);
                    Rectangle box = c.boundingBox();
                    rect.x = Math.min(bounds.x, box.x);
                    rect.y = Math.min(bounds.y, box.y);
                    rect.width = (int) Math.max(bounds.getMaxX(), box.getMaxX()) - rect.x;
                    rect.height = (int) Math.max(bounds.getMaxY(), box.getMaxY()) - rect.y;
                }
            }
        }

        this.selectedLabels = new int[labels.size()];
        int i = 0;
        for (int l : labels) {
            selectedLabels[i++] = l;
        }
        Arrays.sort(this.selectedLabels);

        setProperty("bounds", rect);
    }
}
