/*
 * Created on 09.12.2007
 *
 */
package ch.codez.autophoto.gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.GridLayout;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.HashSet;
import java.util.Set;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.text.DefaultStyledDocument;

import org.apache.log4j.Logger;

import ch.codez.autophoto.AppOptions;
import ch.codez.autophoto.controller.PaneCloseListener;

public class SemiTransparentPane extends JPanel {

    public final static Dimension SCREEN_SIZE = Toolkit.getDefaultToolkit().getScreenSize();

    public final static Dimension HALF_SCREEN_SIZE = new Dimension(
            (int) (SCREEN_SIZE.getWidth() * 0.75), (int) (SCREEN_SIZE.getHeight() * 0.75));

    public final static Dimension NOTIFICATION_SIZE = new Dimension(350, 250);

    protected final static int BORDER_WIDTH = 20;

    protected final static double ARC_SIZE = 0.1;

    // protected final static Color BG_COLOR = new Color(30, 30, 30, 230);
    protected final static Color BG_COLOR = new Color(50, 50, 50, 230);

    protected final static Color FONT_COLOR = new Color(255, 255, 255);

    private final static long SCALE_TIME = 500;

    private final static int FLASH_TIME = 20;

    private Set<PaneCloseListener> listeners = new HashSet<PaneCloseListener>();

    private JPanel bottom;

    private boolean flashing;

    protected JTextArea captionField;

    protected JPanel captionPane;

    private JButton use, discard;

    private JComponent content;

    private final static Logger log = Logger.getLogger(SemiTransparentPane.class);

    public SemiTransparentPane() {
        this.init();
    }

    public void showContent(JComponent component, Dimension size) {
        this.removeAll();
        this.content = component;
        this.add(component, BorderLayout.CENTER);
        this.add(this.bottom, BorderLayout.SOUTH);
        this.requestFocus();
        this.setVisible(true);
        if (size != null) {
            this.setSize(size);
        }
        this.validate();
        this.getParent().validate();
    }

    public synchronized void flashOff() {
        this.flashing = true;
        this.setSize(SCREEN_SIZE);
        this.repaint();
        try {
            Thread.sleep(FLASH_TIME);
        } catch (InterruptedException e) {
        }
        this.setVisible(false);
        this.flashing = false;
    }

    public void scaleTo(Dimension endSize) {
        if (endSize == null) {
            endSize = HALF_SCREEN_SIZE;
        }

        final Dimension startSize = this.getSize();
        final double scaleX = endSize.getWidth() - startSize.getWidth();
        final double scaleY = endSize.getHeight() - startSize.getHeight();
        new Thread() {
            public void run() {
                boolean scaling = true;
                long start = System.currentTimeMillis();
                while (scaling) {
                    long current = System.currentTimeMillis() - start;
                    if (current > SCALE_TIME) {
                        current = SCALE_TIME;
                        scaling = false;
                    }
                    double factor = current / (double) SCALE_TIME;
                    int w = (int) (startSize.getWidth() + factor * scaleX);
                    int h = (int) (startSize.getHeight() + factor * scaleY);

                    SemiTransparentPane.this.setSize(w, h);
                    SemiTransparentPane.this.repaint();

                    try {
                        Thread.sleep(50);
                    } catch (InterruptedException e) {
                    }
                }

                SemiTransparentPane.this.getRootPane().setDefaultButton(use);
                SemiTransparentPane.this.requestFocus();
            }
        }.start();
    }

    public void setSize(int width, int height) {
        super.setSize(width, height);
        this.setMaximumSize(new Dimension(width, height));
        this.setLocation((int) (SCREEN_SIZE.getWidth() - width) / 2,
                (int) (SCREEN_SIZE.getHeight() - height) / 2);
        this.validate();
        this.getParent().validate();
    }

    public void setSize(Dimension dim) {
        this.setSize((int) dim.getWidth(), (int) dim.getHeight());
    }

    public void addCloseListener(PaneCloseListener l) {
        listeners.add(l);
    }

    public void removeCloseListener(PaneCloseListener l) {
        listeners.remove(l);
    }

    public String getCaption() {
        return captionField.getText();
    }

    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        if (this.flashing) {
            this.paintFlash(g);
        } else {
            this.paintBackground(g);
        }
    }

    protected void paintBackground(Graphics g) {
        int w = this.getWidth();
        int h = this.getHeight();
        g.setColor(BG_COLOR);
        g.fillRoundRect(0, 0, w, h, (int) (w * ARC_SIZE), (int) (h * ARC_SIZE));
    }

    private void paintFlash(Graphics g) {
        g.setColor(Color.WHITE);
        g.fillRect(0, 0, (int) SCREEN_SIZE.getWidth(), (int) SCREEN_SIZE.getHeight());
    }

    private void init() {
        this.setVisible(false);
        this.setOpaque(false);
        this.setFocusable(true);
        this.setLayout(new BorderLayout(BORDER_WIDTH, BORDER_WIDTH));
        this.setBorder(BorderFactory.createEmptyBorder(BORDER_WIDTH, BORDER_WIDTH, BORDER_WIDTH,
                BORDER_WIDTH));
        this.addKeyListener(new EscapeKeyListener());
        this.initSlogan();
        this.initCloser();
    }

    private void initCloser() {
        bottom = new JPanel();
        bottom.setLayout(new GridLayout(1, 2, 50, 50));
        bottom.setBorder(BorderFactory.createEmptyBorder(0, 50, 0, 50));
        bottom.setOpaque(false);

        use = createButton("Verwenden", Color.GREEN);
        use.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                if (SemiTransparentPane.this.askForSlogan()) {
                    SemiTransparentPane.this.captionField.setText("");
                    SemiTransparentPane.this.showContent(captionPane, null);
                } else {
                    SemiTransparentPane.this.close(true);
                }
            }
        });
        bottom.add(use);

        discard = createButton("Verwerfen", Color.RED);
        discard.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                SemiTransparentPane.this.close(false);
            }
        });
        bottom.add(discard);

        this.add(this.bottom, BorderLayout.SOUTH);
    }

    private JButton createButton(String label, Color color) {
        JButton button = new JButton(label);
        button.setFont(button.getFont().deriveFont(30f));
        button.setBackground(Color.LIGHT_GRAY);
        button.setOpaque(true);
        button.setBorder(BorderFactory.createLineBorder(color.darker()));
        return button;
    }

    private void initSlogan() {
        int length = AppOptions.getInstance().getCaptionLength();

        this.captionPane = new JPanel();
        captionPane.setOpaque(false);
        captionPane.setLayout(new BorderLayout(BORDER_WIDTH, BORDER_WIDTH));

        int factor = 12 - ((length + 50) / 50);
        factor = factor < 1 ? 1 : factor;
        captionPane.setBorder(BorderFactory.createEmptyBorder(BORDER_WIDTH * factor,
                BORDER_WIDTH * 5, BORDER_WIDTH * factor, BORDER_WIDTH * 5));

        JLabel captionLabel = new JLabel("Dein Slogan:");
        captionLabel.setFont(captionLabel.getFont().deriveFont(30f));
        captionLabel.setForeground(Color.WHITE);
        captionPane.add(captionLabel, BorderLayout.NORTH);

        captionField = new JTextArea();
        captionField.setFont(captionField.getFont().deriveFont(30f));
        captionField.setLineWrap(true);
        captionField.setWrapStyleWord(true);
        captionField.setBorder(BorderFactory.createEmptyBorder(BORDER_WIDTH, BORDER_WIDTH,
                BORDER_WIDTH, BORDER_WIDTH));

        if (length > 0) {
            DefaultStyledDocument doc = new DefaultStyledDocument();
            doc.setDocumentFilter(new DocumentSizeFilter(length));
            captionField.setDocument(doc);
        }

        captionPane.add(captionField, BorderLayout.CENTER);
    }

    protected boolean askForSlogan() {
        return AppOptions.getInstance().getCaptionLength() != 0 && content != captionPane;
    }

    protected void close(boolean ok) {
        this.setVisible(false);
        for (PaneCloseListener l : this.listeners) {
            l.paneClosed(ok);
        }
    }

    private class EscapeKeyListener extends KeyAdapter {
        public void keyPressed(KeyEvent e) {
            if (e.getKeyCode() == KeyEvent.VK_ESCAPE) {
                SemiTransparentPane.this.close(false);
            } else if (e.getKeyCode() == KeyEvent.VK_ENTER || e.getKeyCode() == KeyEvent.VK_SPACE) {
                SemiTransparentPane.this.close(true);
            }
        }
    }

}
