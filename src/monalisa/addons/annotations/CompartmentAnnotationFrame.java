/*
 *
 *  This file is part of the software MonaLisa.
 *  MonaLisa is free software, dependent on non-free software. For more information read LICENCE and README.
 *
 *  (c) Department of Molecular Bioinformatics, Institute of Computer Science, Johann Wolfgang
 *  Goethe-University Frankfurt am Main, Germany
 *
 */

package monalisa.addons.annotations;

import java.awt.Component;
import java.awt.Desktop;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.swing.DefaultComboBoxModel;
import javax.swing.DefaultListModel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import static monalisa.addons.annotations.AnnotationsPanel.MIRIAM_BIO_QUALIFIERS;
import static monalisa.addons.annotations.AnnotationsPanel.SBO_TERM;
import monalisa.data.pn.Compartment;
import monalisa.resources.ResourceManager;
import monalisa.resources.StringResources;
import monalisa.util.ComboboxToolTipRenderer;
import monalisa.util.MonaLisaHyperlinkListener;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.jdom2.input.SAXBuilder;
import org.sbml.jsbml.CVTerm;
import org.sbml.jsbml.CVTerm.Qualifier;

/**
 *
 * @author jens
 */
public class CompartmentAnnotationFrame extends javax.swing.JFrame {

    private static final ResourceManager resources = ResourceManager.instance();
    private static final StringResources strings = resources.getDefaultStrings();
    private static final Logger LOGGER = LogManager.getLogger(CompartmentAnnotationFrame.class);

    private final Component parent;
    private final Compartment compartment;

    private DefaultListModel<MiriamWrapper> miModel;

    protected Map<String, Integer> miriamRegistryMap = new HashMap<>();

    private boolean editMiriamIdentifier;
    private MiriamWrapper identifierInEdit;


    /**
     * Creates new form CompartmentAnnotationFrame
     */
    public CompartmentAnnotationFrame(Component parent, Compartment c) {
        LOGGER.info("Initializing CompartmentAnnotationFrame");
        this.compartment = c;
        this.parent = parent;

        setLocationRelativeTo(parent);
        setTitle("Compartment Annotation");
        setIconImage(resources.getImage("icon-16.png"));

        initComponents();

        compartmentLabel.setText(compartment.getName());

        // START: MIRIAM
        LOGGER.debug("Starting MIRIAM part");
        miModel = (DefaultListModel<MiriamWrapper>)miriamIdentifiers.getModel();
        qualifier.addItem(CVTerm.Qualifier.BQB_ENCODES);
        qualifier.addItem(CVTerm.Qualifier.BQB_HAS_PART);
        qualifier.addItem(CVTerm.Qualifier.BQB_HAS_PROPERTY);
        qualifier.addItem(CVTerm.Qualifier.BQB_HAS_TAXON);
        qualifier.addItem(CVTerm.Qualifier.BQB_HAS_VERSION);
        qualifier.addItem(CVTerm.Qualifier.BQB_IS);
        qualifier.addItem(CVTerm.Qualifier.BQB_IS_DESCRIBED_BY);
        qualifier.addItem(CVTerm.Qualifier.BQB_IS_ENCODED_BY);
        qualifier.addItem(CVTerm.Qualifier.BQB_IS_HOMOLOG_TO);
        qualifier.addItem(CVTerm.Qualifier.BQB_IS_PART_OF);
        qualifier.addItem(CVTerm.Qualifier.BQB_IS_PROPERTY_OF);
        qualifier.addItem(CVTerm.Qualifier.BQB_IS_VERSION_OF);
        qualifier.addItem(CVTerm.Qualifier.BQB_OCCURS_IN);
        qualifier.addItem(CVTerm.Qualifier.BQB_UNKNOWN);

        if(compartment.hasProperty(MIRIAM_BIO_QUALIFIERS)) {
            int childCount;
            for(CVTerm cvt : (List<CVTerm>)compartment.getProperty(MIRIAM_BIO_QUALIFIERS)) {
                childCount =  cvt.getChildCount();
                if(childCount > 1) {
                    for(int i=0; i < cvt.getChildCount(); i++) {
                        miModel.addElement(new MiriamWrapper(cvt.getBiologicalQualifierType(), cvt.getChildAt(i).toString()));
                    }
                } else {
                    miModel.addElement(new MiriamWrapper(cvt));
                }
            }
        }
        LOGGER.debug("Finished MIRIAM part");
        LOGGER.debug("Starting SBO part");
        // START: SBO
        ComboboxToolTipRenderer sboCbRenderer = new ComboboxToolTipRenderer();
        sboCb.setRenderer(sboCbRenderer);
        ArrayList<String> sboToolTips = new ArrayList<>();

        SAXBuilder builder = new SAXBuilder();
        Document doc = null;
        try {
            LOGGER.debug("Reading from 'SBO_XML.xml'");
            URL sboURL = ResourceManager.instance().getResourceUrl("SBO_XML.xml");
            InputStream istream = sboURL.openStream();
            doc = builder.build(istream);
        }
        catch (IOException | JDOMException ex) {
            LOGGER.error(ex);
        }
        Element root = doc.getRootElement();
        Element e;
        sboCb.addItem("No Term set");
        sboToolTips.add("");
        LOGGER.debug("Adding SBO tooltips to compartment");
        for(Object o : root.getChildren() ) {
            e = (Element) o;
            sboCb.addItem(((Element)e.getContent().get(1)).getValue());
            sboToolTips.add(((Element)e.getContent().get(3)).getValue()+" : "+((Element)e.getContent().get(7)).getValue().trim());
        }
        sboCbRenderer.setTooltips(sboToolTips);
        LOGGER.debug("Finished SBO part");
        // START: MIRIAM registry
        LOGGER.debug("Starting MIRIAM registry part");
        ComboboxToolTipRenderer miriamRegistryCbRenderer = new ComboboxToolTipRenderer();
        miriamRegistry.setRenderer(miriamRegistryCbRenderer);
        ArrayList<String> miriamRegistryToolTips = new ArrayList<>();

        builder = new SAXBuilder();
        try {
            LOGGER.debug("Reading from 'miriam_registry.xml'");
            URL sboURL = ResourceManager.instance().getResourceUrl("miriam_registry.xml");
            InputStream istream = sboURL.openStream();
            doc = builder.build(istream);
        }
        catch (IOException | JDOMException ex) {
            LOGGER.error(ex);
        }
        root = doc.getRootElement();
        MiriamRegistryWrapper mrw;
        Integer counter = 0;
        String name, comment, url = "";
        Pattern pattern;
        LOGGER.debug("Adding MIRIAM URLs to compartment");
        for(Object o : root.getChildren() ) {
            e = (Element) o;

            if(e.getAttribute("obsolete") != null)
                continue;
            if(e.getName().equals("listOfTags"))
                continue;

            pattern = Pattern.compile(e.getAttributeValue("pattern"));

            name = e.getChild("name", e.getNamespace()).getValue();
            comment = e.getChild("definition", e.getNamespace()).getValue();

            Element uri;
            for(Object u : e.getChild("uris", e.getNamespace()).getChildren()) {
                uri = (Element) u;

                if(uri.getAttributeValue("type").equals("URL") && uri.getAttribute("deprecated") == null) {
                    url = uri.getValue();
                    break;
                }
            }

            mrw = new MiriamRegistryWrapper(name, url, comment, pattern);
            miriamRegistry.addItem(mrw);
            miriamRegistryToolTips.add(mrw.getComment());
            miriamRegistryMap.put(url, counter);
            counter++;
        }
        miriamRegistryCbRenderer.setTooltips(miriamRegistryToolTips);
        LOGGER.debug("Finished MIRIAM registry part");
        LOGGER.debug("Setting SBO terms to compartments");
        sboCb.setSelectedIndex(0);
        if(compartment.hasProperty(SBO_TERM)) {
            sboCb.setSelectedItem(compartment.getProperty(SBO_TERM));
        } else {
            sboCb.setSelectedIndex(0);
        }
        LOGGER.info("Finished initializing CompartmentAnnotationFrame");
    }

    public void deleteMiriamIdentifier(MiriamWrapper mw, JList owner) {
        LOGGER.info("Deleting MIRIAM identifier from compartment");
        if(identifierInEdit != null) {
            uri.setText("");
            editMiriamIdentifier = false;
            addMiriamIdentifier.setText("Add");
            identifierInEdit = null;
        }

        ((List<CVTerm>)compartment.getProperty(MIRIAM_BIO_QUALIFIERS)).remove(mw.getCVTerm());
        miModel.removeElement(mw);
        LOGGER.info("Successfully deleted MIRIAM identifier from compartment");
    }

    public void editMiriamIdentifier(MiriamWrapper mw, JList owner) {
        LOGGER.info("Editing MIRIAM identifier for compartment");
        qualifier.setSelectedItem(mw.getCVTerm().getBiologicalQualifierType());
        uri.setText(mw.getCVTerm().getResourceURI(0).substring(mw.getCVTerm().getResourceURI(0).lastIndexOf("/")+1));
        miriamRegistry.setSelectedIndex(miriamRegistryMap.get(mw.getCVTerm().getResourceURI(0).substring(0, mw.getCVTerm().getResourceURI(0).lastIndexOf("/")+1)));
        editMiriamIdentifier = true;
        addMiriamIdentifier.setText("Save");
        identifierInEdit = mw;
        LOGGER.info("Successfully edited MIRIAM identifier for compartment");
    }

    public void goToMiriamIdentifier(MiriamWrapper mw) {
        LOGGER.info("Going to MIRIAM identifier for compartment");
        if(Desktop.isDesktopSupported()) {
            try {
                Desktop.getDesktop().browse(new URI(mw.getCVTerm().getResourceURI(0)));
            }
            catch (IOException | URISyntaxException ex) {
                LOGGER.error(ex);
            }
        }
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        jPanel1 = new javax.swing.JPanel();
        jPanel2 = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        qualifier = new javax.swing.JComboBox<>();
        jLabel2 = new javax.swing.JLabel();
        uri = new javax.swing.JTextField();
        addMiriamIdentifier = new javax.swing.JButton();
        jLabel3 = new javax.swing.JLabel();
        jScrollPane2 = new javax.swing.JScrollPane();
        miriamIdentifiers = new javax.swing.JList<>();
        jLabel4 = new javax.swing.JLabel();
        jLabel13 = new javax.swing.JLabel();
        miriamRegistry = new javax.swing.JComboBox();
        sboTerm = new javax.swing.JLabel();
        sboCb = new javax.swing.JComboBox<>();
        saveSBOTerm = new javax.swing.JButton();
        jButton1 = new javax.swing.JButton();
        compartmentLabel = new javax.swing.JLabel();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setMinimumSize(new java.awt.Dimension(400, 600));
        setPreferredSize(new java.awt.Dimension(400, 600));
        getContentPane().setLayout(new java.awt.GridBagLayout());

        jPanel1.setLayout(new java.awt.GridBagLayout());

        jPanel2.setLayout(new java.awt.GridBagLayout());

        jLabel1.setText("Qualifier:");
        jLabel1.setToolTipText("A list of all available MIRIAM qualifiers.");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_START;
        gridBagConstraints.insets = new java.awt.Insets(10, 8, 0, 0);
        jPanel2.add(jLabel1, gridBagConstraints);

        qualifier.setModel(new javax.swing.DefaultComboBoxModel());
        qualifier.setMinimumSize(new java.awt.Dimension(175, 24));
        qualifier.setPreferredSize(new java.awt.Dimension(175, 24));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.insets = new java.awt.Insets(10, 9, 0, 0);
        jPanel2.add(qualifier, gridBagConstraints);

        jLabel2.setText("ID:");
        jLabel2.setToolTipText("The ID from the selected database");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_START;
        gridBagConstraints.insets = new java.awt.Insets(10, 8, 0, 0);
        jPanel2.add(jLabel2, gridBagConstraints);

        uri.setMinimumSize(new java.awt.Dimension(175, 19));
        uri.setPreferredSize(new java.awt.Dimension(175, 19));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.insets = new java.awt.Insets(10, 9, 0, 0);
        jPanel2.add(uri, gridBagConstraints);

        addMiriamIdentifier.setText("Add");
        addMiriamIdentifier.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                addMiriamIdentifierActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 5;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.insets = new java.awt.Insets(10, 8, 10, 0);
        jPanel2.add(addMiriamIdentifier, gridBagConstraints);

        jLabel3.setFont(new java.awt.Font("Dialog", 1, 14)); // NOI18N
        jLabel3.setText("MIRIAM");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.insets = new java.awt.Insets(10, 0, 5, 0);
        jPanel2.add(jLabel3, gridBagConstraints);

        jScrollPane2.setPreferredSize(new java.awt.Dimension(237, 90));

        miriamIdentifiers.setModel(new DefaultListModel());
        miriamIdentifiers.setToolTipText("A list of all MIRIAM identifiers assigned to the selected compartment");
        miriamIdentifiers.setMinimumSize(new java.awt.Dimension(150, 150));
        miriamIdentifiers.setName("entry"); // NOI18N
        miriamIdentifiers.setPreferredSize(new java.awt.Dimension(200, 200));
        miriamIdentifiers.addMouseListener(new ModellersMouseListenerCompartment(this, miriamIdentifiers));
        jScrollPane2.setViewportView(miriamIdentifiers);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 7;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(10, 0, 0, 0);
        jPanel2.add(jScrollPane2, gridBagConstraints);

        jLabel4.setText("List of Identifier");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 6;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.insets = new java.awt.Insets(10, 0, 5, 0);
        jPanel2.add(jLabel4, gridBagConstraints);

        jLabel13.setText("Database:");
        jLabel13.setToolTipText("A list of all databases, a identifier can come from");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_START;
        gridBagConstraints.insets = new java.awt.Insets(10, 8, 0, 0);
        jPanel2.add(jLabel13, gridBagConstraints);

        miriamRegistry.setModel(new javax.swing.DefaultComboBoxModel());
        miriamRegistry.setMinimumSize(new java.awt.Dimension(175, 24));
        miriamRegistry.setPreferredSize(new java.awt.Dimension(175, 24));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.insets = new java.awt.Insets(10, 9, 0, 0);
        jPanel2.add(miriamRegistry, gridBagConstraints);

        sboTerm.setFont(new java.awt.Font("Dialog", 1, 14)); // NOI18N
        sboTerm.setText("SBO Term");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 8;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.insets = new java.awt.Insets(10, 0, 0, 0);
        jPanel2.add(sboTerm, gridBagConstraints);

        sboCb.setModel(new DefaultComboBoxModel());
        sboCb.setToolTipText("A list of all SBO Terms which can be assigned to the selected compartment");
        sboCb.setMinimumSize(new java.awt.Dimension(125, 24));
        sboCb.setPreferredSize(new java.awt.Dimension(125, 24));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 9;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.insets = new java.awt.Insets(10, 0, 0, 0);
        jPanel2.add(sboCb, gridBagConstraints);

        saveSBOTerm.setText("Set SBO Term");
        saveSBOTerm.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                saveSBOTermActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 10;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.insets = new java.awt.Insets(10, 0, 0, 0);
        jPanel2.add(saveSBOTerm, gridBagConstraints);

        jButton1.setText("Close");
        jButton1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton1ActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 11;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.insets = new java.awt.Insets(25, 0, 0, 0);
        jPanel2.add(jButton1, gridBagConstraints);

        compartmentLabel.setFont(new java.awt.Font("Dialog", 1, 14)); // NOI18N
        compartmentLabel.setText("jLabel5");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.insets = new java.awt.Insets(10, 0, 10, 0);
        jPanel2.add(compartmentLabel, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.weightx = 0.85;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(20, 0, 0, 0);
        jPanel1.add(jPanel2, gridBagConstraints);

        getContentPane().add(jPanel1, new java.awt.GridBagConstraints());

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void addMiriamIdentifierActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_addMiriamIdentifierActionPerformed
        LOGGER.info("Trying to add MIRIAM identifier entry to compartment");
        if(!uri.getText().isEmpty()) {
            // Edit a entry
            if(editMiriamIdentifier == true) {
                LOGGER.info("Editing an entry for a compartment");
                MiriamRegistryWrapper mrw = (MiriamRegistryWrapper) miriamRegistry.getSelectedItem();
                Matcher m = mrw.getPattern().matcher(uri.getText().trim());
                if(!m.matches()) {
                    LOGGER.warn("Invalid accession number");
                    JOptionPane.showMessageDialog(this, "Invalid accession number");
                    return;
                }

                List<CVTerm> cvts = (List<CVTerm>) compartment.getProperty(MIRIAM_BIO_QUALIFIERS);

                // Same identifier = update the uri
                LOGGER.info("Same identifier, updating the uri");
                if(identifierInEdit.getCVTerm().getBiologicalQualifierType().equals((Qualifier) qualifier.getSelectedItem())) {
                    for(CVTerm cvt : cvts) {
                        if((identifierInEdit.getCVTerm().getBiologicalQualifierType()).equals(cvt.getBiologicalQualifierType())) {
                            cvt.getResources().set(cvt.getResources().indexOf(identifierInEdit.getURI()), mrw.getURL()+uri.getText().trim());
                            break;
                        }
                    }
                } else { // new identifier = update the identifier and the uri
                    LOGGER.info("New identifier, updating identifier and uri");
                    // first: delete the old one
                    CVTerm toRemove = null;
                    for(CVTerm cvt : cvts) {
                        if((identifierInEdit.getCVTerm().getBiologicalQualifierType()).equals(cvt.getBiologicalQualifierType())) {
                            cvt.getResources().remove(cvt.getResources().indexOf(identifierInEdit.getURI()));
                            if(cvt.getResourceCount() == 0) {
                                toRemove = cvt;
                            }
                            break;
                        }
                    }
                    if(toRemove != null) {
                        LOGGER.info("Removing old one");
                        cvts.remove(toRemove);
                        compartment.putProperty(MIRIAM_BIO_QUALIFIERS, cvts);
                    }
                    LOGGER.info("Adding new ones");
                    // now add the new ones
                    cvts = (List<CVTerm>) compartment.getProperty(MIRIAM_BIO_QUALIFIERS);
                    boolean qualifierWasThere = false;
                    for(CVTerm cvt : cvts) {
                        if(((Qualifier) qualifier.getSelectedItem()).equals(cvt.getBiologicalQualifierType())) {
                            qualifierWasThere = true;
                            cvt.addResource(uri.getText().trim());
                        }
                        compartment.putProperty(MIRIAM_BIO_QUALIFIERS, cvts);
                    }

                    if(!qualifierWasThere) {
                        MiriamWrapper mw = new MiriamWrapper((Qualifier) qualifier.getSelectedItem(), mrw.getURL()+uri.getText().trim());
                        ((List<CVTerm>)compartment.getProperty(MIRIAM_BIO_QUALIFIERS)).add(mw.getCVTerm());
                    }

                }

                identifierInEdit.setQualifier((Qualifier) qualifier.getSelectedItem());
                identifierInEdit.setURI(mrw.getURL()+uri.getText().trim());

                editMiriamIdentifier = false;
                addMiriamIdentifier.setText("Add");
                identifierInEdit = null;
                miriamIdentifiers.repaint();
                LOGGER.info("Finished editing entry for a compartment");
            }
            else if(editMiriamIdentifier == false) {
                LOGGER.info("Adding new entry for a compartment");
                MiriamRegistryWrapper mrw = (MiriamRegistryWrapper) miriamRegistry.getSelectedItem();

                Matcher m = mrw.getPattern().matcher(uri.getText().trim());
                if(!m.matches()) {
                    LOGGER.warn("Invalid accession number");
                    JOptionPane.showMessageDialog(this, "Invalid accession number");
                    return;
                }

                MiriamWrapper mw = new MiriamWrapper((Qualifier) qualifier.getSelectedItem(), mrw.getURL()+uri.getText().trim());
                miModel.addElement(mw);

                if(!compartment.hasProperty(MIRIAM_BIO_QUALIFIERS)) {
                    compartment.putProperty(MIRIAM_BIO_QUALIFIERS, new ArrayList<CVTerm>());
                }

                List<CVTerm> cvts = (List<CVTerm>) compartment.getProperty(MIRIAM_BIO_QUALIFIERS);
                boolean qualifierWasThere = false;
                for(CVTerm cvt : cvts) {
                    if((mw.getCVTerm().getBiologicalQualifierType()).equals(cvt.getBiologicalQualifierType())) {
                        qualifierWasThere = true;
                        cvt.addResource(uri.getText().trim());
                    }
                    compartment.putProperty(MIRIAM_BIO_QUALIFIERS, cvts);
                }

                if(!qualifierWasThere) {
                    ((List<CVTerm>)compartment.getProperty(MIRIAM_BIO_QUALIFIERS)).add(mw.getCVTerm());
                }
                LOGGER.info("Finished adding new entry for compartment");
            }

            uri.setText("");
        }
        LOGGER.info("Finished adding MIRIAM identifier entry for compartment");
    }//GEN-LAST:event_addMiriamIdentifierActionPerformed

    private void saveSBOTermActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_saveSBOTermActionPerformed
        LOGGER.info("Adding SBO term to compartment");
        if(sboCb.getSelectedItem() != null && sboCb.getSelectedIndex() > 0) {
            compartment.putProperty(SBO_TERM, (String)sboCb.getSelectedItem());
        }
        LOGGER.info("Succesfully added SBO term to compartment");
        // Shouldn't there also be a function to remove SBO terms like in AnnotationsPanel?
    }//GEN-LAST:event_saveSBOTermActionPerformed

    private void jButton1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton1ActionPerformed
        setVisible(true);
        this.dispose();
    }//GEN-LAST:event_jButton1ActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton addMiriamIdentifier;
    private javax.swing.JLabel compartmentLabel;
    private javax.swing.JButton jButton1;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel13;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JList<MiriamWrapper> miriamIdentifiers;
    private javax.swing.JComboBox miriamRegistry;
    private javax.swing.JComboBox<Qualifier> qualifier;
    private javax.swing.JButton saveSBOTerm;
    private javax.swing.JComboBox<String> sboCb;
    private javax.swing.JLabel sboTerm;
    private javax.swing.JTextField uri;
    // End of variables declaration//GEN-END:variables
}
