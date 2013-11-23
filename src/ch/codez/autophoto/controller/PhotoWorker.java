/*
 * Created on 30.11.2007
 *
 */
package ch.codez.autophoto.controller;

import java.awt.image.BufferedImage;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashSet;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.PriorityBlockingQueue;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.log4j.Logger;

import ch.codez.autophoto.AppOptions;
import ch.codez.autophoto.jai.LayerBastler;

import com.sun.media.jai.codec.ImageCodec;
import com.sun.media.jai.codec.ImageEncoder;
import com.sun.media.jai.codec.PNGEncodeParam;

public class PhotoWorker implements Runnable {

    private static Logger log = Logger.getLogger(PhotoWorker.class);

    private static final long SLEEP_INTERVAL = 2000;

    private static PhotoWorker INSTANCE = new PhotoWorker();

    private Queue<File> tasks = new PriorityBlockingQueue<File>();

    private LayerBastler bastler = new LayerBastler();

    private Thread workThread;

    private boolean running = false;

    private AppOptions settings = AppOptions.getInstance();

    private Set<WorkerListener> listeners = new HashSet<WorkerListener>();

    public static PhotoWorker getInstance() {
        return INSTANCE;
    }

    public void start() {
        this.running = true;
        log.debug("told to start");
        this.workLoop();
    }

    public void stop() {
        if (this.running) {
            log.debug("told to stop.");
        }
        this.running = false;
    }

    public synchronized void addSouvenirImage(File image) {
        this.tasks.offer(image);
        if (!this.running) {
            this.start();
        }
    }

    public int getQueueLength() {
        return this.tasks.size();
    }

    public void run() {
        File image = null;
        try {
            Thread.sleep(settings.getDirectorWorkerDelay());
        } catch (InterruptedException e) {
        }
        while (this.running && (image = this.tasks.poll()) != null) {
            try {
                long start = System.currentTimeMillis();
                this.processImage(image);
                log.info(System.currentTimeMillis() - start + "ms to process image");
            } catch (Throwable t) {
                log.error("Fatal error while processing image", t);
                this.tasks.add(image);
            }

            for (WorkerListener l : listeners) {
                l.workerImageProcessed();
            }
            try {
                Thread.sleep(SLEEP_INTERVAL);
            } catch (InterruptedException e) {
            }
        }
        this.finish();
    }

    private synchronized void workLoop() {
        if (this.workThread == null) {
            this.workThread = new Thread(this);
            this.workThread.start();
            for (WorkerListener l : listeners) {
                l.workerStarted();
            }
            log.debug("started.");
        }
    }

    private void finish() {
        this.running = false;
        this.workThread = null;

        for (WorkerListener l : listeners) {
            l.workerStopped();
        }
        log.debug("finished");
    }

    private void processImage(File image) {
        String baseName = FilenameUtils.getBaseName(image.getName());
        String name = settings.getPathSouvenirs() + baseName + ".png";
        log.debug("Rendering souvenir " + name);

        try {
            BufferedImage souvenir = bastler.compose(image.getAbsolutePath());
            this.save(souvenir, name);
            log.debug("Souvenir " + name + " saved.");
            FileUtils.copyFile(new File(name), new File(settings.getPathDestination() + baseName
                    + ".png"));
        } catch (IOException e) {
            log.error("Could not save souvenir to file " + name, e);
        } catch (IllegalArgumentException iae) {
            log.error("Could not compose souvenir " + name, iae);
        }
    }

    private void save(BufferedImage image, String file) throws IOException {
        BufferedOutputStream out = new BufferedOutputStream(new FileOutputStream(file));
        PNGEncodeParam.RGB param = new PNGEncodeParam.RGB();
        ImageEncoder encoder = ImageCodec.createImageEncoder("png", out, param);

        encoder.encode(image);
        out.close();
    }

    public void addWorkerListener(WorkerListener listener) {
        listeners.add(listener);
    }

    public void removeWorkerListener(WorkerListener listener) {
        listeners.remove(listener);
    }
}
