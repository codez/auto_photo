/*
 * Created on 21.11.2007
 *
 */
package ch.codez.autophoto.jai;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.awt.image.RenderedImage;
import java.awt.image.renderable.ParameterBlock;

import javax.media.jai.Interpolation;
import javax.media.jai.JAI;
import javax.media.jai.ParameterBlockJAI;
import javax.media.jai.PlanarImage;
import javax.media.jai.registry.RenderedRegistryMode;

import org.apache.log4j.Logger;

import ch.codez.autophoto.AppOptions;

public class LayerBastler {

    private static Logger log = Logger.getLogger(LayerBastler.class);

    public BufferedImage compose(String snapshotFile) {
        long start = System.currentTimeMillis();
        PlanarImage snapshot = this.loadFile(snapshotFile);
        long end = System.currentTimeMillis();
        log.debug("loading image - " + (end - start));

        PlanarImage result = snapshot;

        if (AppOptions.getInstance().getMaskImage()) {
            start = end;
            BufferedImage circle = cutCircle(snapshot.getAsBufferedImage());
            end = System.currentTimeMillis();
            log.debug("cutting circle - " + (end - start));

            start = end;
            PlanarImage mask = getMask(circle);
            end = System.currentTimeMillis();
            log.debug("masking image - " + (end - start));

            start = end;
            PlanarImage componentMask = getConnectedComponents(mask);
            end = System.currentTimeMillis();
            log.debug("find components- " + (end - start));

            start = end;
            PlanarImage image = applyTransparencyMask(snapshot, componentMask);
            end = System.currentTimeMillis();
            log.debug("apply mask - " + (end - start));

            start = end;
            PlanarImage cropped = crop(image, componentMask);
            end = System.currentTimeMillis();
            log.debug("crop image - " + (end - start));

            result = cropped;
        }

        return result.getAsBufferedImage();
    }

    private PlanarImage loadFile(String filename) {
        PlanarImage image = JAI.create("fileload", filename);

        int maxWidth = AppOptions.getInstance().getMaxWidth();
        if (maxWidth > 0 && maxWidth < image.getWidth()) {
            float factor = maxWidth / (float) image.getWidth();
            ParameterBlockJAI pb = new ParameterBlockJAI("Scale", RenderedRegistryMode.MODE_NAME);
            pb.setSource("source0", image);
            pb.setParameter("xScale", factor);
            pb.setParameter("yScale", factor);
            pb.setParameter("interpolation",
                    Interpolation.getInstance(Interpolation.INTERP_BICUBIC));

            image = JAI.create("Scale", pb);
        }

        log.debug("loaded " + filename + " - " + image.getWidth() + "x" + image.getHeight());
        return image;
    }

    private PlanarImage getConnectedComponents(PlanarImage mask) {
        // find connected components
        ParameterBlockJAI pb = new ParameterBlockJAI("ConnectedComponents",
                RenderedRegistryMode.MODE_NAME);

        pb.setSource("source0", mask);

        PlanarImage labels = JAI.create("ConnectedComponents", pb, null);
        ConnectedComponent[] components = ConnectedComponent.connectedComponents(new Raster2(labels
                .getData()));

        // select largest components
        pb = new ParameterBlockJAI("ComponentsSelector", RenderedRegistryMode.MODE_NAME);
        pb.setSource("source0", labels);
        pb.setParameter("components", components);
        pb.setParameter("minPercent", AppOptions.getInstance().getMinComponentPercent());

        PlanarImage largest = JAI.create("ComponentsSelector", pb);

        // overlay masks
        pb = new ParameterBlockJAI("And", RenderedRegistryMode.MODE_NAME);
        pb.setSource("source0", largest);
        pb.setSource("source1", mask);

        PlanarImage newMask = JAI.create("And", pb);
        newMask.setProperty("bounds", largest.getProperty("bounds"));
        return newMask;
    }

    private PlanarImage getMask(RenderedImage snapshot) {
        AppOptions options = AppOptions.getInstance();

        ParameterBlockJAI pb = new ParameterBlockJAI("ColorEraser", RenderedRegistryMode.MODE_NAME);

        pb.setSource("source0", snapshot);

        pb.setParameter("color", options.getCamBackgroundColor());
        pb.setParameter("areaSize", options.getRenderArea());
        pb.setParameter("tolerance", options.getRenderTolerance());
        pb.setParameter("minPercentageForTransparency", options.getRenderOpaqueUpTo());
        pb.setParameter("maxPercentageForOpaquenes", options.getRenderTransparentFrom());

        return JAI.create("ColorEraser", pb, null);
    }

    private PlanarImage applyTransparencyMask(PlanarImage snapshot, RenderedImage mask) {
        ParameterBlockJAI pb = new ParameterBlockJAI("BandMerge", RenderedRegistryMode.MODE_NAME);

        pb.setSource("source0", snapshot);
        pb.setSource("source1", mask);

        return JAI.create("BandMerge", pb, null);
    }

    private PlanarImage crop(RenderedImage image, PlanarImage mask) {
        Rectangle bounds = (Rectangle) mask.getProperty("bounds");
        log.debug("Bounds " + bounds.toString());

        ParameterBlock pb = new ParameterBlock();
        pb.addSource(image);
        pb.add((float) bounds.x);
        pb.add((float) bounds.y);
        pb.add((float) bounds.width);
        pb.add((float) bounds.height);
        return JAI.create("crop", pb);
    }

    private int getCroppingIndex(int[] sums, boolean up) {
        int MIN = 510;
        int i = up ? 5 : sums.length - 6;
        int last = i;
        int start = i;
        while (i > 4 && i < sums.length - 5) {
            if (last != start && sums[last] < sums[i]) {
                return last;
            }
            last = start;
            if (sums[i] > MIN) {
                last = i;
            }
            i = up ? i + 1 : i - 1;
        }
        return i;
    }

    private BufferedImage cutCircle(BufferedImage image) {
        int w = image.getWidth();
        int h = image.getHeight();

        AppOptions options = AppOptions.getInstance();
        int percent = options.getCutRadiusPercent();

        int radius = Math.min(w, h) * percent / 100 / 2;
        int left = w / 2 - radius;
        int right = w / 2 + radius;
        int top = h / 2 - radius;
        int bottom = h / 2 + radius;

        Graphics2D g = image.createGraphics();
        int[] bg = options.getCamBackgroundColor();
        g.setColor(new Color(bg[0], bg[1], bg[2]));

        if (left > 0) {
            g.fillRect(0, 0, left, h);
        }
        if (right < w) {
            g.fillRect(right, 0, w - right, h);
        }
        if (top > 0) {
            g.fillRect(left, 0, right - left, top);
        }
        if (bottom < h) {
            g.fillRect(left, bottom, right - left, h - bottom);
        }

        // fill in the corners with pretty roundness

        // list of numbers, 0 through n - 1
        int n = 10;
        double[][] points = new double[n][2];

        // list of n numbers evenly distributed from 0 to 1.0 inclusive
        for (int i = 0; i < n; i++) {
            double v = i / (double) (n - 1);
            // list of n radians evenly distributed from 0 to pi/4 inclusive
            v = v * Math.PI * 2 / 4.0;
            // list of points evenly distributed around the circumference in the
            // first quadrant of a unit circle
            points[i][0] = radius * Math.cos(v);
            points[i][1] = radius * Math.sin(v);
        }

        int[][] quadrants = new int[][] { { 1, 1 }, { -1, 1 }, { -1, -1 }, { 1, -1 } };

        // we'll draw these points with trapezoids that connect to the
        // top or bottom rectangle and flip them around each quadrant
        for (int[] quad : quadrants) {
            int xFlip = quad[0];
            int yFlip = quad[1];
            int edge = bottom * yFlip;
            for (int i = 0; i < n - 1; i++) {
                int a0 = (int) (points[i][0] * xFlip + w / 2.0);
                int a1 = (int) (points[i][1] * yFlip + h / 2.0);
                int b0 = (int) (points[i + 1][0] * xFlip + w / 2.0);
                int b1 = (int) (points[i + 1][1] * yFlip + h / 2.0);

                g.fillPolygon(new int[] { a0, b0, b0, a0 }, new int[] { a1, b1, edge, edge }, 4);
            }
        }
        // image.flush();
        return image;
    }
}
