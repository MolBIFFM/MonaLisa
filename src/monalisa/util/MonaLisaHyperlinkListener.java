/*
 *
 *  This file ist part of the software MonaLisa.
 *  MonaLisa is free software, dependend on non-free software. For more information read LICENCE and README.
 *
 *  (c) Department of Molecular Bioinformatics, Institue of Computer Science, Johann Wolfgang
 *  Goethe-University Frankfurt am Main, Germany
 *
 */

package monalisa.util;

import java.awt.Desktop;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;

/**
 * Make hyperlinks in JEditorPane clickable
 * @author Jens Einloft
 */
public class MonaLisaHyperlinkListener implements HyperlinkListener {

    @Override
    public void hyperlinkUpdate(HyperlinkEvent hle) {
        if (HyperlinkEvent.EventType.ACTIVATED.equals(hle.getEventType())) {
            if(Desktop.isDesktopSupported())
                try {
                    Desktop.getDesktop().browse(hle.getURL().toURI());
                } catch (    IOException | URISyntaxException ex) {
                    Logger.getLogger(MonaLisaHyperlinkListener.class.getName()).log(Level.SEVERE, null, ex);
                }
        }
    }

}
