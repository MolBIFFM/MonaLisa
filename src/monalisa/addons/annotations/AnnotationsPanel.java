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

import java.awt.Desktop;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.swing.DefaultComboBoxModel;
import javax.swing.DefaultListModel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import monalisa.addons.AddonPanel;
import monalisa.addons.netviewer.NetViewer;
import monalisa.addons.netviewer.NetViewerNode;
import monalisa.data.pn.Compartment;
import monalisa.data.pn.PetriNetFacade;
import monalisa.data.pn.UniquePetriNetEntity;
import monalisa.resources.ResourceManager;
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
public class AnnotationsPanel extends AddonPanel {

    public final static String MIRIAM_BIO_QUALIFIERS = "MIRIAM_BIO_QUALIFIERS";
    public final static String MIRIAM_MODEL_QUALIFIERS = "MIRIAM_MODEL_QUALIFIERS";
    public final static String HISTORY = "HISTORY";
    public final static String MODEL_NAME = "MODEL_NAME";
    public final static String SBO_TERM = "SBO_TERM";

    private final static Logger LOGGER = LogManager.getLogger(AnnotationsPanel.class);
    private DefaultListModel<MiriamWrapper> miModelEntry;
    private NetViewerNode selectedNV;

    private boolean editMiriamIdentifierEntry;
    private MiriamWrapper identifierInEditEntry;

    private ModelInformationFrame mif;

    protected Map<String, Integer> miriamRegistryMap = new HashMap<>();

    private final String helpText;

    /**
     * Creates new form AnnotationsPanel
     */
    public AnnotationsPanel(final NetViewer netViewer, final PetriNetFacade petriNet) {
        super(netViewer, petriNet, "Annotations");
        LOGGER.info("Initializing AnnotationsPanel");
        helpText = "<html><center>&nbsp;&nbsp;&nbsp;For an overview over MIRIAM click <a href=\"http://www.ebi.ac.uk/miriam/main/\">here</a>&nbsp;&nbsp;&nbsp;"
                + "<br />&nbsp;&nbsp;&nbsp;For an overview over SBO click <a href=\"http://www.ebi.ac.uk/sbo/main/\">here</a>&nbsp;&nbsp;&nbsp;</center></html>";
        initComponents();

        mif = new ModelInformationFrame(this, petriNet, netViewer);

        identifierInEditEntry = null;
        mif.identifierInEditModel = null;
        LOGGER.info("Started MIRIAM part");
        // START: MIRIAM
        miModelEntry = (DefaultListModel<MiriamWrapper>)miriamIdentifiersEntry.getModel();
        entryQualifier.addItem(Qualifier.BQB_ENCODES);
        entryQualifier.addItem(Qualifier.BQB_HAS_PART);
        entryQualifier.addItem(Qualifier.BQB_HAS_PROPERTY);
        entryQualifier.addItem(Qualifier.BQB_HAS_TAXON);
        entryQualifier.addItem(Qualifier.BQB_HAS_VERSION);
        entryQualifier.addItem(Qualifier.BQB_IS);
        entryQualifier.addItem(Qualifier.BQB_IS_DESCRIBED_BY);
        entryQualifier.addItem(Qualifier.BQB_IS_ENCODED_BY);
        entryQualifier.addItem(Qualifier.BQB_IS_HOMOLOG_TO);
        entryQualifier.addItem(Qualifier.BQB_IS_PART_OF);
        entryQualifier.addItem(Qualifier.BQB_IS_PROPERTY_OF);
        entryQualifier.addItem(Qualifier.BQB_IS_VERSION_OF);
        entryQualifier.addItem(Qualifier.BQB_OCCURS_IN);
        entryQualifier.addItem(Qualifier.BQB_UNKNOWN);

        vertexNameLabel.setText("");
        setIdentifierEnabled(false);

        netViewer.getVisualizationViewer().getRenderContext().getPickedVertexState().addItemListener(new ItemListener() {
            @Override
            public void itemStateChanged(ItemEvent ie) {
                LOGGER.info("Item state changed");
                if(ie.getStateChange() == ItemEvent.DESELECTED) {
                    LOGGER.info("Item deselected");
                    vertexNameLabel.setText("");
                    setIdentifierEnabled(false);
                }
                if(ie.getStateChange() == ItemEvent.SELECTED) {
                    LOGGER.info("Item selected");
                    miModelEntry.removeAllElements();
                    Set<NetViewerNode> picked = netViewer.getVisualizationViewer().getRenderContext().getPickedVertexState().getPicked();
                    // Only one node is selected
                    if(picked.size() == 1) {
                        setIdentifierEnabled(true);

                        selectedNV = ((NetViewerNode) (netViewer.getVisualizationViewer().getRenderContext().getPickedVertexState().getPicked().toArray())[0]).getMasterNode();

                        if(selectedNV.getNodeType().equals(NetViewer.BEND)) {
                            return;
                        }

                        vertexNameLabel.setText(selectedNV.getName());

                        UniquePetriNetEntity upe;
                        if(selectedNV.getNodeType().equals(NetViewer.TRANSITION)) {
                            upe = petriNet.findTransition(selectedNV.getId());
                        } else {
                            upe = petriNet.findPlace(selectedNV.getId());
                        }

                        if(upe.hasProperty(MIRIAM_BIO_QUALIFIERS)) {
                            fillIdentifierList((List<CVTerm>) upe.getProperty(MIRIAM_BIO_QUALIFIERS));
                        }

                        sboCb.setSelectedIndex(0);
                        if(upe.hasProperty(SBO_TERM)) {
                            sboCb.setSelectedItem(upe.getProperty(SBO_TERM));
                        } else {
                            sboCb.setSelectedIndex(0);
                        }

                        editMiriamIdentifierEntry = false;
                        addMiriamIdentifierEntry.setText("Save");

                    } else {
                        vertexNameLabel.setText("");
                        setIdentifierEnabled(false);
                    }
                }
            LOGGER.info("Dealt with item state change");
            }
        });

        // END: MIRIAM
        LOGGER.info("Finished MIRIAM part");
        LOGGER.info("Started SBO part");
        // START: SBO
        ComboboxToolTipRenderer sboCbRenderer = new ComboboxToolTipRenderer();
        sboCb.setRenderer(sboCbRenderer);
        ArrayList<String> sboToolTips = new ArrayList<>();

        SAXBuilder builder = new SAXBuilder();
        Document doc = null;
        try {
            LOGGER.info("Reading from 'SBO_XML.xml'");
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
        String toolTip;
        LOGGER.info("Adding SBO tooltips");
        for(Object o : root.getChildren() ) {
            e = (Element) o;
            sboCb.addItem(((Element)e.getContent().get(1)).getValue());
            toolTip = ((Element)e.getContent().get(3)).getValue();
            sboToolTips.add(toolTip);
        }
        sboCbRenderer.setTooltips(sboToolTips);
        LOGGER.info("Finished SBO part");
        LOGGER.info("Starting MIRIAM registry part");
        // START: MIRIAM registry
        ComboboxToolTipRenderer miriamRegistryCbRenderer = new ComboboxToolTipRenderer();
        miriamRegistryEntry.setRenderer(miriamRegistryCbRenderer);
        mif.miriamRegistryModel.setRenderer(miriamRegistryCbRenderer);
        ArrayList<String> miriamRegistryToolTips = new ArrayList<>();

        builder = new SAXBuilder();
        try {
            LOGGER.info("Reading from miriam_registry.xml");
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
        LOGGER.info("Adding MIRIAM URLs");
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
            miriamRegistryEntry.addItem(mrw);
            mif.miriamRegistryModel.addItem(mrw);
            miriamRegistryToolTips.add("<html>"+mrw.getComment()+"</html>");
            miriamRegistryMap.put(url, counter);
            counter++;
        }
        miriamRegistryCbRenderer.setTooltips(miriamRegistryToolTips);

        // END: MIRIAM
        LOGGER.info("Finished MIRIAM registry part");
        editMiriamIdentifierEntry = false;
        mif.editMiriamIdentifierModel = false;
    }

    static void editCompartmentIdentifiers(Compartment c) {

    }

    private void fillIdentifierList(List<CVTerm> cvts) {
        LOGGER.info("Filling identifier list");
        int childCount;
        for(CVTerm cvt : cvts) {
            childCount =  cvt.getChildCount();
            if(childCount > 1) {
                for(int i=0; i < cvt.getChildCount(); i++) {
                    miModelEntry.addElement(new MiriamWrapper(cvt.getBiologicalQualifierType(), cvt.getChildAt(i).toString()));
                }
            } else {
                miModelEntry.addElement(new MiriamWrapper(cvt));
            }
        }
        LOGGER.info("Successfully filled identifier list");
    }

    private void setIdentifierEnabled(boolean state) {
        LOGGER.info("Setting identifiers to '" + state + "'");
        entryQualifier.setEnabled(state);
        miriamRegistryEntry.setEnabled(state);
        uriEntry.setEnabled(state);
        addMiriamIdentifierEntry.setEnabled(state);
        miriamIdentifiersEntry.setEnabled(state);
        sboCb.setEnabled(state);
        saveSBOTerm.setEnabled(state);

        if(!state) {
            miModelEntry.removeAllElements();
        }
        LOGGER.info("Finished setting identifiers to '" + state + "'");
    }

    public void goToMiriamIdentifier(MiriamWrapper mw) {
        LOGGER.info("Going to MIRIAM identifier");
        if(Desktop.isDesktopSupported()) {
            try {
                Desktop.getDesktop().browse(new URI(mw.getCVTerm().getResourceURI(0)));
            } catch (IOException | URISyntaxException ex) {
                LOGGER.error(ex);
            }
        }
    }

    public void editMiriamIdentifier(MiriamWrapper mw, JList owner) {
        LOGGER.info("Editing MIRIAM identifier");
        if(owner.getName().equalsIgnoreCase("entry")) {
            entryQualifier.setSelectedItem(mw.getCVTerm().getBiologicalQualifierType());
            uriEntry.setText(mw.getCVTerm().getResourceURI(0).substring(mw.getCVTerm().getResourceURI(0).lastIndexOf("/")+1));
            miriamRegistryEntry.setSelectedIndex(miriamRegistryMap.get(mw.getCVTerm().getResourceURI(0).substring(0, mw.getCVTerm().getResourceURI(0).lastIndexOf("/")+1)));
            editMiriamIdentifierEntry = true;
            addMiriamIdentifierEntry.setText("Save");
            identifierInEditEntry = mw;
        } else if(owner.getName().equalsIgnoreCase("model")) {
            mif.editMiriamIdentifier(mw);
        }
        LOGGER.info("Finished editing MIRIAM identifier");
    }

    public void deleteMiriamIdentifier(MiriamWrapper mw, JList owner) {
        LOGGER.info("Deleting MIRIAM identifier");
        if(owner.getName().equalsIgnoreCase("entry")) {
            if(identifierInEditEntry != null) {
                uriEntry.setText("");
                editMiriamIdentifierEntry = false;
                addMiriamIdentifierEntry.setText("Add");
                identifierInEditEntry = null;
            }

            UniquePetriNetEntity upe;

            if(selectedNV.getNodeType().equals(NetViewer.TRANSITION)) {
                upe = petriNet.findTransition(selectedNV.getId());
            } else {
                upe = petriNet.findPlace(selectedNV.getId());
            }

            ((List<CVTerm>)upe.getProperty(MIRIAM_BIO_QUALIFIERS)).remove(mw.getCVTerm());
            miModelEntry.removeElement(mw);
        } else if(owner.getName().equalsIgnoreCase("model")) {
            mif.deleteMiriamIdentifier(mw);
        }
        LOGGER.info("Finished deleting MIRIAM identifier");
    }

    public void editModeller(ModellerWrapper mw, int selectedIndex) {
        LOGGER.info("Editing Modeller");
        mif.selectedModellerIndex = selectedIndex;

        mif.email.setText(mw.getEmail());
        mif.fName.setText(mw.getfName());
        mif.lName.setText(mw.getlName());
        mif.organisation.setText(mw.getOrganisation());
        LOGGER.info("Finished editing Modeller");
    }

    public void editDate(DateWrapper dw, int selectedIndex) {
        LOGGER.info("Editing date");
        mif.selectedDateIndex = selectedIndex;

        mif.month.setText(dw.getMonth());
        mif.year.setText(dw.getYear());
        mif.day.setText(dw.getDay());
        LOGGER.info("Editing date");
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
        jLabel1 = new javax.swing.JLabel();
        entryQualifier = new javax.swing.JComboBox<>();
        jLabel2 = new javax.swing.JLabel();
        uriEntry = new javax.swing.JTextField();
        addMiriamIdentifierEntry = new javax.swing.JButton();
        jLabel3 = new javax.swing.JLabel();
        jScrollPane2 = new javax.swing.JScrollPane();
        miriamIdentifiersEntry = new javax.swing.JList<>();
        jLabel4 = new javax.swing.JLabel();
        jLabel13 = new javax.swing.JLabel();
        miriamRegistryEntry = new javax.swing.JComboBox();
        jLabel21 = new javax.swing.JLabel();
        vertexNameLabel = new javax.swing.JLabel();
        sboTerm = new javax.swing.JLabel();
        sboCb = new javax.swing.JComboBox<>();
        saveSBOTerm = new javax.swing.JButton();
        jButton1 = new javax.swing.JButton();
        helpBox = new javax.swing.JEditorPane("text/html", helpText);
        jButton2 = new javax.swing.JButton();

        setLayout(new java.awt.GridBagLayout());

        jPanel1.setLayout(new java.awt.GridBagLayout());

        jLabel1.setText("Qualifier:");
        jLabel1.setToolTipText("A list of all available MIRIAM qualifiers");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_START;
        gridBagConstraints.insets = new java.awt.Insets(10, 8, 0, 0);
        jPanel1.add(jLabel1, gridBagConstraints);

        entryQualifier.setModel(new javax.swing.DefaultComboBoxModel());
        entryQualifier.setMinimumSize(new java.awt.Dimension(175, 24));
        entryQualifier.setPreferredSize(new java.awt.Dimension(175, 24));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.insets = new java.awt.Insets(10, 9, 0, 0);
        jPanel1.add(entryQualifier, gridBagConstraints);

        jLabel2.setText("ID:");
        jLabel2.setToolTipText("The ID from the selected database");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_START;
        gridBagConstraints.insets = new java.awt.Insets(10, 8, 0, 0);
        jPanel1.add(jLabel2, gridBagConstraints);

        uriEntry.setMinimumSize(new java.awt.Dimension(175, 19));
        uriEntry.setPreferredSize(new java.awt.Dimension(175, 19));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.insets = new java.awt.Insets(10, 9, 0, 0);
        jPanel1.add(uriEntry, gridBagConstraints);

        addMiriamIdentifierEntry.setText("Add");
        addMiriamIdentifierEntry.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                addMiriamIdentifierEntryActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 5;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.insets = new java.awt.Insets(10, 8, 10, 0);
        jPanel1.add(addMiriamIdentifierEntry, gridBagConstraints);

        jLabel3.setFont(new java.awt.Font("Dialog", 1, 14)); // NOI18N
        jLabel3.setText("MIRIAM");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.insets = new java.awt.Insets(10, 0, 5, 0);
        jPanel1.add(jLabel3, gridBagConstraints);

        jScrollPane2.setPreferredSize(new java.awt.Dimension(237, 90));

        miriamIdentifiersEntry.setModel(new DefaultListModel());
        miriamIdentifiersEntry.setToolTipText("A list of all MIRIAM identifiers assigned to the selected entity");
        miriamIdentifiersEntry.setMinimumSize(new java.awt.Dimension(150, 150));
        miriamIdentifiersEntry.setName("entry"); // NOI18N
        miriamIdentifiersEntry.setPreferredSize(new java.awt.Dimension(200, 200));
        miriamIdentifiersEntry.addMouseListener(new MiriamIdentifiersMouseListener(miriamIdentifiersEntry, this));
        jScrollPane2.setViewportView(miriamIdentifiersEntry);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 7;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(10, 0, 0, 0);
        jPanel1.add(jScrollPane2, gridBagConstraints);

        jLabel4.setText("List of Identifier");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 6;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.insets = new java.awt.Insets(10, 0, 5, 0);
        jPanel1.add(jLabel4, gridBagConstraints);

        jLabel13.setText("Database:");
        jLabel13.setToolTipText("A list of all databases, a identifier can come from");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_START;
        gridBagConstraints.insets = new java.awt.Insets(10, 8, 0, 0);
        jPanel1.add(jLabel13, gridBagConstraints);

        miriamRegistryEntry.setModel(new javax.swing.DefaultComboBoxModel());
        miriamRegistryEntry.setMinimumSize(new java.awt.Dimension(175, 24));
        miriamRegistryEntry.setPreferredSize(new java.awt.Dimension(175, 24));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.insets = new java.awt.Insets(10, 9, 0, 0);
        jPanel1.add(miriamRegistryEntry, gridBagConstraints);

        jLabel21.setFont(new java.awt.Font("Dialog", 1, 14)); // NOI18N
        jLabel21.setText("In progress:");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_START;
        gridBagConstraints.insets = new java.awt.Insets(10, 8, 0, 0);
        jPanel1.add(jLabel21, gridBagConstraints);

        vertexNameLabel.setFont(new java.awt.Font("Dialog", 1, 14)); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_START;
        gridBagConstraints.insets = new java.awt.Insets(10, 9, 0, 0);
        jPanel1.add(vertexNameLabel, gridBagConstraints);

        sboTerm.setFont(new java.awt.Font("Dialog", 1, 14)); // NOI18N
        sboTerm.setText("SBO Term");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 8;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.insets = new java.awt.Insets(20, 0, 0, 0);
        jPanel1.add(sboTerm, gridBagConstraints);

        sboCb.setModel(new DefaultComboBoxModel());
        sboCb.setToolTipText("A list of all SBO Terms which can be assigned to the selected entity");
        sboCb.setMinimumSize(new java.awt.Dimension(125, 24));
        sboCb.setPreferredSize(new java.awt.Dimension(125, 24));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 9;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.insets = new java.awt.Insets(10, 0, 0, 0);
        jPanel1.add(sboCb, gridBagConstraints);

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
        jPanel1.add(saveSBOTerm, gridBagConstraints);

        jButton1.setText("Edit Model Annotations");
        jButton1.setToolTipText("Opens a dialog to edit the annotations of the model itselfs");
        jButton1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton1ActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 12;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.insets = new java.awt.Insets(20, 0, 0, 0);
        jPanel1.add(jButton1, gridBagConstraints);

        helpBox.setEditable(false);
        helpBox.setBackground(java.awt.Color.lightGray);
        helpBox.addHyperlinkListener(new MonaLisaHyperlinkListener());
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 13;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.insets = new java.awt.Insets(20, 0, 0, 0);
        jPanel1.add(helpBox, gridBagConstraints);

        jButton2.setText("Delete SBO Term");
        jButton2.setToolTipText("Delete all made annotations!!!!");
        jButton2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton2ActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 11;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.insets = new java.awt.Insets(15, 0, 0, 0);
        jPanel1.add(jButton2, gridBagConstraints);
        jButton2.getAccessibleContext().setAccessibleName("DeleteSBOTerm");

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.weightx = 0.85;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(20, 0, 0, 0);
        add(jPanel1, gridBagConstraints);
    }// </editor-fold>//GEN-END:initComponents

    private void addMiriamIdentifierEntryActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_addMiriamIdentifierEntryActionPerformed
        LOGGER.info("Trying to add MIRIAM identifier entry");
        if(!uriEntry.getText().isEmpty() && selectedNV != null) {
            // Edit a entry
            if(editMiriamIdentifierEntry == true) {
                LOGGER.info("Editing an entry");
                MiriamRegistryWrapper mrw = (MiriamRegistryWrapper) miriamRegistryEntry.getSelectedItem();
                Matcher m = mrw.getPattern().matcher(uriEntry.getText().trim());
                if(!m.matches()) {
                    LOGGER.warn("Invalid accession number");
                    JOptionPane.showMessageDialog(this.netViewer, "Invalid accession number");
                    return;
                }

                UniquePetriNetEntity upe;

                if(selectedNV.getNodeType().equals(NetViewer.TRANSITION)) {
                    upe = petriNet.findTransition(selectedNV.getId());
                } else {
                    upe = petriNet.findPlace(selectedNV.getId());
                }
                List<CVTerm> cvts = (List<CVTerm>) upe.getProperty(MIRIAM_BIO_QUALIFIERS);
                // Same identifier = update the uri
                if(identifierInEditEntry.getCVTerm().getBiologicalQualifierType().equals((Qualifier) entryQualifier.getSelectedItem())) {
                    LOGGER.info("Same identifier, updating the uri");
                    for(CVTerm cvt : cvts) {
                        if((identifierInEditEntry.getCVTerm().getBiologicalQualifierType()).equals(cvt.getBiologicalQualifierType())) {
                            cvt.getResources().set(cvt.getResources().indexOf(identifierInEditEntry.getURI()), mrw.getURL()+uriEntry.getText().trim());
                            break;
                        }
                    }
                } else { // new identifier = update the identifier and the uri
                    LOGGER.info("New identifier, updating identifier and uri");
                    // first: delete the old one
                    CVTerm toRemove = null;
                    for(CVTerm cvt : cvts) {
                        if((identifierInEditEntry.getCVTerm().getBiologicalQualifierType()).equals(cvt.getBiologicalQualifierType())) {
                            cvt.getResources().remove(cvt.getResources().indexOf(identifierInEditEntry.getURI()));
                            if(cvt.getResourceCount() == 0) {
                                toRemove = cvt;
                            }
                            break;
                        }
                    }
                    if(toRemove != null) {
                        LOGGER.info("Removing old one");
                        cvts.remove(toRemove);
                        upe.putProperty(MIRIAM_BIO_QUALIFIERS, cvts);
                    }
                    LOGGER.info("Adding new ones");
                    // now add the new ones
                    cvts = (List<CVTerm>) upe.getProperty(MIRIAM_BIO_QUALIFIERS);
                    boolean qualifierWasThere = false;
                    for(CVTerm cvt : cvts) {
                        if(((Qualifier) entryQualifier.getSelectedItem()).equals(cvt.getBiologicalQualifierType())) {
                            qualifierWasThere = true;
                            cvt.addResource(uriEntry.getText().trim());
                        }
                        upe.putProperty(MIRIAM_BIO_QUALIFIERS, cvts);
                    }

                    if(!qualifierWasThere) {
                        MiriamWrapper mw = new MiriamWrapper((Qualifier) entryQualifier.getSelectedItem(), mrw.getURL()+uriEntry.getText().trim());
                        ((List<CVTerm>)upe.getProperty(MIRIAM_BIO_QUALIFIERS)).add(mw.getCVTerm());
                    }

                }

                identifierInEditEntry.setQualifier((Qualifier) entryQualifier.getSelectedItem());
                identifierInEditEntry.setURI(mrw.getURL()+uriEntry.getText().trim());

                editMiriamIdentifierEntry = false;
                addMiriamIdentifierEntry.setText("Add");
                identifierInEditEntry = null;
                miriamIdentifiersEntry.repaint();
                LOGGER.info("Finished editing entry");
            }
            else if(editMiriamIdentifierEntry == false) {
                LOGGER.info("Adding new entry");
                MiriamRegistryWrapper mrw = (MiriamRegistryWrapper) miriamRegistryEntry.getSelectedItem();

                Matcher m = mrw.getPattern().matcher(uriEntry.getText().trim());
                if(!m.matches()) {
                    LOGGER.warn("Invalid accession number");
                    JOptionPane.showMessageDialog(this.netViewer, "Invalid accession number");
                    return;
                }

                MiriamWrapper mw = new MiriamWrapper((Qualifier) entryQualifier.getSelectedItem(), mrw.getURL()+uriEntry.getText().trim());
                miModelEntry.addElement(mw);

                UniquePetriNetEntity upe;

                if(selectedNV.getNodeType().equals(NetViewer.TRANSITION)) {
                    upe = petriNet.findTransition(selectedNV.getId());
                } else {
                    upe = petriNet.findPlace(selectedNV.getId());
                }

                if(!upe.hasProperty(MIRIAM_BIO_QUALIFIERS)) {
                    upe.putProperty(MIRIAM_BIO_QUALIFIERS, new ArrayList<CVTerm>());
                }

                List<CVTerm> cvts = (List<CVTerm>) upe.getProperty(MIRIAM_BIO_QUALIFIERS);
                boolean qualifierWasThere = false;
                for(CVTerm cvt : cvts) {
                    if((mw.getCVTerm().getBiologicalQualifierType()).equals(cvt.getBiologicalQualifierType())) {
                        qualifierWasThere = true;
                        cvt.addResource(uriEntry.getText().trim());
                    }
                    upe.putProperty(MIRIAM_BIO_QUALIFIERS, cvts);
                }

                if(!qualifierWasThere) {
                    ((List<CVTerm>)upe.getProperty(MIRIAM_BIO_QUALIFIERS)).add(mw.getCVTerm());
                }
            LOGGER.info("Finished adding new entry");
            }

            uriEntry.setText("");
        }
        LOGGER.info("Finished adding MIRIAM identifier entry");
    }//GEN-LAST:event_addMiriamIdentifierEntryActionPerformed

    private void saveSBOTermActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_saveSBOTermActionPerformed
        LOGGER.info("Adding SBO term");
        if(sboCb.getSelectedItem() != null && selectedNV != null && sboCb.getSelectedIndex() > 0) {

            UniquePetriNetEntity upe;
            if(selectedNV.getNodeType().equals(NetViewer.TRANSITION)) {
                upe = petriNet.findTransition(selectedNV.getId());
            } else {
                upe = petriNet.findPlace(selectedNV.getId());
            }

            upe.putProperty(SBO_TERM, (String)sboCb.getSelectedItem());
        }
        LOGGER.info("Successfully added SBO term");
    }//GEN-LAST:event_saveSBOTermActionPerformed

    private void jButton1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton1ActionPerformed
        this.netViewer.displayMenu(mif.getContentPane(), "mif");
    }//GEN-LAST:event_jButton1ActionPerformed

    private void jButton2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton2ActionPerformed
        LOGGER.info("Removing SBO term");
        if(sboCb.getSelectedItem() != null && selectedNV != null && sboCb.getSelectedIndex() > 0) {

            UniquePetriNetEntity upe;
            if(selectedNV.getNodeType().equals(NetViewer.TRANSITION)) {
                upe = petriNet.findTransition(selectedNV.getId());
            } else {
                upe = petriNet.findPlace(selectedNV.getId());
            }

            upe.removeProperty(SBO_TERM);
        }
        LOGGER.info("Finished removing SBO term");
    }//GEN-LAST:event_jButton2ActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton addMiriamIdentifierEntry;
    private javax.swing.JComboBox<Qualifier> entryQualifier;
    private javax.swing.JEditorPane helpBox;
    private javax.swing.JButton jButton1;
    private javax.swing.JButton jButton2;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel13;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel21;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JList<MiriamWrapper> miriamIdentifiersEntry;
    private javax.swing.JComboBox miriamRegistryEntry;
    private javax.swing.JButton saveSBOTerm;
    private javax.swing.JComboBox<String> sboCb;
    private javax.swing.JLabel sboTerm;
    private javax.swing.JTextField uriEntry;
    private javax.swing.JLabel vertexNameLabel;
    // End of variables declaration//GEN-END:variables
}