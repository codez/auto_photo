package ch.codez.autophoto.jai;

import java.awt.Rectangle;
import java.awt.image.DataBufferInt;
import java.awt.image.Raster;
import java.awt.image.RenderedImage;
import java.awt.image.WritableRaster;

import javax.media.jai.ImageLayout;
import javax.media.jai.UntiledOpImage;

public class ConnectedComponentsOpImage extends UntiledOpImage {

    /**
     * Constructs ConnectedComponentsOpImage. Image dimensions are copied from
     * the source image. The tile grid layout, SampleModel, and ColorModel may
     * optionally be specified by an ImageLayout object
     * 
     * @param source a RenderedImage
     * @param layout an ImageLayout optionally containing the tile grid layout,
     *            SampleModel, and ColorModel or null.
     */
    public ConnectedComponentsOpImage(RenderedImage source, ImageLayout layout) {
        super(source, null, layout);
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
        int label = 1;
        int u, v, top = 0, left = 0;
        int eqTable[] = new int[width * height];
        DataBufferInt eqImage = new DataBufferInt(width * height);

        /* mark four-connected components in two passes */
        for (v = 0; v < height; v++) {
            for (u = 0; u < width; u++) {
                if (source.grey(u, v) == 0)
                    continue;

                boolean leftDefined = true;
                boolean topDefined = true;
                if (u == 0)
                    leftDefined = false;
                if (v == 0)
                    topDefined = false;
                boolean leftOn = false;
                boolean topOn = false;
                if (leftDefined)
                    left = eqImage.getElem(v * width + (u - 1));
                if (topDefined)
                    top = eqImage.getElem((v - 1) * width + u);

                if (topDefined && source.grey(u, v - 1) != 0)
                    topOn = true;
                if (leftDefined && source.grey(u - 1, v) != 0)
                    leftOn = true;

                if (topOn && !leftOn) {
                    eqImage.setElem(v * width + u, top);
                } else if (leftOn && !topOn) {
                    eqImage.setElem(v * width + u, left);
                } else if (topOn && leftOn && left == top) {
                    eqImage.setElem(v * width + u, top);
                } else if (topOn && leftOn && (left != top)) {
                    eqImage.setElem(v * width + u, top);
                    eqImage.setElem(v * width + u, fixEqTable(eqTable, label, top, left));
                } else {
                    eqImage.setElem(v * width + u, label);
                    label++;
                }
            }
        }

        for (v = 0; v < height; v++) {
            for (u = 0; u < width; u++) {
                if (eqImage.getElem(v * width + u) > 0) {
                    int temp_label = eqImage.getElem(v * width + u);
                    while (eqTable[temp_label] != 0) {
                        temp_label = eqTable[temp_label];
                    }
                    eqImage.setElem(v * width + u, temp_label);
                }
            }
        }
        int j = 1;
        for (int i = 1; i < label; i++) {
            if (eqTable[i] == 0) {
                eqTable[i] = j++;
            }
        }
        for (v = 0; v < height; v++) {
            for (u = 0; u < width; u++) {
                if (eqImage.getElem(v * width + u) > 0) {
                    dst.setSample(u, v, 0, eqTable[eqImage.getElem(v * width + u)]);
                } else {
                    dst.setSample(u, v, 0, 0);
                }
            }
        }
    }

    private int fixEqTable(int table[], int size, int label1, int label2) {
        while (true) {
            if (label1 == label2) {
                return label1;
            } else if (label1 > label2) {
                if (table[label1] == 0) {
                    table[label1] = label2;
                    return label2;
                } else {
                    label1 = table[label1];
                }
            } else {
                if (table[label2] == 0) {
                    table[label2] = label1;
                    return label1;
                } else {
                    label2 = table[label2];
                }
            }
        }
    }
}