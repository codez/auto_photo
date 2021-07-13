/*
 * Created on 30.11.2007
 *
 */
package ch.codez.autophoto.controller;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.PriorityBlockingQueue;

import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;


import ch.codez.autophoto.AppOptions;

import javax.imageio.ImageIO;

public class PhotoWorker implements Runnable {

    private static Logger log = Logger.getLogger(PhotoWorker.class);

    private static final long SLEEP_INTERVAL = 2000;

    private static PhotoWorker INSTANCE = new PhotoWorker();

    private Queue<Picture> tasks = new PriorityBlockingQueue<Picture>();

    private LayerBastler bastler = new LayerBastler();

    private Thread workThread;

    private boolean running = false;

    private AppOptions settings = AppOptions.getInstance();

    private Set<WorkerListener> listeners = new HashSet<WorkerListener>();

    private File captionFile;

    public static PhotoWorker getInstance() {
        return INSTANCE;
    }

    public PhotoWorker() {
        this.captionFile = new File(settings.getPathSouvenirs() + settings.getCaptionFile());
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

    public synchronized void addPhoto(File image, String name) {
        this.tasks.offer(new Picture(image, name));
        if (!this.running) {
            this.start();
        }
    }

    public int getQueueLength() {
        return this.tasks.size();
    }

    public void run() {
        Picture picture = null;
        try {
            Thread.sleep(settings.getDirectorWorkerDelay());
        } catch (InterruptedException e) {
        }
        while (this.running && (picture = this.tasks.poll()) != null) {
            try {
                long start = System.currentTimeMillis();
                this.processImage(picture);
                log.info(System.currentTimeMillis() - start + "ms to process image");
            } catch (Throwable t) {
                log.error("Fatal error while processing image", t);
                this.tasks.add(picture);
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

    private void processImage(Picture picture) {
        String name = picture.getWorkName();
        log.debug("Rendering souvenir " + name);

        try {
            BufferedImage souvenir = bastler.compose(picture.getSourceName());
            saveImage(souvenir, name);
            if (settings.getCaptionLength() != 0) {
                saveCaption(picture);
            }
            copyToDestination(picture, name);
            log.debug("Souvenir " + name + " saved.");
        } catch (IOException e) {
            log.error("Could not save souvenir to file " + name, e);
        } catch (IllegalArgumentException iae) {
            log.error("Could not compose souvenir " + name, iae);
        }
    }

    private void saveImage(BufferedImage image, String file) throws IOException {
        ImageIO.write(image, "JPG", new File(file));
    }

    private void saveCaption(Picture picture) throws IOException {
        String json = "[]";
        if (captionFile.exists()) {
            json = FileUtils.readFileToString(captionFile, "UTF-8").trim();
        }

        StringBuilder result = new StringBuilder();
        String existing = json.substring(0, json.length() - 1).trim();
        result.append(existing);
        if (existing.endsWith("}")) {
            result.append(",");
        }
        result.append("\n\t{ \"image\": \"");
        result.append(picture.getBaseName()).append(".").append(Picture.EXTENSION);
        result.append("\",\n\t  \"caption\": ");
        result.append(JsonUtil.quote(picture.getCaption()));
        result.append(",\n\t  \"date\": ");
        result.append(JsonUtil.quote(picture.getDate()));
        result.append(" }\n");
        result.append("]");

        File tmpFile = new File(captionFile.getAbsolutePath() + ".tmp");
        FileUtils.write(tmpFile, result, "UTF-8");
        FileUtils.copyFile(tmpFile, captionFile);
    }

    private void copyToDestination(Picture picture, String name) throws IOException {
        FileUtils.copyFile(new File(name), new File(picture.getDestinationName()));

        if (settings.getCaptionLength() != 0) {
            File destinationFile = new File(
                    settings.getPathDestination() + settings.getCaptionFile());
            FileUtils.copyFile(captionFile, destinationFile);
        }
    }

    public void addWorkerListener(WorkerListener listener) {
        listeners.add(listener);
    }

    public void removeWorkerListener(WorkerListener listener) {
        listeners.remove(listener);
    }
}
