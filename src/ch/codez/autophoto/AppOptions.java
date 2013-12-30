/*
 * Created on 21.11.2007
 *
 */
package ch.codez.autophoto;

import java.awt.Color;
import java.io.File;

import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.apache.commons.configuration.reloading.FileChangedReloadingStrategy;
import org.apache.log4j.Logger;

public class AppOptions {

    public final static String CONFIG_FILE = "autophoto.properties";

    private static AppOptions INSTANCE = new AppOptions();

    private static Logger log = Logger.getLogger(AppOptions.class);

    private PropertiesConfiguration config;

    private int sequence = 0;

    public static AppOptions getInstance() {
        return INSTANCE;
    }

    private AppOptions() {
        this.initConfig();
        this.sequence = this.config.getInt("picture.lastseq", 0);
    }

    public int getNextSequence() {
        this.sequence++;
        this.save();
        return this.sequence;
    }

    public boolean getIsKioskMode() {
        return this.config.getBoolean("kiosk.mode", false);
    }

    public boolean getIsFakeFullscreen() {
        return this.config.getBoolean("fake.fullscreen", true);
    }

    public int[] getCamBackgroundColor() {
        return this.getColor("cam.bgcolor", "#FFFFFF");
    }

    public long getDirectorWorkerDelay() {
        return this.config.getLong("director.workerdelay", 2000);
    }

    public int getCaptionLength() {
        return this.config.getInt("director.captionLength", -1);
    }

    public String getCaptionFile() {
        return this.config.getString("director.captionFile", "captions.json");
    }

    public float getPictureQuality() {
        return this.config.getFloat("picture.quality");
    }

    public boolean getMaskImage() {
        return this.config.getBoolean("render.mask", true);
    }

    public int getRenderArea() {
        return this.config.getInt("render.area", 2);
    }

    public int getRenderTolerance() {
        return this.config.getInt("render.tolerance", 10);
    }

    public int getRenderOpaqueUpTo() {
        return this.config.getInt("render.opaqueUpTo", 20);
    }

    public int getRenderTransparentFrom() {
        return this.config.getInt("render.transparentFrom", 60);
    }

    public int getMinComponentPercent() {
        return this.config.getInt("render.minComponentPercent", 10);
    }

    public int getCutRadiusPercent() {
        return this.config.getInt("render.cutRadiusPercent", 100);
    }

    public int getMaxWidth() {
        return this.config.getInt("render.maxWidth", 0);
    }

    public float getSideRatio() {
        return this.config.getFloat("render.sideRatio", 0);
    }

    public String getPathSnapshots() {
        return this.getPath("path.snapshots");
    }

    public String getPathSouvenirs() {
        return this.getPath("path.masked");
    }

    public String getPathDestination() {
        return this.getPath("path.destination");
    }

    public Color getLafColorHighlight() {
        int[] color = this.getColor("laf.color.highlight", "#ff70aa");
        return new Color(color[0], color[1], color[2]);
    }

    public String getLafMessageMain() {
        return this.config.getString("laf.message.main");
    }

    private void save() {
        this.config.setProperty("picture.lastseq", new Integer(this.sequence));
        try {
            this.config.save();
        } catch (ConfigurationException e) {
            log.warn("Could not save configuration.");
        }
    }

    private String getPath(String key) {
        String path = this.config.getString(key, ".");
        if (!path.endsWith(File.separator)) {
            path += File.separator;
        }
        return path;
    }

    private int[] getColor(String key, String def) {
        String bg = this.config.getString(key, def);
        int color[] = new int[3];
        int index = ('#' == bg.charAt(0)) ? 1 : 0;
        for (int i = 0; i < 3; i++) {
            color[i] = Integer.parseInt(bg.substring(index, index + 2), 16);
            index += 2;
        }
        return color;
    }

    private void initConfig() {
        PropertiesConfiguration config;
        try {
            config = new PropertiesConfiguration(CONFIG_FILE);
            config.setReloadingStrategy(new FileChangedReloadingStrategy());
        } catch (ConfigurationException e) {
            log.error("Configuration file " + CONFIG_FILE + " not found!", e);
            config = new PropertiesConfiguration();
        }
        this.config = config;
    }

}
