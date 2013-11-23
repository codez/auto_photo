/*
 * Created on 09.12.2007
 *
 */
package ch.codez.autophoto.gui;

import java.awt.Graphics;
import java.awt.Image;
import java.awt.Insets;

import javax.swing.JPanel;

public class ImagePane extends JPanel {

    private Image image;
    
    private boolean transparent = true;
    
    public ImagePane() {}
    
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
        
        Insets insets = this.getInsets();
        int insetWidth = insets.left + insets.right;
        int insetHeight = insets.top + insets.bottom;
        
        int left = insets.left;
        int top = insets.top;
        int width = this.image.getWidth(null);
        int height = this.image.getHeight(null);
        double ratio = width / (double)height;
        
        width = this.getWidth() - insetWidth;
        height = this.getHeight() - insetHeight;
        int maxWidth = (int)(height * ratio);
        if (width > maxWidth) {
            left = (width - maxWidth) / 2 + insets.left;
            width = maxWidth;
        }
        int maxHeight = (int)(width / ratio);
        if (height > maxHeight) {
            top = (height - maxHeight) / 2 + insets.top;
            height = maxHeight;
        }
        
        g.drawImage(this.image, left, top, width, height, null);
    }
}
