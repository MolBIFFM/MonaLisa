/*
 *
 *  This file ist part of the software MonaLisa.
 *  MonaLisa is free software, dependend on non-free software. For more information read LICENCE and README.
 *
 *  (c) Department of Molecular Bioinformatics, Institue of Computer Science, Johann Wolfgang
 *  Goethe-University Frankfurt am Main, Germany
 *
 */

package monalisa.results;

import monalisa.resources.StringResources;

public class MauritiusMapConfiguration implements Configuration {
    private static final long serialVersionUID = 1L;

    @Override
    public boolean equals(Object obj) {
        // There's only one specimen of this configuration, multiple instances
        // all resolve to the same.
        return obj.getClass() == getClass();
    }
    
    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
    
    @Override
    public String toString() {
        return "MauritiusMap";
    }

    @Override
    public String toString(StringResources strings) {
        return strings.get(toString());
    }

    @Override
    public Boolean isExportable() {
        return true;
    }
}
