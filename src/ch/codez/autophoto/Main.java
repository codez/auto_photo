/*
 * Created on 13.11.2007
 *
 */
package ch.codez.autophoto;

import java.io.File;

import org.apache.log4j.Logger;

import ch.codez.autophoto.gui.BoothFrame;

public class Main {

    private static Logger log = Logger.getLogger(Main.class);

    public static void main(String[] args) throws Exception {
        assertSettings();
        setOSXOptions();
        createPictureDirectories();

        BoothFrame frame = new BoothFrame();
        frame.runFullScreen();
    }

    private static void setOSXOptions() {
        // System.setProperty("apple.awt.fakefullscreen", "true");
        // System.setProperty("apple.awt.fullscreenusefade", "true");
        System.setProperty("apple.laf.useScreenMenuBar", "true");
    }

    private static void createPictureDirectories() {
        AppOptions settings = AppOptions.getInstance();
        assertDirectoryExistance(settings.getPathSnapshots());
        assertDirectoryExistance(settings.getPathSouvenirs());
    }

    private static void assertDirectoryExistance(String path) {
        File dir = new File(path);
        if (!dir.exists()) {
            boolean success = dir.mkdirs();
            if (!success) {
                log.error("Could not create directory " + path);
            } else {
                log.info("Created directory " + path);
            }
        }
    }

    private static void assertSettings() {
        assertExistingProperties();
    }

    private static void assertExistingProperties() {
        File properties = new File(AppOptions.CONFIG_FILE);
        if (!properties.exists()) {
            System.out.println("No settings file defined (" + properties.getName() + ").");
            // System.exit(1);
        }
    }

}
