/*
 *
 *  This file ist part of the software MonaLisa.
 *  MonaLisa is free software, dependend on non-free software. For more information read LICENCE and README.
 *
 *  (c) Department of Molecular Bioinformatics, Institue of Computer Science, Johann Wolfgang
 *  Goethe-University Frankfurt am Main, Germany
 *
 */

package monalisa.addons.netviewer.wrapper;

import java.io.Serializable;
import java.util.List;
import monalisa.addons.netviewer.NetViewerNode;
import monalisa.results.TInvariants;

/**
 *
 * @author Jens
 */
public class SISWrapper implements Serializable {
    private static final long serialVersionUID = -1653518945518597091L;
    
    private TInvariants sis;
    private List<NetViewerNode> originalTransitions;
    private List<NetViewerNode> addedTransitions;
    private String name = "";

    public SISWrapper(TInvariants tinvariants, List<NetViewerNode> originalTransitions, List<NetViewerNode> addedTransitions) {
        this.sis = tinvariants;
        this.originalTransitions = originalTransitions;
        this.addedTransitions = addedTransitions;
        
        for(NetViewerNode n : originalTransitions)
            name += n.getName()+"+";
        if(!addedTransitions.isEmpty()) {
            name += "[";
            for(NetViewerNode n : addedTransitions)
                name += n.getName()+"+";
            name = name.substring(0,name.length()-1)+"]";
        } else
            name = name = name.substring(0,name.length()-1);
    }

    public SISWrapper(TInvariants tinvariants, List<NetViewerNode> transitions, List<NetViewerNode> addedTransitions, String name) {
        this.sis = tinvariants;
        this.originalTransitions = transitions;
        this.name = name;
    }

    public TInvariants getSIS() {
        return sis;
    }
    
    public List<NetViewerNode> getOriginalTransitions() {
        return originalTransitions;
    }

    public List<NetViewerNode> getAddedTransitions() {
        return addedTransitions;
    }
        
    @Override
    public String toString() {
        return name;
    }
}
