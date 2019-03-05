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

import java.awt.Component;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.ListCellRenderer;

/**
 * Renderer to show the LabelName of a node and not his name.
 * @author Jens Einloft
 */
public class LogicalPlacesListCellRenderer implements ListCellRenderer {

    @Override
    public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
        NetViewerNode node = (NetViewerNode)value;
        DefaultListCellRenderer defaultRenderer = new DefaultListCellRenderer();
        JLabel renderer = (JLabel)defaultRenderer.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
        if(node.getLogicalPlaces().size() < 2 || node.getNodeType().equalsIgnoreCase((NetViewer.TRANSITION)))
            renderer.setText(node.getName());
        else
            renderer.setText(node.getName()+" ("+node.getLogicalPlaces().size()+" logical species)");
        renderer.setForeground(node.getSearchBarColor());
        return renderer;
    }
}
