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

import monalisa.addons.netviewer.wrapper.SISWrapper;
import edu.uci.ics.jung.visualization.control.AbstractPopupGraphMousePlugin;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.MouseEvent;
import java.io.IOException;
import javax.swing.AbstractAction;
import javax.swing.DefaultListModel;
import javax.swing.JFileChooser;
import javax.swing.JPopupMenu;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import monalisa.resources.ResourceManager;
import monalisa.resources.StringResources;
import monalisa.results.TInvariantsConfiguration;
import monalisa.synchronisation.Synchronizer;
import monalisa.util.MonaLisaFileChooser;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * SearchBar Tab in the ToolBar.
 * @author Jens Einloft
 */
public class SearchBar extends javax.swing.JFrame {
    private static final long serialVersionUID = 428469061686606820L;
    private static final Logger LOGGER = LogManager.getLogger(SearchBar.class);

    /** Creates new form SearchBar */
    public SearchBar(final NetViewer netViewer, Synchronizer synchronizer) {
        this.netViewer = netViewer;
        LOGGER.info("Initializing SearchBar");
        initComponents();

        allPlacesList.setName(NetViewer.PLACE);
        allPlacesList.addMouseListener(new SearchBarPopupMousePlugin(this.netViewer, synchronizer, allPlacesList));
        allPlacesList.setCellRenderer(new LogicalPlacesListCellRenderer());
        // If a place in the search bar is selected, he (and all logical places) will select it in the NetViewer, too
        allPlacesList.addListSelectionListener(new ListSelectionListener() {
            @Override
            public void valueChanged(ListSelectionEvent e) {
                LOGGER.debug("Selecting place in NetViewer as well");
                netViewer.vv.getRenderContext().getPickedVertexState().clear();
                for(Object obj : allPlacesList.getSelectedValuesList()) {
                    NetViewerNode nvNode = (NetViewerNode)obj;
                    for(NetViewerNode n : nvNode.getLogicalPlaces())
                        netViewer.vv.getRenderContext().getPickedVertexState().pick(n, true);
                }
            }
        });

        allTransitionsList.setName(NetViewer.TRANSITION);
        allTransitionsList.addMouseListener(new SearchBarPopupMousePlugin(this.netViewer, synchronizer, allTransitionsList));
        allTransitionsList.setCellRenderer(new LogicalPlacesListCellRenderer());
        // If a transition in the search bar is selected, he will select it in the NetViewer, too
        allTransitionsList.addListSelectionListener(new ListSelectionListener() {
            @Override
            public void valueChanged(ListSelectionEvent e) {
                LOGGER.debug("Selecting Transition in NetViewer as well");
                netViewer.vv.getRenderContext().getPickedVertexState().clear();
                for(Object obj : allTransitionsList.getSelectedValuesList()) {
                    netViewer.vv.getRenderContext().getPickedVertexState().pick((NetViewerNode)obj, true);
                }
            }
        });
        LOGGER.info("Successfully initialized SearchBar");
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        jScrollPane1 = new javax.swing.JScrollPane();
        mainSP = new javax.swing.JScrollPane();
        mainPanel = new javax.swing.JPanel();
        transitionsLabel = new javax.swing.JLabel();
        placesLabel = new javax.swing.JLabel();
        doublePlacesWarning = new javax.swing.JLabel();
        doubleTransitionsWarning = new javax.swing.JLabel();
        searchTextField = new javax.swing.JTextField();
        searchLabel = new javax.swing.JLabel();
        allTransitionsSp = new javax.swing.JScrollPane();
        allTransitionsList = new javax.swing.JList();
        allPlacesSp = new javax.swing.JScrollPane();
        allPlacesList = new javax.swing.JList();

        setTitle("SearchBar");
        setBounds(new java.awt.Rectangle(460, 550, 460, 550));
        setIconImage(resources.getImage("icon-16.png"));
        setName("SearchBar"); // NOI18N

        mainSP.setPreferredSize(new java.awt.Dimension(276, 532));

        mainPanel.setBorder(javax.swing.BorderFactory.createEmptyBorder(1, 1, 1, 1));
        mainPanel.setPreferredSize(new java.awt.Dimension(274, 525));
        mainPanel.setLayout(new java.awt.GridBagLayout());

        transitionsLabel.setText("Reactions");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 0, 5);
        mainPanel.add(transitionsLabel, gridBagConstraints);

        placesLabel.setText("Compounds");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 0, 5);
        mainPanel.add(placesLabel, gridBagConstraints);

        doublePlacesWarning.setForeground(java.awt.Color.red);
        doublePlacesWarning.setText("C");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(2, 5, 0, 5);
        mainPanel.add(doublePlacesWarning, gridBagConstraints);

        doubleTransitionsWarning.setForeground(java.awt.Color.red);
        doubleTransitionsWarning.setText("R");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(2, 5, 0, 5);
        mainPanel.add(doubleTransitionsWarning, gridBagConstraints);

        searchTextField.addKeyListener(new SearchFieldKeyListener(this.netViewer, searchTextField));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 7;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(2, 5, 0, 5);
        mainPanel.add(searchTextField, gridBagConstraints);

        searchLabel.setText("Search");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 6;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 0, 5);
        mainPanel.add(searchLabel, gridBagConstraints);

        allTransitionsList.setModel(new DefaultListModel());
        allTransitionsSp.setViewportView(allTransitionsList);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 5;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(2, 5, 0, 5);
        mainPanel.add(allTransitionsSp, gridBagConstraints);

        allPlacesList.setModel(new DefaultListModel()
        );
        allPlacesSp.setViewportView(allPlacesList);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(2, 5, 0, 5);
        mainPanel.add(allPlacesSp, gridBagConstraints);

        mainSP.setViewportView(mainPanel);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(mainSP, javax.swing.GroupLayout.DEFAULT_SIZE, 300, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(mainSP, javax.swing.GroupLayout.DEFAULT_SIZE, 600, Short.MAX_VALUE)
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    // Variables declaration - do not modify//GEN-BEGIN:variables
    protected javax.swing.JList allPlacesList;
    private javax.swing.JScrollPane allPlacesSp;
    protected javax.swing.JList allTransitionsList;
    private javax.swing.JScrollPane allTransitionsSp;
    protected javax.swing.JLabel doublePlacesWarning;
    protected javax.swing.JLabel doubleTransitionsWarning;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JPanel mainPanel;
    private javax.swing.JScrollPane mainSP;
    protected javax.swing.JLabel placesLabel;
    private javax.swing.JLabel searchLabel;
    private javax.swing.JTextField searchTextField;
    protected javax.swing.JLabel transitionsLabel;
    // End of variables declaration//GEN-END:variables

    private static final ResourceManager resources = ResourceManager.instance();
    private static final StringResources strings = resources.getDefaultStrings();

    private final NetViewer netViewer;
}
