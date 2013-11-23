/*
 * Created on 25.11.2007
 *
 */
package ch.codez.autophoto.jai;

import java.awt.RenderingHints;
import java.awt.image.RenderedImage;
import java.awt.image.renderable.ParameterBlock;

import javax.media.jai.CRIFImpl;
import javax.media.jai.ImageLayout;
import javax.media.jai.OpImage;

import com.sun.media.jai.opimage.RIFUtil;

public class ColorEraserCRIF extends CRIFImpl {

    public ColorEraserCRIF() {
        super("ColorEraser");
    }

    public RenderedImage create(ParameterBlock paramBlock, RenderingHints renderHints) {
        // Get ImageLayout from renderHints if any.
        ImageLayout layout = RIFUtil.getImageLayoutHint(renderHints);
        OpImage op = new ColorEraserOperator(paramBlock.getRenderedSource(0), layout, renderHints,
                (int[]) paramBlock.getObjectParameter(0),
                (Integer) paramBlock.getObjectParameter(1),
                (Integer) paramBlock.getObjectParameter(2));

        op = new AreaSmoothOperator(op, layout, renderHints, new int[] { 0, 0, 0 },
                (Integer) paramBlock.getObjectParameter(1),
                (Integer) paramBlock.getObjectParameter(3),
                (Integer) paramBlock.getObjectParameter(4));
        return op;
    }

}
