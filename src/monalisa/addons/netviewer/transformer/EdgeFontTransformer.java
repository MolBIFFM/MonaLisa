/*
 *
 *  This file ist part of the software MonaLisa.
 *  MonaLisa is free software, dependend on non-free software. For more information read LICENCE and README.
 *
 *  (c) Molekulare Bioinformatik, Goethe University Frankfurt, Frankfurt am Main, Germany
 *
 */

package monalisa.addons.netviewer.transformer;

import java.awt.Font;
import monalisa.addons.netviewer.NetViewerEdge;
import org.apache.commons.collections15.Transformer;

/**
 *
 * @author Jens Einloft
 */
public class EdgeFontTransformer implements Transformer<NetViewerEdge, Font> {

    private Font font;
    
    public EdgeFontTransformer(Font font) {
        this.font = font;
    }        
    
    public int getFontSize() {
        return this.font.getSize();
    }
    
    public void setFontSize(int fontSize) {
        this.font = new Font("Helvetica", Font.BOLD, fontSize);
    }

    @Override
    public Font transform(NetViewerEdge k) {
        return this.font;
    }
    
}
