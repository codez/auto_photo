package ch.codez.autophoto.controller;

import java.io.File;
import java.util.Arrays;
import java.util.List;

public class Picture {
    private File file;

    private List<String> captions;

    public Picture(File file, String caption) {
        this(file, Arrays.asList(caption));
    }

    public Picture(File file, List<String> captions) {
        this.file = file;
        this.captions = captions;
    }

    public File getFile() {
        return this.file;
    }

    public List<String> getCaptions() {
        return this.captions;
    }
}
