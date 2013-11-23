/*
 * Created on 10.12.2007
 *
 */
package ch.codez.autophoto.gui;

import java.awt.Graphics;
import java.awt.Image;
import java.awt.Rectangle;

import javax.swing.JPanel;

public class BackgroundPane extends JPanel {
    
    private Image image;
    
    public BackgroundPane(Image background) {
        this.image = background;
    }

    protected void paintComponent(Graphics g) {   
        super.paintComponent(g);
        if (image == null) {
            return;
        }

        int width = image.getHeight(this);
        int height = image.getWidth(this);
        if (width <= 0 || height <= 0) {
            return;
        }
        
        Rectangle clip = g.getClipBounds();
        for (int x = clip.x; x < (clip.x + clip.width) ; x += width) {
            for (int y = clip.y; y < (clip.y + clip.height) ; y += height) {
                g.drawImage(this.image, x, y, this);
            }
        }
    }
}
