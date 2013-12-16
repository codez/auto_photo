/*
 * Created on 08.04.2004
 *
 * $Id: ChicaneFrame.java,v 1.6 2004/04/14 23:12:47 pascal Exp $
 */
package ch.codez.autophoto.gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.Toolkit;
import java.io.File;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JLayeredPane;
import javax.swing.JPanel;
import javax.swing.OverlayLayout;
import javax.swing.SwingUtilities;

import org.apache.commons.io.monitor.FileAlterationListenerAdaptor;
import org.apache.commons.io.monitor.FileAlterationMonitor;
import org.apache.commons.io.monitor.FileAlterationObserver;
import org.apache.log4j.Logger;

import ch.codez.autophoto.AppOptions;
import ch.codez.autophoto.controller.PaneCloseListener;
import ch.codez.autophoto.controller.PhotoWorker;
import ch.codez.autophoto.controller.WorkerListener;

public class BoothFrame extends JFrame implements PaneCloseListener, WorkerListener {

    private final static String CONTROL_BG_IMAGE = "/images/metal.jpg";

    private final static String SPINNER_IMAGE = "/images/spinner.gif";

    private final static int BORDER = 50;

    private static Logger log = Logger.getLogger(BoothFrame.class);

    // notification components
    private SemiTransparentPane notifier = new SemiTransparentPane();

    private ImagePane previewPane = new ImagePane();

    private FileAlterationMonitor fileMonitor;

    private Queue<File> pictureQueue = new ConcurrentLinkedQueue<File>();

    private File currentImage = null;

    private JLabel queueLabel = new JLabel();

    private JLabel spinner = new JLabel();

    public BoothFrame() {
        super("AutoPhoto");
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
        if (ok) {
            PhotoWorker.getInstance().addSouvenirImage(currentImage, notifier.getCaption());
        }
        ready();
    }

    public void ready() {
        if (this.isNotifying()) {
            return;
        }

        currentImage = pictureQueue.poll();
        if (currentImage != null) {
            ImageIcon icon = new ImageIcon(currentImage.getAbsolutePath());
            this.previewPane.setImage(icon.getImage());
            this.notifier.showContent(this.previewPane, SemiTransparentPane.SCREEN_SIZE);
            this.notifier.scaleTo(SemiTransparentPane.HALF_SCREEN_SIZE);
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
        this.initFileMonitor();
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
        return pane;
    }

    private JComponent initInfo() {
        JLabel message = new JLabel("<html><center>" + AppOptions.getInstance().getLafMessageMain()
                + "</center></html>");
        Font font = message.getFont().deriveFont(Font.BOLD,
                (int) SemiTransparentPane.SCREEN_SIZE.getHeight() / 15);
        message.setFont(font);
        message.setForeground(Color.white);
        message.setHorizontalAlignment(JLabel.CENTER);
        return message;
    }

    private JPanel initSpinnerPane() {
        JPanel pane = new JPanel();
        pane.setLayout(new BorderLayout());

        JPanel overlay = new JPanel();
        overlay.setLayout(new OverlayLayout(overlay));

        ImageIcon icon = this.loadIcon(SPINNER_IMAGE);
        spinner.setIcon(icon);
        queueLabel.setText("");
        queueLabel.setForeground(Color.WHITE);
        queueLabel.setFont(queueLabel.getFont().deriveFont(9f));
        spinner.setAlignmentX(0.5f);
        spinner.setAlignmentY(0.5f);
        queueLabel.setAlignmentX(0.5f);
        queueLabel.setAlignmentY(0.5f);
        queueLabel.setVisible(false);
        spinner.setVisible(false);

        overlay.add(queueLabel);
        overlay.add(spinner);

        pane.add(overlay, BorderLayout.WEST);
        return pane;
    }

    private void initFileMonitor() {
        FileAlterationObserver observer = new FileAlterationObserver(AppOptions.getInstance()
                .getPathSnapshots());
        observer.addListener(new FileListener());
        fileMonitor = new FileAlterationMonitor(1000, observer);
        try {
            fileMonitor.start();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private ImageIcon loadIcon(String file) {
        ImageIcon icon = new ImageIcon(this.getClass().getResource(file));
        log.debug("Image " + file + " loaded with status " + icon.getImageLoadStatus());
        return icon;
    }

    private class FileListener extends FileAlterationListenerAdaptor {
        public void onFileCreate(File file) {
            String fileName = file.getName().toLowerCase();
            if ((fileName.endsWith(".jpg") || fileName.endsWith(".jpeg"))
                    && !fileName.startsWith("~")) {
                pictureQueue.add(file);
                SwingUtilities.invokeLater(new Runnable() {
                    public void run() {
                        ready();
                    }
                });
            }
        }
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

}
