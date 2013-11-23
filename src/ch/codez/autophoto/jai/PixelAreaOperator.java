/*
 * Created on 21.11.2007
 *
 */
package ch.codez.autophoto.jai;

import java.awt.Rectangle;
import java.awt.Transparency;
import java.awt.color.ColorSpace;
import java.awt.image.BandedSampleModel;
import java.awt.image.ComponentColorModel;
import java.awt.image.DataBuffer;
import java.awt.image.Raster;
import java.awt.image.RenderedImage;
import java.awt.image.WritableRaster;
import java.util.Map;

import javax.media.jai.AreaOpImage;
import javax.media.jai.BorderExtender;
import javax.media.jai.BorderExtenderConstant;
import javax.media.jai.ImageLayout;
import javax.media.jai.RasterAccessor;
import javax.media.jai.RasterFormatTag;

public abstract class PixelAreaOperator extends AreaOpImage {

    protected int pixelStride;

    protected int scanlineStride;

    public PixelAreaOperator(RenderedImage source, ImageLayout layout, Map configuration,
            int[] color, int bounds) {
        super(source, getImageLayout(layout, source), configuration, true,
                getConstantExtender(color), bounds, bounds, bounds, bounds);

        this.setProperty("colsSum", new int[source.getWidth()]);
        this.setProperty("rowsSum", new int[source.getHeight()]);
    }

    public void nextLine() {
    }

    public abstract byte doSomethingNasty(byte[][] srcDataArrays, int[] pixelOffsets, int numBands);

    /**
     * Performs a modified threshold operation on the pixels in a given
     * rectangle. Sample values below a lower limit are clamped to 0, while
     * those above an upper limit are clamped to 255. The results are returned
     * in the input WritableRaster dest. The sources are cobbled.
     * 
     * @param sources an array of sources, guarantee to provide all necessary
     *            source data for computing the rectangle.
     * @param dest a tile that contains the rectangle to be computed.
     * @param destRect the rectangle within this OpImage to be processed.
     */
    protected void computeRect(Raster[] sources, WritableRaster dest, Rectangle destRect) {
        Raster source = sources[0];
        Rectangle srcRect = mapDestRect(destRect, 0);
        RasterFormatTag[] formatTags = getFormatTags();

        RasterAccessor srcAccessor = new RasterAccessor(source, srcRect, formatTags[0],
                getSourceImage(0).getColorModel());
        RasterAccessor dstAccessor = new RasterAccessor(dest, destRect, formatTags[1],
                getColorModel());

        this.pixelStride = srcAccessor.getPixelStride();
        this.scanlineStride = srcAccessor.getScanlineStride();

        switch (dstAccessor.getDataType()) {
        case DataBuffer.TYPE_BYTE:
            RasterIterator iterator = new RasterIterator(srcAccessor, dstAccessor);
            iterator.iterate(this);
            setSums(iterator.getColsSum(), iterator.getRowsSum(), dest.getMinX(), dest.getMinY());
            break;
        default:
            String className = this.getClass().getName();
            throw new RuntimeException(className + " does not implement computeRect"
                    + " for int/short/float/double data");
        }

        // If the RasterAccessor object set up a temporary buffer for the
        // op to write to, tell the RasterAccessor to write that data
        // to the raster now that we're done with it.
        if (dstAccessor.isDataCopy()) {
            dstAccessor.clampDataArrays();
            dstAccessor.copyDataToRaster();
        }
    }

    private void setSums(int[] cols, int[] rows, int offsetX, int offsetY) {
        int[] colsSum = (int[]) getProperty("colsSum");
        int[] rowsSum = (int[]) getProperty("rowsSum");

        for (int i = 0; i < cols.length; i++) {
            colsSum[i + offsetX] += cols[i];
        }
        for (int i = 0; i < rows.length; i++) {
            rowsSum[i + offsetY] += rows[i];
        }
    }

    private static ImageLayout getImageLayout(ImageLayout layout, RenderedImage image) {
        if (layout == null) {
            layout = new ImageLayout();
        }
        layout.setSampleModel(new BandedSampleModel(DataBuffer.TYPE_BYTE, image.getWidth(), image
                .getHeight(), 1));
        layout.setColorModel(new ComponentColorModel(ColorSpace.getInstance(ColorSpace.CS_GRAY),
                new int[] { 8 }, false, false, Transparency.TRANSLUCENT, DataBuffer.TYPE_BYTE));
        return layout;
    }

    public static BorderExtender getConstantExtender(int[] color) {
        double[] constant = new double[color.length];
        for (int i = 0; i < color.length; i++) {
            constant[i] = color[i];
        }
        return new BorderExtenderConstant(constant);
    }

}
