/*
 * Created on 10.12.2007
 *
 */
package ch.codez.souvenirbooth.gui;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.Stroke;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.Arc2D;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Point2D;

import javax.swing.Action;
import javax.swing.Icon;
import javax.swing.JButton;

public class RoundButton extends JButton {

    private final static int CIRCLE_BORDER = 20;
    private final static int BORDER_DIST = 1;

    private Shape shape = null;

    private boolean mouseInside = false;

    public RoundButton(Action a) {
        super(a);
        this.init();
    }

    public RoundButton(Icon icon) {
        super(icon);
        this.init();
    }

    private void init() {
        Icon icon = this.getIcon();
        if (icon != null) {
            Dimension size = getPreferredSize();
            size.width = size.height = Math.max(icon.getIconWidth() + CIRCLE_BORDER,
                    icon.getIconHeight() + CIRCLE_BORDER);
            setPreferredSize(size);
        }
        setFocusPainted(icon == null);
        setContentAreaFilled(false);
        this.addMouseListener(new MouseInsideListener());
    }

    public void setIcon(Icon i) {
        super.setIcon(i);
        this.init();
    }

    protected void paintComponent(Graphics g) {
        if (this.mouseInside && !this.getModel().isPressed()) {
            this.paintHighlight(g);
        }
        this.getIcon().paintIcon(this, g, CIRCLE_BORDER/2, CIRCLE_BORDER/2);
    }

    protected void paintBorder(Graphics g) {
        Graphics2D g2 = (Graphics2D) g;

        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_RENDERING,
                RenderingHints.VALUE_RENDER_QUALITY);

        Stroke s = new BasicStroke(1);
        g2.setStroke(s);

        //gradient helpers
        Color col1 = getForeground().darker();
        Color col2 = getForeground().brighter();
        Point2D.Double pt1 = new Point2D.Double(0, 0);
        Point2D.Double pt2 = new Point2D.Double(getSize().width - 1,
                getSize().height - 1);

        //inner circle
        Arc2D.Double arc = new Arc2D.Double(BORDER_DIST, BORDER_DIST,
                getSize().width - 2 - 2*BORDER_DIST,
                getSize().height - 2 - 2*BORDER_DIST,
                0, 360, Arc2D.CHORD);
        GradientPaint gp  = new GradientPaint(pt1, col2, pt2, col1, true);
        g2.setPaint(gp);
        g2.draw(arc);

        //outer circle
        arc = new Arc2D.Double(0, 0, getSize().width - 2,
                getSize().height - 2, 0, 360, Arc2D.CHORD);
        gp = new GradientPaint(pt1, col1, pt2, col2, true);
        g2.setPaint(gp);
        g2.draw(arc);
    }

    protected void setInside(boolean inside) {
        this.mouseInside = inside;
        this.repaint();
    }

    private void paintHighlight(Graphics g) {
        Graphics2D g2 = (Graphics2D)g;
        g2.setColor(this.getForeground());

        Arc2D.Double arc = new Arc2D.Double(1, 1, getSize().width - 3,
                getSize().height - 3, 0, 360, Arc2D.CHORD);
        g2.fill(arc);
    }

    public boolean contains(int x, int y) {
        if (shape == null || !shape.getBounds().equals(getBounds())) {
            shape = new Ellipse2D.Float(0, 0, getWidth(), getHeight());
        }
        return shape.contains(x, y);
    }

    private class MouseInsideListener extends MouseAdapter {
        public void mouseEntered(MouseEvent e) {
            RoundButton.this.setInside(true);
        }
        public void mouseExited(MouseEvent e) {
            RoundButton.this.setInside(false);
        }
    }

}
