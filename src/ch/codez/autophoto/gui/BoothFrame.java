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
import ch.codez.souvenirbooth.gui.RoundButton;
import org.apache.log4j.Logger;

import ch.codez.autophoto.AppOptions;

public class BoothFrame extends JFrame implements PaneCloseListener, WorkerListener, DirectorListener {

    private final static String CONTROL_BG_IMAGE = "/images/metal.jpg";

    private final static String SPINNER_IMAGE = "/images/spinner.gif";

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
        log.debug("Pane closed");
        if (ok && currentImage != null) {
            PhotoWorker.getInstance().addSouvenirImage(currentImage, notifier.getName(),
                    notifier.getCrime());
        }
        if (this.isCountingDown) {
            this.director.cancel();
            this.isCountingDown = false;
        }
    }
    public void countDownAt(int i) {
        this.isCountingDown = true;
        this.countdown.setText(String.valueOf(i));
        this.notifier.showContent(this.countdown, PreviewPane.HALF_SCREEN_SIZE, false);

        if (i == 0) {
            this.notifier.flashOff();
            this.isCountingDown = false;
        }
        this.validate();
    }

    public void processing() {
        /*
        this.notifier.showContent(this.processing,
                PreviewPane.NOTIFICATION_SIZE);
        this.notifier.setCloseText("its längt's");
         */
    }

    public void ready(String filename) {
        if (this.isNotifying() || this.isCountingDown) {
            return;
        }
        if (filename != null) {
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

    private JButton initTriggerButton() {
        ImageIcon icon = this.loadIcon(CAMERA_IMAGE);
        icon = new ImageIcon(icon.getImage().getScaledInstance(
                BUTTON_SIZE, BUTTON_SIZE, Image.SCALE_SMOOTH));
        JButton button = new RoundButton(new CountdownAction(icon));
        button.setForeground(AppOptions.getInstance().getLafColorHighlight());
        return button;
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
        public CountdownAction(Icon icon) {
            super(null, icon);
        }

        public void actionPerformed(ActionEvent e) {
            if (BoothFrame.this.isNotifying()) {
                return;
            }
            BoothFrame.this.director.andAction();
        }
    }
}
