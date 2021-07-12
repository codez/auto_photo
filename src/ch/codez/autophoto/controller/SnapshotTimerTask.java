/*
 * Created on 02.12.2007
 *
 */
package ch.codez.autophoto.controller;

import ch.codez.autophoto.AppOptions;
import ch.codez.autophoto.camera.CameraAdapter;
import org.apache.log4j.Logger;

public class SnapshotTimerTask extends DelayedTask {

    private static String FILE_PREFIX = "photo";
    private static Logger log = Logger.getLogger(SnapshotTimerTask.class);

    private static String EXTENSION_SNAPSHOT = "jpg";

    private int sequenceNumber;
    
    private Director director;
   
    public SnapshotTimerTask(Director director, int seq) {
        super(getSnapshotDelay());
        this.sequenceNumber = seq;
        this.director = director;
    }
    
    public void run() {
        if (this.takeSnapshot()) {
            log.debug("Took snapshot");
            this.director.snapshotReady(this.getSnapshotFilename());
        }
    }
   
    private boolean takeSnapshot() {
        CameraAdapter cam = CameraAdapter.getInstance();
        return cam.takeSnapshot(getSnapshotFilename());
    }

    private String getSnapshotFilename() {
        return AppOptions.getInstance().getPathSnapshots() +
                this.createSnapshotFilename();
    }

    private String createSnapshotFilename() {
        return String.format("%s_%04d.%s",
                FILE_PREFIX,
                this.sequenceNumber,
                EXTENSION_SNAPSHOT);
    }

    private static long getSnapshotDelay() {
        AppOptions settings = AppOptions.getInstance();
        return settings.getDirectorCountdown() * 1000 - settings.getCamDelay();
    }

}
