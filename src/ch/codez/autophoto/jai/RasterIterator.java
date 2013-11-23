/*
 * Created on 21.11.2007
 *
 */
package ch.codez.autophoto.jai;

import javax.media.jai.RasterAccessor;

import org.apache.log4j.Logger;

public class RasterIterator {

    private static Logger log = Logger.getLogger(RasterIterator.class);

    // array indexes
    private static final int SRC = 0;

    private static final int DST = 1;

    // Raster accessors
    private RasterAccessor src;

    private RasterAccessor dst;

    private int[] pixelStrides;

    private int[] scanlineStrides;

    private int[] numBands = new int[2];

    private int[] colsSum;

    private int[] rowsSum;

    public RasterIterator(RasterAccessor src, RasterAccessor dst) {
        this.src = src;
        this.dst = dst;

        this.numBands = new int[] { src.getNumBands(), dst.getNumBands() };
        this.pixelStrides = new int[] { src.getPixelStride(), dst.getPixelStride() };
        this.scanlineStrides = new int[] { src.getScanlineStride(), dst.getScanlineStride() };
    }

    public void iterate(PixelAreaOperator transformer) {
        int dwidth = dst.getWidth();
        int dheight = dst.getHeight();
        rowsSum = new int[dheight];
        colsSum = new int[dwidth];

        byte srcDataArrays[][] = src.getByteDataArrays();
        byte dstDataArrays[][] = dst.getByteDataArrays();

        int bandOffsets[][] = new int[][] { src.getBandOffsets(), dst.getBandOffsets() };

        int maxBands = Math.max(this.numBands[SRC], this.numBands[DST]);
        int scanlineOffsets[][] = new int[2][maxBands];
        int pixelOffsets[][] = new int[2][maxBands];

        for (int k = 0; k < this.numBands[SRC]; k++) {
            scanlineOffsets[SRC][k] = bandOffsets[SRC][k];
        }
        for (int k = 0; k < this.numBands[DST]; k++) {
            scanlineOffsets[DST][k] = bandOffsets[DST][k];
        }

        for (int j = 0; j < dheight; j++) {
            transformer.nextLine();
            // log.debug("Rendering line " + j + " of " + dheight);

            // copy offsets
            for (int k = 0; k < this.numBands[SRC]; k++) {
                pixelOffsets[SRC][k] = scanlineOffsets[SRC][k];
            }
            for (int k = 0; k < this.numBands[DST]; k++) {
                pixelOffsets[DST][k] = scanlineOffsets[DST][k];
            }

            for (int i = 0; i < dwidth; i++) {
                // calculate transparency value
                byte b = transformer.doSomethingNasty(srcDataArrays, pixelOffsets[SRC],
                        numBands[SRC]);
                colsSum[i] += (int) b & 0xFF;
                rowsSum[j] += (int) b & 0xFF;
                dstDataArrays[0][pixelOffsets[DST][0]] = b;

                // increment x
                for (int k = 0; k < this.numBands[SRC]; k++) {
                    pixelOffsets[SRC][k] += pixelStrides[SRC];
                }
                for (int k = 0; k < this.numBands[DST]; k++) {
                    pixelOffsets[DST][k] += pixelStrides[DST];
                }
            }
            // increment y
            for (int k = 0; k < this.numBands[SRC]; k++) {
                scanlineOffsets[SRC][k] += scanlineStrides[SRC];
            }
            for (int k = 0; k < this.numBands[DST]; k++) {
                scanlineOffsets[DST][k] += scanlineStrides[DST];
            }
        }
    }

    public int[] getRowsSum() {
        return rowsSum;
    }

    public int[] getColsSum() {
        return colsSum;
    }
}
