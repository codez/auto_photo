package ch.codez.autophoto.jai;

import java.awt.RenderingHints;
import java.awt.image.RenderedImage;
import java.awt.image.renderable.ParameterBlock;
import java.awt.image.renderable.RenderedImageFactory;

import javax.media.jai.ImageLayout;
import javax.media.jai.OperationDescriptorImpl;

public class ConnectedComponentsDescriptor extends OperationDescriptorImpl implements
        RenderedImageFactory {

    private static final String[][] resources = {
            { "GlobalName", "ConnectedComponents" },
            { "LocalName", "ConnectedComponents" },
            { "Vendor", "edu.usf.csee" },
            { "Description", "Generate connected components from binary image." },
            { "DocURL",
                    "http://figment.csee.usf.edu/~mpowell/jai/ConnectedComponentsDescriptor.html" },
            { "Version", "beta" }, };

    private static final String[] supportedModes = { "rendered" };

    private static final int numSources = 1;

    private static final String[] paramNames = {};

    private static final Class[] paramClasses = {};

    private static final Object[] paramDefaults = {};

    /**
     * Constructs a ConnectedComponentsDescriptor.
     */
    public ConnectedComponentsDescriptor() {
        super(resources, supportedModes, numSources, paramNames, paramClasses, paramDefaults, null);
    }

    /**
     * creates a ConnectedComponentsOpImage.
     * 
     * @param pb operation parameters.
     * @param hints result image rendering parameters.
     * @return an instance of ConnectedComponentsOpImage.
     */
    public RenderedImage create(ParameterBlock pb, RenderingHints hints) {
        if (!validateParameters(pb)) {
            return null;
        }
        return new ConnectedComponentsOpImage(pb.getRenderedSource(0), new ImageLayout());
    }

    /**
     * validate the parameter types for this operation.
     * 
     * @param pb the operation parameters.
     * @return validation assessment (boolean)
     */
    public boolean validateParameters(ParameterBlock pb) {
        return true;
    }

}