/*
 * Created on 30.12.2007
 *
 */
package ch.codez.autophoto.camera;

import org.apache.log4j.Logger;

import java.io.File;

public class GPhotoAdapter extends CommandAdapter {

    private static Logger log = Logger.getLogger(GPhotoAdapter.class);
    
    private final String GPHOTO_CMD = "/usr/local/bin/gphoto2 ";
    
    private final String GPHOTO_CAPTURE_CMD = GPHOTO_CMD + " --capture-image-and-download -F 1 -I 1";

    private final String GPHOTO_TEST_CMD = GPHOTO_CMD + "--summary";
    
    private final String SNAPSHOT_FILE = "capt0000.jpg";
    
    
    public String getCaptureCommand(String filename) {
        if (filename != null) {
            return GPHOTO_CAPTURE_CMD;
        } else {
            return GPHOTO_TEST_CMD;
        }
    }
    
    public boolean takeSnapshot(String filename) {
        boolean success = super.takeSnapshot(filename);
        if (success) {
            File snapshot = new File(SNAPSHOT_FILE);
            File target = new File(filename);
            success = snapshot.renameTo(target);
        }
        return success;
    }
    
    public boolean isRandom() {
        return false;
    }
    
}
