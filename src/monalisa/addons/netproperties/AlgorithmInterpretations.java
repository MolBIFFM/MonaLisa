/*
 *
 *  This file is part of the software MonaLisa.
 *  MonaLisa is free software, dependent on non-free software. For more information read LICENCE and README.
 *
 *  (c) Department of Molecular Bioinformatics, Institute of Computer Science, Johann Wolfgang
 *  Goethe-University Frankfurt am Main, Germany
 *
 */

package monalisa.addons.netproperties;
import java.util.HashMap;

/**
 * a short description of the different net properties. Used in the tooltips.
 * @author daniel
 */


public class AlgorithmInterpretations {
    protected static final HashMap<String,String> HELP = new HashMap<>();

    static {
        HELP.put("ordinary", 
                "<b>Ordinary:</b><br> A Petri net is said to be ordinary, if the multiplicity "
                        + "of every arc equals one [Sta90, Definition 2.3 (24)]<br>");        
               
        HELP.put("homogenous", 
                "<br><br><b>Homogenous:</b><br>A Petri net is said to be homogenous, if for"
                        + " any place p, all arcs starting at p have the same multiplicity [Sta90, Definition14.4 (157)]. "
                        + "Once the required minimum quantity of a tokens on place p is reached, all post-transitions of p are "
                        + "equally likely. In particular, each ordinary Petri net is homogeneous.<br>");
        
        HELP.put("non-blocking multiplicity", 
                "<br><br><b>non-blocking multiplicity:</b><br> A Petri net has non-blocking multiplicity, if for each place p, "
                        + "the minimum of multiplicities of the arcs ending at p is not less than the maximum of multiplicities "
                        + "of the arcs starting at p [Sta90, Definition15.3(164)].<br>");
               
        HELP.put("pure", 
                "<br><br><b>Pure:</b><br>A Petri net is pure, if there is no transition in the current Petri net, "
                        + "for which a pre-place is also a post-place, i.e. it is loop-free [Sta90].<br>");   
        
        HELP.put("conservative", 
                "<br><br><b>Conservative:</b><br>A Petri net is said to be conservative, if all transitions add exactly as "
                        + "many tokens to their post-places as they subtract from their pre-places. In a conservative Petri net, "
                        + "the total number of tokens is thus not altered by firing any transition. Therefore, the amount of "
                        + "tokens is an invariant.[Roh99] <br>");    
        
        HELP.put("subConservative", 
                "<br><br><b>Sub-conservative:</b><br>A Petri net is sub-conservative, if all transitions add at most as many "
                        + "tokens to their post-places as they subtract from their pre-places. The total number of tokens can "
                        + "therefore not increase.<br>");
                
        HELP.put("staticConflictFree", 
                "<br><br><b>Static conflict free:</b><br>If two transitions have a common pre-place, they are in a static conflict "
                        + "about the tokens on this pre-place. Then, the Petri net is not static conflict free. [Sta90, Definition 3.4 (35)]<br>");      
        
        HELP.put("connected", 
                "<br><br><b>Connected:</b><br>A Petri net is said to be connected, if for each node in the net, there exists an "
                        + "undirected path to every other node [Sta90, Definition 14.1 (148)].<br>");  
        
        HELP.put("stronglyConnected", 
                "<br><br><b>Strongly connected:</b><br>If a net is connected, it is checked whether each node also has a directed "
                        + "path to every other node, i.e. the direction of the arcs is also considered [Sta90, Definition14.1 (148)].<br>");
        
        HELP.put("sources", "<br><br><b>[Sta90]</b> Starke, Peter H.: Analyse von Petri-Netz-Modellen. Stuttgart: B.G. Teubner, 1990 (Leitfäden und Monographien der Informatik)"
                            + "<br><br><b>[Roc99]</b> Roch, Stephan and Starke, Peter H.: <a href=\"http://www2.informatik.hu-berlin.de/lehrstuehle/automaten/ina/#manual\">INA Integrated Net analyzer Version2.2 Manual</a>");               
    }
    
    public AlgorithmInterpretations() {
        
    }
}
