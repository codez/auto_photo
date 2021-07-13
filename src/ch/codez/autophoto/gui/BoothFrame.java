/*
 * Created on 08.04.2004
 *
 * $Id: ChicaneFrame.java,v 1.6 2004/04/14 23:12:47 pascal Exp $
 */
package ch.codez.autophoto.gui;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.io.File;

import javax.swing.*;

import ch.codez.autophoto.controller.*;
import org.apache.log4j.Logger;

import ch.codez.autophoto.AppOptions;

public class BoothFrame extends JFrame implements PaneCloseListener, WorkerListener, DirectorListener {

    private final static String CONTROL_BG_IMAGE = "/images/metal.jpg";

    private final static String SPINNER_IMAGE = "/images/spinner.gif";
    private final static String SPINNER_LARGE_IMAGE = "/images/spinner_large.gif";

    private final static String CAMERA_IMAGE = "/images/camera.gif";

    private final static int BORDER = 50;

    private final static int BUTTON_SIZE = 60;
    

    private static Logger log = Logger.getLogger(BoothFrame.class);

    // notification components
    private PreviewPane notifier = new PreviewPane();

    private ImagePane imagePane = new ImagePane();

    private File currentImage = null;

    private Director director = new Director();

    private boolean isCountingDown = false;

    private JLabel queueLabel = new JLabel();
    private JLabel countdown = new JLabel();
    private JLabel spinner = new JLabel();
    private JLabel processing = new JLabel();

    private JPanel triggerPane = new JPanel();

    
    public BoothFrame() {
        super("AutoPhoto");
        this.director.addListener(this);
        this.init();
    }

    public void runFullScreen() {
        if (AppOptions.getInstance().getIsKioskMode()) {
            GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
            GraphicsDevice gs = ge.getDefaultScreenDevice();
            gs.setFullScreenWindow(this);
        }

        PhotoWorker.getInstance().addWorkerListener(this);
        this.validate();
    }

    public void paneClosed(boolean ok) {
        log.debug("Pane closed " + ok);
        if (ok && currentImage != null) {
            PhotoWorker.getInstance().addPhoto(currentImage, notifier.getCaption());
        }
        if (this.isCountingDown) {
            this.director.cancel();
            this.isCountingDown = false;
        }
        this.triggerPane.setVisible(true);
    }

    public void countDownAt(int i) {
        this.isCountingDown = i > 0;
        log.debug("count down " + this.isCountingDown + " at " + i);
        this.countdown.setText(String.valueOf(i));
        this.notifier.showContent(this.countdown, PreviewPane.HALF_SCREEN_SIZE, false);

        if (i == 0) {
            this.notifier.flashOff();
        }
        this.validate();
    }

    public void processing() {
        this.notifier.showContent(this.processing,
                PreviewPane.HALF_SCREEN_SIZE, false);
    }

    public void ready(String filename) {
        if (this.isCountingDown) {
            log.debug("Ready but still counting down" );
            return;
        }
        log.debug("Ready " + filename);
        if (filename != null) {
            this.currentImage = new File(filename);
            ImageIcon icon = new ImageIcon(filename);
            this.imagePane.setImage(icon.getImage());
            this.notifier.showContent(this.imagePane, PreviewPane.SCREEN_SIZE, true);
            this.notifier.scaleTo(PreviewPane.HALF_SCREEN_SIZE);
        }
    }

    private boolean isNotifying() {
        return this.notifier.isVisible();
    }

    protected void init() {
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.setBackground(Color.BLACK);
        this.setSize(Toolkit.getDefaultToolkit().getScreenSize());

        if (AppOptions.getInstance().getIsKioskMode()) {
            this.setUndecorated(true);
            this.setResizable(false);
        }

        this.setVisible(true);

        this.initFramePanel();
        this.initCountdown();
        this.initProcessing();
        this.notifier.addCloseListener(this);

        this.validate();
    }

    private void initFramePanel() {
        JPanel container = new JPanel();
        container.setLayout(new BorderLayout());
        if (!AppOptions.getInstance().getIsKioskMode()) {
            container.setBorder(BorderFactory.createLineBorder(Color.BLACK, 20));
        }

        JLayeredPane center = new JLayeredPane();
        center.setLayout(new OverlayLayout(center));
        center.add(this.notifier, JLayeredPane.POPUP_LAYER);
        center.add(this.initLabelPane(), JLayeredPane.DEFAULT_LAYER);

        container.add(center, BorderLayout.CENTER);
        container.add(this.initSpinnerPane(), BorderLayout.SOUTH);
        this.getContentPane().add(container, BorderLayout.CENTER);
    }

    private JPanel initLabelPane() {
        JPanel pane = new JPanel();
        pane.setLayout(new BorderLayout());
        pane.setBorder(BorderFactory.createEmptyBorder(BORDER, BORDER, BORDER, BORDER));
        pane.add(initInfo(), BorderLayout.CENTER);
        pane.add(this.initTriggerButton(), BorderLayout.SOUTH);
        pane.setBackground(Color.black);
        return pane;
    }

    private JComponent initInfo() {
        JLabel message = new JLabel("<html><center>" + AppOptions.getInstance().getLafMessageMain()
                + "</center></html>");

        Font font = new Font(AppOptions.getInstance().getLafFont(), Font.PLAIN,
                (int) PreviewPane.SCREEN_SIZE.getHeight() / 15);
        message.setFont(font);
        message.setForeground(Color.white);
        message.setHorizontalAlignment(JLabel.CENTER);
        return message;
    }

    private JComponent initTriggerButton() {
        ImageIcon icon = this.loadIcon(CAMERA_IMAGE);
        icon = new ImageIcon(icon.getImage().getScaledInstance(
                BUTTON_SIZE, BUTTON_SIZE, Image.SCALE_SMOOTH));
        Color color = new Color(90, 255, 90);
        JButton button = new JButton(new CountdownAction());
        button.setFont(getLafFont());
        button.setOpaque(true);
        button.setBackground(color.brighter());
        button.setAlignmentX(Component.CENTER_ALIGNMENT);
        button.setBorder(BorderFactory.createLineBorder(color.darker()));
        button.setPreferredSize(new Dimension(400, 60));
        button.setMaximumSize(new Dimension(400, 60));
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        triggerPane.setLayout(new BoxLayout(triggerPane, BoxLayout.Y_AXIS));
        triggerPane.add(button);
        triggerPane.setBackground(Color.black);
        return triggerPane;
    }

    private Font getLafFont() {
        return new Font(AppOptions.getInstance().getLafFont(), Font.PLAIN, 36);
    }

    private JPanel initSpinnerPane() {
        JPanel pane = new JPanel();
        pane.setLayout(new BorderLayout());
        pane.setBackground(Color.black);

        JPanel overlay = new JPanel();
        overlay.setLayout(new OverlayLayout(overlay));
        overlay.setBackground(Color.black);

        ImageIcon icon = this.loadIcon(SPINNER_IMAGE);
        spinner.setIcon(icon);
        spinner.setAlignmentX(0.5f);
        spinner.setAlignmentY(0.5f);
        spinner.setVisible(false);

        queueLabel.setText("");
        queueLabel.setForeground(Color.WHITE);
        queueLabel.setFont(queueLabel.getFont().deriveFont(9f));
        queueLabel.setAlignmentX(0.5f);
        queueLabel.setAlignmentY(0.5f);
        queueLabel.setVisible(false);

        overlay.add(queueLabel);
        overlay.add(spinner);

        pane.add(overlay, BorderLayout.WEST);
        return pane;
    }

    private void initCountdown() {
        this.countdown.setHorizontalAlignment(JLabel.CENTER);
        Font font = this.countdown.getFont().deriveFont(Font.BOLD,
                (int)PreviewPane.HALF_SCREEN_SIZE.getHeight() / 2);
        this.countdown.setFont(font);
        this.countdown.setForeground(Color.WHITE);
    }

    private void initProcessing() {
        ImageIcon icon = this.loadIcon(SPINNER_LARGE_IMAGE);
        processing.setIcon(icon);
        processing.setOpaque(false);
        processing.setIconTextGap(20);

        processing.setHorizontalAlignment(JLabel.CENTER);
        processing.setHorizontalTextPosition(JLabel.CENTER);
        processing.setVerticalTextPosition(JLabel.BOTTOM);
        processing.setForeground(Color.WHITE);
        processing.setText(null);
    }

    private ImageIcon loadIcon(String file) {
        ImageIcon icon = new ImageIcon(this.getClass().getResource(file));
        log.debug("Image " + file + " loaded with status " + icon.getImageLoadStatus());
        return icon;
    }


    private void updateSpinner(boolean visible) {
        queueLabel.setText(String.valueOf(PhotoWorker.getInstance().getQueueLength()));
        spinner.setVisible(visible);
        queueLabel.setVisible(visible);
        spinner.validate();
        queueLabel.validate();
    }

    public void workerStarted() {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                updateSpinner(true);
            }
        });
    }

    public void workerStopped() {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                updateSpinner(false);
            }
        });
    }

    public void workerImageProcessed() {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                updateSpinner(true);
            }
        });
    }

    private class CountdownAction extends AbstractAction {
        public CountdownAction() {
            super("Foto aufnehmen");
        }

        public void actionPerformed(ActionEvent e) {
            if (BoothFrame.this.isNotifying()) {
                return;
            }

            BoothFrame.this.triggerPane.setVisible(false);
            BoothFrame.this.director.andAction();
        }
    }
}
