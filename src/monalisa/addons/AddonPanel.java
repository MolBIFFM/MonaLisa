/*
 *
 *  This file is part of the software MonaLisa.
 *  MonaLisa is free software, dependent on non-free software. For more information read LICENCE and README.
 *
 *  (c) Department of Molecular Bioinformatics, Institute of Computer Science, Johann Wolfgang
 *  Goethe-University Frankfurt am Main, Germany
 *
 */
package monalisa.addons;

import java.util.HashMap;
import java.util.Map;
import monalisa.addons.netviewer.NetViewer;
import monalisa.addons.netviewer.listener.NetChangedListener;
import monalisa.data.pn.PetriNetFacade;
import monalisa.resources.ResourceManager;
import monalisa.resources.StringResources;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Basic Panel for AddOns. Provides some basic functions like the registration
 * of the tab of the NetViewer ToolBar. Also registers the AddOn in the data
 * storage management.
 *
 * @author jens
 */
public class AddonPanel extends javax.swing.JPanel implements NetChangedListener, AddonStorageManagment {

    private static final Logger LOGGER = LogManager.getLogger(AddonPanel.class);
    protected static final ResourceManager resources = ResourceManager.instance();
    protected static final StringResources strings = resources.getDefaultStrings();

    protected NetViewer netViewer;
    protected PetriNetFacade pnf;
    protected String addonName;

    /**
     * Creates new form AddonPanel and add it to the ToolBar
     */
    public AddonPanel(NetViewer netViewer, PetriNetFacade pnf, String addonName) {
        LOGGER.info("Adding '" + addonName + "' to ToolBar");
        this.netViewer = netViewer;
        this.pnf = pnf;
        this.addonName = addonName;

        this.netViewer.addTabToMenuBar(addonName, this);

        this.netViewer.addNetChangedListener(this);
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 400, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 300, Short.MAX_VALUE)
        );
    }// </editor-fold>//GEN-END:initComponents
    // Variables declaration - do not modify//GEN-BEGIN:variables
    // End of variables declaration//GEN-END:variables

    /**
     * Returns the name of the AddOn.
     *
     * @return The name of the AddOn
     */
    public String getAddOnName() {
        return this.addonName;
    }

    /**
     * Is called from the NetViewer, if the Petri net is changed. Should be used
     * to reset the AddOn.
     */
    @Override
    public void netChanged() {
    }

    /**
     * Is called from the Project class to get all data, which should be saved
     * for the AddOn. All kind of objects can be stored here, if the class
     * implements the "Serializable" interface. The string is an identifier if
     * more than one object should be saved.
     *
     * @return A map with the data to store.
     */
    @Override
    public Map<String, Object> getObjectsForStorage() {
        return new HashMap<>();
    }

    /**
     * Is called to send the stored data to the AddOn. It will get the map which
     * is saved with getObjectsForStorage() method.
     *
     * @param storage
     */
    @Override
    public void receiveStoredObjects(Map<String, Object> storage) {

    }
}
