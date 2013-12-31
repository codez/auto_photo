/*
 * Created on 09.12.2007
 *
 */
package ch.codez.autophoto.gui;

import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Insets;

import javax.swing.JPanel;

import org.apache.log4j.Logger;

import ch.codez.autophoto.AppOptions;

public class ImagePane extends JPanel {
    private final static Logger log = Logger.getLogger(ImagePane.class);

    private Image image;

    private boolean transparent = true;

    public ImagePane() {
    }

    public ImagePane(boolean transparent) {
        this.transparent = transparent;
    }

    public void setImage(Image i) {
        this.image = i;
    }

    protected void paintComponent(Graphics g) {
        if (!transparent) {
            super.paintComponent(g);
        }
        if (image == null) {
            return;
        }

        int rotation = AppOptions.getInstance().getRotationAngle();
        double ratio = getImageRatio();

        log.debug("ratio " + ratio);
        Insets insets = this.getInsets();
        int insetWidth = insets.left + insets.right;
        int insetHeight = insets.top + insets.bottom;

        int left = insets.left;
        int top = insets.top;
        int width = this.getWidth() - insetWidth;
        int height = this.getHeight() - insetHeight;
        log.debug("pane " + width + "x" + height);

        int maxWidth = (int) ((rotation == 0 ? height : width) * ratio);
        log.debug("maxWidth " + maxWidth);
        if (width > maxWidth) {
            left = (width - maxWidth) / 2 + insets.left;
            width = maxWidth;
        }
        int maxHeight = (int) (rotation == 0 ? width / ratio : width * ratio);
        log.debug("maxHeight " + maxHeight);
        if (height > maxHeight) {
            top = (height - maxHeight) / 2 + insets.top;
            height = maxHeight;
        }

        if (rotation != 0) {
            ((Graphics2D) g).rotate(Math.toRadians(rotation), (this.getWidth()) / 2,
                    (this.getHeight()) / 2);
        }
        log.debug("Draw at " + left + "x" + top + " w=" + width + " h=" + height);
        g.drawImage(this.image, left, top, width, height, null);
    }

    private double getImageRatio() {
        int width = this.image.getWidth(null);
        int height = this.image.getHeight(null);
        if (AppOptions.getInstance().getRotationAngle() != 0) {
            int dummy = width;
            width = height;
            height = dummy;
        }
        log.debug("Image w=" + width + " h=" + height);
        return width / (double) height;
    }
}
