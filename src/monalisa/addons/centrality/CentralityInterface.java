/*
 *
 *  This file ist part of the software MonaLisa.
 *  MonaLisa is free software, dependend on non-free software. For more information read LICENCE and README.
 *
 *  (c) Department of Molecular Bioinformatics, Institue of Computer Science, Johann Wolfgang
 *  Goethe-University Frankfurt am Main, Germany
 *
 */

package monalisa.addons.centrality;

import java.util.Map;

/**
 * Interface for calculating centralities of nodes in a Petri net
 * @author Lilya Mirzoyan
 */
public interface CentralityInterface {
    
    public void calculate();  
    public Map<Integer, Double> getRankingForPlaces();    
    public Map<Integer, Double> getRankingForTransitions();    
   
}
