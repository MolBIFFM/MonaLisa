/*
 *
 *  This file is part of the software MonaLisa.
 *  MonaLisa is free software, dependent on non-free software. For more information read LICENCE and README.
 *
 *  (c) Department of Molecular Bioinformatics, Institute of Computer Science, Johann Wolfgang
 *  Goethe-University Frankfurt am Main, Germany
 *
 */

package monalisa.addons.annotations;

import java.util.regex.Pattern;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 *
 * @author Jens Einloft
 */
public class MiriamRegistryWrapper {

    private final Logger LOGGER = LogManager.getLogger(MiriamRegistryWrapper.class);
    private final String name;
    private final String url;
    private final String comment;
    private final Pattern pattern;

    public MiriamRegistryWrapper(String name, String url, String comment, Pattern pattern) {
        LOGGER.debug("Creating new MiriamRegistryWrapper for '" + name + "'");
        this.name = name;
        this.url = url;
        this.comment = comment;
        this.pattern = pattern;
    }

    public String getComment() {
        return this.comment;
    }

    public String getURL() {
        return this.url;
    }

    public String getName() {
        return this.name;
    }

    public Pattern getPattern(){
        return this.pattern;
    }

    @Override
    public String toString() {
        return this.name;
    }
}
