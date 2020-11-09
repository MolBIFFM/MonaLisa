/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package monalisa.results;

import monalisa.resources.StringResources;

/**
 *
 * @author daniel
 */
public final class MInvariantsConfiguration implements Configuration {

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
        return "Manatee Invariants";
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
