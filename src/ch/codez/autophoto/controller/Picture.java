package ch.codez.autophoto.controller;

import java.io.File;

import org.apache.commons.io.FilenameUtils;
import org.apache.log4j.Logger;

import ch.codez.autophoto.AppOptions;

public class Picture implements Comparable<Picture> {
    public final static String EXTENSION = "png";

    private final static Logger log = Logger.getLogger(Picture.class);

    private File file;

    private String name;

    private String crime;

    public Picture(File file, String name, String crime) {
        this.file = file;
        this.name = name;
        this.crime = crime;
    }

    public String getBaseName() {
        return FilenameUtils.getBaseName(file.getName());
    }

    public String getWorkName() {
        return AppOptions.getInstance().getPathSouvenirs() + getBaseName() + "." + EXTENSION;
    }

    public String getDestinationName() {
        return AppOptions.getInstance().getPathDestination() + getBaseName() + "." + EXTENSION;
    }

    public String getSourceName() {
        return file.getAbsolutePath();
    }

    public File getFile() {
        return this.file;
    }

    public String getName() {
        return this.name;
    }

    public String getCrime() {
        return this.crime;
    }

    public int compareTo(Picture o) {
        return this.getFile().compareTo(o.getFile());
    }

}
