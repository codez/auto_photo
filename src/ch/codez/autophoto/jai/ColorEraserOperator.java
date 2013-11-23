/*
 * Created on 21.11.2007
 *
 */
package ch.codez.autophoto.jai;

import java.awt.image.RenderedImage;
import java.util.Map;

import javax.media.jai.ImageLayout;

public class ColorEraserOperator extends PixelAreaOperator {
   
    int[] color;
    int lowerSimilarityThreshold,
        upperSimilarityThreshold,
        ratio;
    
    public ColorEraserOperator(RenderedImage source, ImageLayout layout, 
                               Map configuration, 
                               int[] color, int bounds, int tolerance) {
       super(source, layout, configuration, color, bounds);

       this.color = color;
       this.lowerSimilarityThreshold = (int)(tolerance * 0.9 * 3 * 255 / 100);
       this.upperSimilarityThreshold = (int)(tolerance * 1.8 * 3 * 255 / 100);
       this.ratio = 255 / (upperSimilarityThreshold - lowerSimilarityThreshold);
    }
    
    public byte doSomethingNasty(byte[][] srcDataArrays, int[] pixelOffsets, int numBands) {
        double sum = this.getPixelBackgroundSimilarity(srcDataArrays, pixelOffsets, numBands);
        if (sum <= lowerSimilarityThreshold) {
            return (byte)0;
        } else if (sum < upperSimilarityThreshold) {
            return (byte)((sum - lowerSimilarityThreshold) * ratio);
        } else {
            return (byte)255;
        }
    }
    
    private double getPixelBackgroundSimilarity(byte[][] srcDataArrays, int[] pixelOffsets, int numBands) {
        int sum = 0;
        for (int k = 0; k < numBands; k++) {
            int pixel = srcDataArrays[k][pixelOffsets[k]] & 0xff;
            sum += Math.abs(color[k] - pixel);
        }
        return sum;
    }   

}
