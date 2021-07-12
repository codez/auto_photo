/*
 * Created on 03.12.2007
 *
 */
package ch.codez.autophoto.camera;

import org.apache.log4j.Logger;

import java.awt.image.BufferedImage;
import java.io.*;

public class ISightCaptureAdapter extends CameraAdapter {

    private static Logger log = Logger.getLogger(ISightCaptureAdapter.class);
    
    private final String COMMAND;
    
    public ISightCaptureAdapter() {
        String dir = System.getProperty("user.dir");
        COMMAND = dir + File.separator + "isightcapture ";
    }
    
    public boolean takeSnapshot(String filename) {
        String command = COMMAND + "-t tiff " + filename;
        try {
            Process proc = Runtime.getRuntime().exec(command);
            this.logOutput(proc);
            return true;
        } catch (IOException e) {
            log.error("Could not capture snapshot using command " + command, e);
        }
        return false;
    }

    public BufferedImage takeSnapshot() {
        return null;
    }

    public boolean selftest() {
        try {
            Process proc = Runtime.getRuntime().exec(COMMAND);
            this.logOutput(proc);
            return true;
        } catch (IOException e) { }
        return false;          
    }
    
    private void logOutput(Process proc) {
        if (log.isDebugEnabled()) {
            this.logStream(proc.getInputStream());
            this.logStream(proc.getErrorStream());
        }
    }

    private void logStream(InputStream stream) {
        BufferedReader reader = new BufferedReader(new InputStreamReader(stream));
        String line = null;
        try {
            while ((line = reader.readLine()) != null) {
                log.debug(line);
            }
        } catch (IOException e) {
            log.debug("Could not debug stream", e);
        }
    }
}
