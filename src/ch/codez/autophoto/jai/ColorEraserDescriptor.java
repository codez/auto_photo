/*
 * Created on 25.11.2007
 *
 */
package ch.codez.autophoto.jai;

import java.awt.RenderingHints;
import java.awt.image.RenderedImage;

import javax.media.jai.JAI;
import javax.media.jai.OperationDescriptorImpl;
import javax.media.jai.ParameterBlockJAI;
import javax.media.jai.RenderedOp;
import javax.media.jai.registry.RenderedRegistryMode;
import javax.media.jai.util.Range;

public class ColorEraserDescriptor extends OperationDescriptorImpl {

    /**
     * The resource strings that provide the general documentation
     * and specify the parameter list for this operation.
     */
    private static final String[][] resources = {
        {"GlobalName",  "ColorEraser"},
        {"LocalName",   "ColorEraser"},
        {"Vendor",      "ch.codez.souvenirbooth"},
        {"Description", "Computes a mask for the given color"},
        {"DocURL",      "http://www.codez.ch"},
        {"Version",     "0.1"},
        {"arg0Desc",    "color to be erased"},
        {"arg1Desc",    "area around each pixel that should be included"},
        {"arg2Desc",    "color tolerance percentage"},
        {"arg3Desc",    "minimum background percentage inducing full transparency"},
        {"arg4Desc",    "maximum background percentage inducing no transparency"},
    };

    /** The parameter class list for this operation. */
    private static final Class[] paramClasses = {
        int[].class, Integer.class, Integer.class, Integer.class, Integer.class
    };

    /** The parameter name list for this operation. */
    private static final String[] paramNames = {
        "color", "areaSize", "tolerance", "minPercentageForTransparency", "maxPercentageForOpaquenes"
    };

    /** The parameter default value list for this operation. */
    private static final Object[] paramDefaults = {
        new int[] {255, 255, 255}, 0, 0, 100, 0 
    };

    private static final String[] supportedModes = {
        "rendered",  "renderable"
    };
    
    private static final Range percentageRange = new Range(Integer.class, 0, 100);
    
    private static final Object[] validParamValues = {
        null, percentageRange, percentageRange, percentageRange, percentageRange
    };

    /** Constructor. */
    public ColorEraserDescriptor() {
        super(resources, supportedModes, 1,
              paramNames, paramClasses, paramDefaults, validParamValues);
    }
    
    public static RenderedOp create(RenderedImage source0,
                                    int[] color,
                                    int areaSize,
                                    int tolerance,
                                    int minPercentageForTransparency,
                                    int maxPercentageForOpaquenes,
                                    RenderingHints hints)  {
        
        ParameterBlockJAI pb = new ParameterBlockJAI("ColorEraser",
                                      RenderedRegistryMode.MODE_NAME);
        
        pb.setSource("source0", source0);
        
        pb.setParameter("color", color);
        pb.setParameter("areaSize", areaSize);
        pb.setParameter("tolerance", tolerance);
        pb.setParameter("minPercentageForTransparency", minPercentageForTransparency);
        pb.setParameter("maxPercentageForOpaquenes", maxPercentageForOpaquenes);
        
        return JAI.create("ColorEraser", pb, hints);
    }
}
