/*
 * Created on 04.12.2007
 *
 */
package ch.codez.autophoto.camera;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;


public class CamDelayUtil extends JFrame {

    private static final String TEMP_FILE = "test.jpg";
    
    private JLabel counter = new JLabel("0");
    
    private boolean working = false;

    public CamDelayUtil() {
        super("Counter");
        this.init();
    }

    public static void main(String[] args) {
        JFrame f = new CamDelayUtil();
    }

    private void init() {
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.setSize(Toolkit.getDefaultToolkit().getScreenSize());
        this.setVisible(true);

        Font font = this.counter.getFont();
        font = font.deriveFont(Font.BOLD, (float)this.getSize().getHeight()/4);
        this.counter.setFont(font);
        this.counter.setHorizontalAlignment(JLabel.CENTER);
        this.getContentPane().add(this.counter, BorderLayout.CENTER);

        this.getContentPane().add(new JButton(new StartAction()), BorderLayout.SOUTH);
    }

    private class RunCounter implements Runnable {
        public void run() {
            long startTime = System.currentTimeMillis();
            while (CamDelayUtil.this.working) {
                long value = System.currentTimeMillis() - startTime;
                CamDelayUtil.this.counter.setText(String.valueOf(value));
                CamDelayUtil.this.repaint();
                try {
                    Thread.sleep(200);
                } catch (InterruptedException e) {}
            }
        }
    }

    private class RunSnapshot implements Runnable {
        public void run() {
            //take picture
            CameraAdapter cam = CameraAdapter.getInstance();
            cam.takeSnapshot(TEMP_FILE);

            CamDelayUtil.this.working = false;
        }
    }

    private class StartAction extends AbstractAction {
        public void actionPerformed(ActionEvent e) {
            CamDelayUtil.this.working = true;
            Thread counter = new Thread(new RunCounter());
            Thread snapshot = new Thread(new RunSnapshot());
            snapshot.start();
            counter.start();
        }
    }
}
