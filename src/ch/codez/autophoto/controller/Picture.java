package ch.codez.autophoto.controller;

import java.io.File;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.io.FilenameUtils;

import ch.codez.autophoto.AppOptions;

public class Picture {
    public final static String EXTENSION = "png";

    private File file;

    private List<String> captions;

    public Picture(File file, String caption) {
        this(file, Arrays.asList(caption));
    }

    public Picture(File file, List<String> captions) {
        this.file = file;
        this.captions = captions;
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

    public List<String> getCaptions() {
        return this.captions;
    }
}
