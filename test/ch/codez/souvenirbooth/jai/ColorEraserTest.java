/*
 * Created on 24.12.2007
 *
 */
package ch.codez.souvenirbooth.jai;

import java.awt.image.BufferedImage;
import java.io.BufferedOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;

import javax.media.jai.JAI;
import javax.media.jai.OperationRegistry;
import javax.media.jai.ParameterBlockJAI;
import javax.media.jai.PlanarImage;
import javax.media.jai.registry.CRIFRegistry;
import javax.media.jai.registry.RIFRegistry;
import javax.media.jai.registry.RenderedRegistryMode;

import ch.codez.autophoto.AppOptions;
import ch.codez.autophoto.jai.ColorEraserCRIF;
import ch.codez.autophoto.jai.ColorEraserDescriptor;

import com.sun.image.codec.jpeg.JPEGCodec;
import com.sun.image.codec.jpeg.JPEGEncodeParam;
import com.sun.image.codec.jpeg.JPEGImageEncoder;

public class ColorEraserTest {

    public static final String SNAPSHOT_FILE = "/Users/pascal/Documents/src/java/SouvenirBooth/tmp.jpg"; // "/Users/pascal/Desktop/elfe.jpg";

    public static final int[] BACKGROUND = new int[] { 210, 240, 235 };

    /**
     * @param args
     */
    public static void main(String[] args) throws IOException {
        registerJAIStuff();

        ColorEraserTest test = new ColorEraserTest();
        long start = System.currentTimeMillis();
        PlanarImage snapshot = test.loadFile(SNAPSHOT_FILE);
        long inter = System.currentTimeMillis();
        System.out.println("Load time: " + (inter - start));
        start = inter;

        PlanarImage mask = test.getMask(snapshot);
        BufferedImage bMask = mask.getAsBufferedImage();
        inter = System.currentTimeMillis();
        System.out.println("Mask time: " + (inter - start));
        start = inter;

        test.save(bMask, "mask.jpg");
        inter = System.currentTimeMillis();
        System.out.println("Save time: " + (inter - start));
    }

    private PlanarImage getMask(PlanarImage snapshot) {
        AppOptions options = AppOptions.getInstance();

        ParameterBlockJAI pb = new ParameterBlockJAI("ColorEraser", RenderedRegistryMode.MODE_NAME);

        pb.setSource("source0", snapshot);

        pb.setParameter("color", options.getCamBackgroundColor());
        pb.setParameter("areaSize", 1);
        pb.setParameter("tolerance", options.getRenderTolerance());
        pb.setParameter("minPercentageForTransparency", options.getRenderOpaqueUpTo());
        pb.setParameter("maxPercentageForOpaquenes", options.getRenderTransparentFrom());

        return JAI.create("ColorEraser", pb, null);
    }

    private void save(BufferedImage image, String file) throws IOException {
        BufferedOutputStream out = new BufferedOutputStream(new FileOutputStream(file));
        JPEGImageEncoder encoder = JPEGCodec.createJPEGEncoder(out);
        JPEGEncodeParam param = encoder.getDefaultJPEGEncodeParam(image);
        param.setQuality(.8F, false);
        // TODO: save photo meta data (date, time)
        encoder.setJPEGEncodeParam(param);
        encoder.encode(image);
        out.close();
    }

    private PlanarImage loadFile(String filename) {
        ParameterBlockJAI pb = new ParameterBlockJAI("fileload", RenderedRegistryMode.MODE_NAME);
        pb.setParameter("filename", filename);
        return JAI.create("fileload", filename);
    }

    private static void registerJAIStuff() {
        OperationRegistry registry = JAI.getDefaultInstance().getOperationRegistry();
        registry.registerDescriptor(new ColorEraserDescriptor());
        ColorEraserCRIF crif = new ColorEraserCRIF();
        RIFRegistry.register(registry, "ColorEraser", "ColorEraser", crif);
        CRIFRegistry.register(registry, "ColorEraser", crif);
    }
}
