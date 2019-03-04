/*
 *
 *  This file ist part of the software MonaLisa.
 *  MonaLisa is free software, dependend on non-free software. For more information read LICENCE and README.
 *
 *  (c) Molekulare Bioinformatik, Goethe University Frankfurt, Frankfurt am Main, Germany
 *
 */

package monalisa.tools.cluster;

import de.molbi.mjcl.clustering.ds.Properties;
import java.util.Arrays;
import java.util.List;
import monalisa.data.pn.TInvariant;

/**
 *
 * @author Jens Einloft
 */
public class ClusterTreeNodeProperties extends Properties {

    List<TInvariant> tinvs;
    
    public ClusterTreeNodeProperties(TInvariant... tinvs) {
        super();       
        
        this.tinvs = Arrays.asList(tinvs);
    }       
    
    public List<TInvariant> getTinvs() {
        return this.tinvs;
    }
    
}
