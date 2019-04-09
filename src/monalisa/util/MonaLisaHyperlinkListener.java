/*
 *
 *  This file is part of the software MonaLisa.
 *  MonaLisa is free software, dependent on non-free software. For more information read LICENCE and README.
 *
 *  (c) Department of Molecular Bioinformatics, Institute of Computer Science, Johann Wolfgang
 *  Goethe-University Frankfurt am Main, Germany
 *
 */

package monalisa.util;

import java.awt.Desktop;
import java.io.IOException;
import java.net.URISyntaxException;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

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
                } catch (IOException | URISyntaxException ex) {
                    LogManager.getLogger(MonaLisaHyperlinkListener.class)
                            .error("Caught exception while trying to make Hyperlink clickable in JEditorPane: ", ex);
                }
        }
    }

}
