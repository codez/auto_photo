/*
 * Created on 03.12.2007
 *
 */
package ch.codez.autophoto.camera;

import ch.codez.autophoto.AppOptions;
import org.apache.log4j.Logger;

public abstract class CameraAdapter {

    private static Logger log = Logger.getLogger(CameraAdapter.class);
    
    public abstract boolean takeSnapshot(String filename);
    
    public abstract boolean selftest();
    
    public boolean isRandom() {
        return false;
    }
    
    public static CameraAdapter getInstance() {
        ClassLoader loader = CameraAdapter.class.getClassLoader();
        String clazz = AppOptions.getInstance().getCamAdapterClass();
        
        try {
            Class adapter = loader.loadClass(clazz);
            Object instance = adapter.getDeclaredConstructor().newInstance();
            return (CameraAdapter)instance;
        } catch (Exception e) {
            log.error("Could not load class " + clazz, e);
        }
        return null;
    }
}
