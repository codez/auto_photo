package ch.codez.autophoto.jai;

import java.awt.RenderingHints;
import java.awt.image.RenderedImage;
import java.awt.image.renderable.ParameterBlock;
import java.awt.image.renderable.RenderedImageFactory;

import javax.media.jai.ImageLayout;
import javax.media.jai.OperationDescriptorImpl;

public class ComponentsSelectorDescriptor extends OperationDescriptorImpl implements
        RenderedImageFactory {

    private static final String[][] resources = { { "GlobalName", "ComponentsSelector" },
            { "LocalName", "ComponentsSelector" }, { "Vendor", "codez.ch" },
            { "Description", "Select connected components from binary image." },
            { "DocURL", "http://www.codez.ch" }, { "Version", "beta" },
            { "arg0Desc", "connected components" },
            { "arg1Desc", "min percent of the biggest component to select" } };

    private static final String[] supportedModes = { "rendered" };

    private static final int numSources = 1;

    private static final String[] paramNames = { "components", "minPercent" };

    private static final Class[] paramClasses = { ConnectedComponent[].class, Integer.class };

    private static final Object[] paramDefaults = { null, 50 };

    /**
     * Constructs a ConnectedComponentsDescriptor.
     */
    public ComponentsSelectorDescriptor() {
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

        return new ComponentSelectorOperator(pb.getRenderedSource(0), new ImageLayout(),
                (ConnectedComponent[]) pb.getObjectParameter(0), pb.getIntParameter(1));
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