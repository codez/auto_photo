/*
 * Created on 21.11.2007
 *
 */
package ch.codez.autophoto.controller;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import org.apache.log4j.Logger;

import ch.codez.autophoto.AppOptions;

import javax.imageio.ImageIO;

public class LayerBastler {

    private static Logger log = Logger.getLogger(LayerBastler.class);

    public BufferedImage compose(String snapshotFile) throws IOException {
        long start = System.currentTimeMillis();
        BufferedImage image = this.loadFile(snapshotFile);
        long end = System.currentTimeMillis();
        log.debug("loading image - " + (end - start));

        return image;
    }

    private BufferedImage loadFile(String filename) throws IOException {
        BufferedImage image = ImageIO.read(new File(filename));
        image = scale(image);

        log.debug("loaded " + filename + " - " + image.getWidth() + "x" + image.getHeight());
        return image;
    }

    private BufferedImage scale(BufferedImage image) {
        int maxWidth = AppOptions.getInstance().getMaxWidth();
        if (maxWidth > 0 && maxWidth < image.getWidth()) {
            float factor = maxWidth / (float) image.getWidth();
            int height = (int) (image.getHeight() * factor);
            BufferedImage outputImage = new BufferedImage(maxWidth,
                    height, image.getType());

            // scales the input image to the output image
            Graphics2D g2d = outputImage.createGraphics();
            g2d.drawImage(image, 0, 0, maxWidth, height, null);
            g2d.dispose();
            return outputImage;
        }
        return image;
    }
}
