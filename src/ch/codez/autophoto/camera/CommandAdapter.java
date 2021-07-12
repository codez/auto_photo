/*
 * Created on 30.12.2007
 *
 */
package ch.codez.autophoto.camera;

import org.apache.log4j.Logger;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public abstract class CommandAdapter extends CameraAdapter {

    private static Logger log = Logger.getLogger(CommandAdapter.class);
    
    
    public abstract String getCaptureCommand(String filename);
    
    
    public boolean takeSnapshot(String filename) {
        try {
            Process proc = this.getProcess(filename);
            this.logOutput(proc);
            int status = proc.waitFor();
            log.debug("exited proc with status " + status);
            return true;
        } catch (InterruptedException e) {
            log.error("Capture process interrupted", e);
        }
        return false;
    }
    
    public boolean selftest() {
        try {
            Process proc = Runtime.getRuntime().exec(this.getCaptureCommand(null));
            this.logOutput(proc);
            return true;
        } catch (IOException e) { }
        return false;          
    }
    
    protected Process getProcess(String filename) {
        String command = this.getCaptureCommand(filename);
        try {
            return Runtime.getRuntime().exec(command);
        } catch (IOException e) {
            log.error("Could not capture snapshot using command " + command, e);
        }
        return null;
    }
    
    protected void logOutput(Process proc) {
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
