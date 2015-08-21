/*
 * Created on 09.12.2007
 *
 */
package ch.codez.autophoto.gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
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
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.text.DefaultStyledDocument;

import org.apache.log4j.Logger;

import ch.codez.autophoto.AppOptions;
import ch.codez.autophoto.controller.PaneCloseListener;

public class PreviewPane extends JPanel {

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

    private final static int FONT_SIZE = 36;

    private Set<PaneCloseListener> listeners = new HashSet<PaneCloseListener>();

    private JPanel bottom;

    private boolean flashing;

    protected JTextField nameField = new JTextField();

    protected JTextField crimeField = new JTextField();

    protected JPanel captionPane;

    private JButton use, discard;

    private JComponent content;

    private final static Logger log = Logger.getLogger(PreviewPane.class);

    public PreviewPane() {
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
        this.repaint();
        this.nameField.requestFocus();
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

                    PreviewPane.this.setSize(w, h);
                    PreviewPane.this.repaint();

                    try {
                        Thread.sleep(50);
                    } catch (InterruptedException e) {
                    }
                }

                PreviewPane.this.getRootPane().setDefaultButton(use);
                PreviewPane.this.requestFocus();
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

    public String getName() {
        return nameField.getText();
    }

    public String getCrime() {
        return crimeField.getText();
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

        use = createButton("Verwenden", new Color(90, 255, 90));
        use.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                if (PreviewPane.this.askForSlogan()) {
                    PreviewPane.this.nameField.setText("");
                    PreviewPane.this.crimeField.setText("");
                    log.debug("Show Caption Pane");
                    PreviewPane.this.showContent(captionPane, null);
                } else {
                    PreviewPane.this.close(true);
                }
            }
        });
        bottom.add(use);

        discard = createButton("Verwerfen", new Color(255, 90, 90));
        discard.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                PreviewPane.this.close(false);
            }
        });
        bottom.add(discard);

        this.add(this.bottom, BorderLayout.SOUTH);
    }

    private JButton createButton(String label, Color color) {
        JButton button = new JButton(label);
        button.setFont(getLafFont());
        button.setBackground(color.brighter()); // (Color.LIGHT_GRAY);
        button.setOpaque(true);
        button.setBorder(BorderFactory.createLineBorder(color.darker()));
        button.setPreferredSize(new Dimension(60, 60));
        return button;
    }

    private Font getLafFont() {
        return new Font(AppOptions.getInstance().getLafFont(), Font.PLAIN, FONT_SIZE);
    }

    private void initSlogan() {
        this.captionPane = new JPanel();
        captionPane.setOpaque(false);
        captionPane.setLayout(new BoxLayout(captionPane, BoxLayout.Y_AXIS));
        captionPane.setBorder(BorderFactory.createEmptyBorder(BORDER_WIDTH * 5, BORDER_WIDTH * 5,
                BORDER_WIDTH * 5, BORDER_WIDTH * 5));

        captionPane.add(Box.createVerticalGlue());

        addLabeledField("Name, Vorname:", nameField);

        captionPane.add(Box.createRigidArea(new Dimension(BORDER_WIDTH, BORDER_WIDTH * 3)));

        addLabeledField("Tatbestand:", crimeField);
    }

    private void addLabeledField(String caption, JTextField field) {
        // label
        JLabel captionLabel = new JLabel(caption);
        captionLabel.setFont(getLafFont());
        captionLabel.setForeground(Color.WHITE);
        captionLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        captionPane.add(captionLabel);

        // gap
        captionPane.add(Box.createRigidArea(new Dimension(BORDER_WIDTH, BORDER_WIDTH)));

        // input
        field.setFont(getLafFont());
        field.setAlignmentX(Component.CENTER_ALIGNMENT);
        field.setBorder(BorderFactory.createEmptyBorder(0, BORDER_WIDTH, 0, BORDER_WIDTH));

        int length = AppOptions.getInstance().getCaptionLength();
        if (length > 0) {
            DefaultStyledDocument doc = new DefaultStyledDocument();
            doc.setDocumentFilter(new DocumentSizeFilter(length));
            field.setDocument(doc);
        }

        captionPane.add(field);
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
                PreviewPane.this.close(false);
            } else if (e.getKeyCode() == KeyEvent.VK_ENTER || e.getKeyCode() == KeyEvent.VK_SPACE) {
                PreviewPane.this.close(true);
            }
        }
    }

}
