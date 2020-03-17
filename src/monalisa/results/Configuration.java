/*
 *
 *  This file is part of the software MonaLisa.
 *  MonaLisa is free software, dependent on non-free software. For more information read LICENCE and README.
 *
 *  (c) Department of Molecular Bioinformatics, Institute of Computer Science, Johann Wolfgang
 *  Goethe-University Frankfurt am Main, Germany
 *
 */

package monalisa.results;

import java.io.Serializable;

import monalisa.resources.StringResources;

/**
 * A configuration of a given tool that produces a given, reproducible result.
 * The configuration should fully identify the result data (apart from the
 * <em>fixed</em> input data, i.e. the Petri net etc.).
 * @author Konrad Rudolph
 */
public interface Configuration extends Serializable {
    static final long serialVersionUID = 4659959647780553777L;

    @Override
    boolean equals(Object obj);
    
    @Override
    int hashCode();
    
    @Override
    String toString();

    Boolean isExportable();

    /**
     * Returns a properly localized description of the configuration.
     * @param strings The localized string resources.
     * @return A string containing the localized description.
     */
    String toString(StringResources strings);
}
