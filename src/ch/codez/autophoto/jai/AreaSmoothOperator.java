/*
 * Created on 21.11.2007
 *
 */
package ch.codez.autophoto.jai;

import java.awt.image.RenderedImage;
import java.util.Map;

import javax.media.jai.ImageLayout;

public class AreaSmoothOperator extends PixelAreaOperator {

    private int disregardBackground;

    private double ratioFactor;

    private int areaSize;

    private int[] lineRatios;

    private int lineIndex = 0;

    public AreaSmoothOperator(RenderedImage source, ImageLayout layout, Map configuration,
            int[] color, int bounds, int disregardBackground, int completeBackground) {
        super(source, layout, configuration, color, bounds);

        this.areaSize = 1 + bounds * 2;
        int areaTotal = (this.areaSize * this.areaSize) * 255;

        this.ratioFactor = 25500.0 / (double) ((completeBackground - disregardBackground) * areaTotal);
        this.disregardBackground = disregardBackground * areaTotal / 100;

        this.lineRatios = new int[this.areaSize];
    }

    public void nextLine() {
        this.lineIndex = 0;
    }

    public byte doSomethingNasty(byte[][] srcDataArrays, int[] pixelOffsets, int numBands) {
        this.initLineRatios(srcDataArrays, pixelOffsets);
        lineRatios[this.lineIndex % this.areaSize] = this.getLineRatio(srcDataArrays, pixelOffsets,
                this.areaSize - 1);

        this.lineIndex++;
        return this.computeRatio();
    }

    private void initLineRatios(byte[][] srcDataArrays, int[] pixelOffsets) {
        if (this.lineIndex == 0) {
            for (int i = 0; i < this.areaSize - 1; i++) {
                lineRatios[i + 1] = this.getLineRatio(srcDataArrays, pixelOffsets, i);
            }
        }
    }

    private int getLineRatio(byte[][] srcDataArrays, int[] pixelOffsets, int line) {
        int areaOffset = pixelOffsets[0] + line;
        int count = 0;
        for (int u = 0; u < this.areaSize; u++) {
            count += srcDataArrays[0][areaOffset] & 0xff;
            areaOffset += scanlineStride;
        }
        return count;
    }

    private byte computeRatio() {
        int sum = 0;
        for (int i = 0; i < this.areaSize; i++) {
            sum += lineRatios[i];
        }
        return this.clampRatio(sum);
    }

    private byte clampRatio(int ratio) {
        ratio = (int) ((ratio - disregardBackground) * ratioFactor);
        // ratio = 255 - ratio;
        if (ratio < 0) {
            ratio = 0;
        } else if (ratio > 255) {
            ratio = 255;
        }
        return (byte) ratio;
    }

}
