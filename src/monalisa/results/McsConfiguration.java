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

import monalisa.data.pn.Transition;
import monalisa.resources.StringResources;

/**
 *
 * @author Jens Einloft
 */
public class McsConfiguration implements Configuration {    
    private static final long serialVersionUID = 3532262047201708171L;
    
    private final Transition objective;
    private final int maxCutSetSize;
    
    public McsConfiguration(Transition objective, int maxCutSetSize) {
        this.objective = objective;        
        this.maxCutSetSize = maxCutSetSize;
    }
    
    public Transition getObjective() {
        return this.objective;
    }
    
    @Override
    public Boolean isExportable() {
        return true;
    }

    @Override
    public String toString() {
        return String.format("MinimalCutSet-from-%s-max-size-%d", this.objective.getProperty("name"), this.maxCutSetSize);
    }

    @Override
    public String toString(StringResources strings) {
        return strings.get(toString());
    }   

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        
        final McsConfiguration other = (McsConfiguration) obj;        
        return this.maxCutSetSize == other.maxCutSetSize && this.objective.equals(other.objective);                
    }           
}
