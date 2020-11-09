/*
 *
 *  This file is part of the software MonaLisa.
 *  MonaLisa is free software, dependent on non-free software. For more information read LICENCE and README.
 *
 *  (c) Department of Molecular Bioinformatics, Institute of Computer Science, Johann Wolfgang
 *  Goethe-University Frankfurt am Main, Germany
 *
 */
package monalisa.addons.netviewer;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import javax.swing.JTextField;

/**
 * KeyListener for the search field in the SearchBar
 *
 * @author Jens Einloft
 */
public class SearchFieldKeyListener implements KeyListener {

    private final Map<NetViewerNode, String> nvToName;
    private final JTextField owner;
    private final NetViewer nv;

    public SearchFieldKeyListener(NetViewer nv, JTextField owner) {
        this.owner = owner;
        this.nv = nv;

        this.nvToName = new HashMap<>();
        for (NetViewerNode nvNode : nv.getAllVertices()) {
            if (!nvNode.getNodeType().equalsIgnoreCase(NetViewer.BEND)) {
                nvToName.put(nvNode, nvNode.getName());
            }
        }
    }

    @Override
    public void keyReleased(KeyEvent ke) {
        Collection<NetViewerNode> toShow = new ArrayList<>();

        if (owner.getText().isEmpty()) {
            toShow = new ArrayList(this.nvToName.keySet());
        } else {
            // Check every name for match
            for (NetViewerNode nvNode : this.nvToName.keySet()) {
                if (nvNode.getName().toLowerCase().contains(owner.getText().toLowerCase())) {
                    toShow.add(nvNode);
                }
            }
        }

        nv.updateSearchBar(toShow);
    }

    @Override
    public void keyTyped(KeyEvent ke) {
    }

    @Override
    public void keyPressed(KeyEvent ke) {
    }

}
