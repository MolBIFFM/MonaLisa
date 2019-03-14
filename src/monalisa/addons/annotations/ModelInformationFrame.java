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

import java.awt.Dimension;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.regex.Matcher;
import javax.swing.DefaultListModel;
import javax.swing.JOptionPane;
import static monalisa.addons.annotations.AnnotationsPanel.HISTORY;
import static monalisa.addons.annotations.AnnotationsPanel.MIRIAM_MODEL_QUALIFIERS;
import static monalisa.addons.annotations.AnnotationsPanel.MODEL_NAME;
import monalisa.addons.netviewer.NetViewer;
import monalisa.data.pn.PetriNetFacade;
import monalisa.resources.ResourceManager;
import monalisa.resources.StringResources;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.sbml.jsbml.CVTerm;
import org.sbml.jsbml.CVTerm.Qualifier;
import org.sbml.jsbml.Creator;
import org.sbml.jsbml.History;

/**
 *
 * @author jens
 */
public class ModelInformationFrame extends javax.swing.JFrame {

    private static final Logger LOGGER = LogManager.getLogger(ModelInformationFrame.class);
    private static final ResourceManager resources = ResourceManager.instance();
    private static final StringResources strings = resources.getDefaultStrings();

    private final AnnotationsPanel ap;
    private final PetriNetFacade petriNet;
    private final NetViewer netViewer;

    protected int selectedModellerIndex;
    protected int selectedDateIndex;

    protected final DefaultListModel<ModellerWrapper> modellersModel;
    protected final DefaultListModel<DateWrapper> datesModel;
    protected final DefaultListModel<MiriamWrapper> miModelModel;

    protected boolean editMiriamIdentifierModel;
    protected MiriamWrapper identifierInEditModel;

    /**
     * Creates new form ModelInformationPanel
     */
    public ModelInformationFrame(AnnotationsPanel ap, final PetriNetFacade petriNet, final NetViewer netViewer) {
        LOGGER.info("Initializing ModelInformationFrame");
        this.ap = ap;
        this.petriNet = petriNet;
        this.netViewer = netViewer;

        setTitle("Model Annotation");
        setIconImage(resources.getImage("icon-16.png"));
        setPreferredSize(new Dimension(350, 760));
        setMinimumSize(new Dimension(350, 760));
        setLocationRelativeTo(netViewer);

        initComponents();

        miModelModel = (DefaultListModel<MiriamWrapper>)miriamIdentifiersModel.getModel();
        modelQualifier.removeAllItems();
        modelQualifier.addItem(CVTerm.Qualifier.BQM_IS);
        modelQualifier.addItem(CVTerm.Qualifier.BQM_IS_DERIVED_FROM);
        modelQualifier.addItem(CVTerm.Qualifier.BQM_IS_DESCRIBED_BY);

        // START: ModelInformations
        LOGGER.info("Starting ModelInformations part");
        modellersModel = (DefaultListModel<ModellerWrapper>) modellersList.getModel();
        datesModel = (DefaultListModel<DateWrapper>) datesList.getModel();

        selectedModellerIndex = -1;
        selectedDateIndex = -1;

        if(petriNet.hasProperty(MODEL_NAME)) {
            modelName.setText( (String) petriNet.getProperty(MODEL_NAME));
        }
        if(petriNet.hasProperty(MIRIAM_MODEL_QUALIFIERS)) {
            LOGGER.info("Adding MIRIAM model qualifiers");
            List<CVTerm> cvts = (List<CVTerm>) petriNet.getProperty(MIRIAM_MODEL_QUALIFIERS);
            int childCount;
            for(CVTerm cvt : cvts) {
                childCount =  cvt.getChildCount();
                if(childCount > 1) {
                    for(int i=0; i < cvt.getChildCount(); i++) {
                        if(cvt.getModelQualifierType() == null)
                            continue;
                        miModelModel.addElement(new MiriamWrapper(cvt.getModelQualifierType(), cvt.getChildAt(i).toString()));
                    }
                } else {
                    miModelModel.addElement(new MiriamWrapper(cvt));
                }
            }
        }

        if(petriNet.hasProperty(HISTORY)) {
            LOGGER.info("Adding Modeller information");
            History hist = (History) petriNet.getProperty(HISTORY);

            if(hist != null) {
                if(hist.getCreatorCount() > 0) {
                    Creator c;
                    for(int i = 0; i < hist.getCreatorCount(); i++) {
                        c = hist.getCreator(i);
                        modellersModel.addElement(new ModellerWrapper(c.getGivenName(), c.getFamilyName(), c.getOrganisation(), c.getEmail()));
                    }
                }

                if(hist.getCreatedDate() != null) {
                    Calendar cal = Calendar.getInstance();
                    cal.setTime(hist.getCreatedDate());
                    datesModel.addElement(new DateWrapper(new Integer(cal.get(Calendar.YEAR)).toString(), new Integer(cal.get(Calendar.MONTH)+1).toString(), new Integer(cal.get(Calendar.DAY_OF_MONTH)).toString()));

                    if(hist.getModifiedDateCount() > 0) {
                        for(Date d : hist.getListOfModifiedDates()) {
                            cal.setTime(d);
                            datesModel.addElement(new DateWrapper(new Integer(cal.get(Calendar.YEAR)).toString(), new Integer(cal.get(Calendar.MONTH)+1).toString(), new Integer(cal.get(Calendar.DAY_OF_MONTH)).toString()));
                        }
                    }
                }
            }
        }
        // END: ModelInformations
        LOGGER.info("Finished ModelInformations part and initializing ModelInformationFrame");
    }

    public void editMiriamIdentifier(MiriamWrapper mw) {
        LOGGER.info("Editing MIRIAM identifier for model");
        modelQualifier.setSelectedItem(mw.getCVTerm().getModelQualifierType());
        uriModel.setText(mw.getCVTerm().getResourceURI(0).substring(mw.getCVTerm().getResourceURI(0).lastIndexOf("/")+1));
        miriamRegistryModel.setSelectedIndex(ap.miriamRegistryMap.get(mw.getCVTerm().getResourceURI(0).substring(0, mw.getCVTerm().getResourceURI(0).lastIndexOf("/")+1)));
        editMiriamIdentifierModel = true;
        addMiriamIdentifierModel.setText("Save");
        identifierInEditModel = mw;
        LOGGER.info("Successfully edited MIRIAM identifier for model");
    }

    public void deleteMiriamIdentifier(MiriamWrapper mw) {
        LOGGER.info("Deleting MIRIAM identifier for model");
        if(identifierInEditModel != null) {
            uriModel.setText("");
            editMiriamIdentifierModel = false;
            addMiriamIdentifierModel.setText("Add");
            identifierInEditModel = null;
        }

        ((List<CVTerm>)petriNet.getProperty(MIRIAM_MODEL_QUALIFIERS)).remove(mw.getCVTerm());
        miModelModel.removeElement(mw);
        LOGGER.info("Finished deleting MIRIAM identifier for model");
    }

    private void saveModelInformations() {
        LOGGER.info("Saving model informations");
        if(!modelName.getText().isEmpty()) {
            petriNet.putProperty(MODEL_NAME, modelName.getText());
        }

        History hist = null;
        if(modellersModel.size() > 0 || datesModel.size() > 0) {
            LOGGER.info("Creating new History");
            hist = new History();
        }

        if(hist != null && modellersModel.size() > 0) {
            LOGGER.info("Filling history with Creators");
            ModellerWrapper mw;
            for(int i = 0; i < modellersModel.size(); i++) {
                mw = (ModellerWrapper) modellersModel.getElementAt(i);
                hist.addCreator(new Creator(mw.getfName(), mw.getlName(), mw.getOrganisation(), mw.getEmail()));
            }
        }

        if(hist != null && datesModel.size() > 0) {
            LOGGER.info("Adding dates to history");
            DateWrapper dw;
            for(int i = 0; i < datesModel.size(); i++) {
                dw = (DateWrapper) datesModel.getElementAt(i);
                if(i == 0) {
                    hist.setCreatedDate(dw.getDate());
                } else {
                    hist.setModifiedDate(dw.getDate());
                }
            }
        }

        if(hist != null)
            petriNet.putProperty(HISTORY, hist);
        LOGGER.info("Finished saving model information");
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

        modelInformationPanel = new javax.swing.JPanel();
        jLabel5 = new javax.swing.JLabel();
        jLabel6 = new javax.swing.JLabel();
        modelName = new javax.swing.JTextField();
        jLabel12 = new javax.swing.JLabel();
        jLabel8 = new javax.swing.JLabel();
        jLabel9 = new javax.swing.JLabel();
        jLabel10 = new javax.swing.JLabel();
        jLabel11 = new javax.swing.JLabel();
        fName = new javax.swing.JTextField();
        lName = new javax.swing.JTextField();
        organisation = new javax.swing.JTextField();
        email = new javax.swing.JTextField();
        saveModeller = new javax.swing.JButton();
        jLabel14 = new javax.swing.JLabel();
        jScrollPane1 = new javax.swing.JScrollPane();
        modellersList = new javax.swing.JList<>();
        jScrollPane3 = new javax.swing.JScrollPane();
        datesList = new javax.swing.JList<>();
        saveDate = new javax.swing.JButton();
        jPanel4 = new javax.swing.JPanel();
        jLabel16 = new javax.swing.JLabel();
        jLabel17 = new javax.swing.JLabel();
        jLabel18 = new javax.swing.JLabel();
        year = new javax.swing.JTextField();
        month = new javax.swing.JTextField();
        day = new javax.swing.JTextField();
        jLabel15 = new javax.swing.JLabel();
        modelQualifier = new javax.swing.JComboBox();
        jLabel19 = new javax.swing.JLabel();
        miriamRegistryModel = new javax.swing.JComboBox();
        jScrollPane4 = new javax.swing.JScrollPane();
        miriamIdentifiersModel = new javax.swing.JList();
        addMiriamIdentifierModel = new javax.swing.JButton();
        jLabel20 = new javax.swing.JLabel();
        uriModel = new javax.swing.JTextField();
        jLabel7 = new javax.swing.JLabel();
        jButton1 = new javax.swing.JButton();

        getContentPane().setLayout(new java.awt.GridBagLayout());

        modelInformationPanel.setLayout(new java.awt.GridBagLayout());

        jLabel5.setFont(new java.awt.Font("Dialog", 1, 14)); // NOI18N
        jLabel5.setText("Model Informations");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.insets = new java.awt.Insets(10, 0, 10, 0);
        modelInformationPanel.add(jLabel5, gridBagConstraints);

        jLabel6.setText("Model Name:");
        jLabel6.setToolTipText("The name of the model");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_START;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 5, 0);
        modelInformationPanel.add(jLabel6, gridBagConstraints);

        modelName.setMinimumSize(new java.awt.Dimension(200, 19));
        modelName.setPreferredSize(new java.awt.Dimension(200, 19));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 5, 0);
        modelInformationPanel.add(modelName, gridBagConstraints);

        jLabel12.setText("Modellers");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 8;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.insets = new java.awt.Insets(5, 0, 0, 0);
        modelInformationPanel.add(jLabel12, gridBagConstraints);

        jLabel8.setText("First Name:");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 9;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_START;
        modelInformationPanel.add(jLabel8, gridBagConstraints);

        jLabel9.setText("Family Name:");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 10;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_START;
        gridBagConstraints.insets = new java.awt.Insets(5, 0, 0, 0);
        modelInformationPanel.add(jLabel9, gridBagConstraints);

        jLabel10.setText("Organisation:");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 11;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_START;
        gridBagConstraints.insets = new java.awt.Insets(5, 0, 0, 0);
        modelInformationPanel.add(jLabel10, gridBagConstraints);

        jLabel11.setText("Email:");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 12;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_START;
        gridBagConstraints.insets = new java.awt.Insets(5, 0, 0, 0);
        modelInformationPanel.add(jLabel11, gridBagConstraints);

        fName.setMinimumSize(new java.awt.Dimension(200, 19));
        fName.setPreferredSize(new java.awt.Dimension(200, 19));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 9;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(0, 5, 0, 0);
        modelInformationPanel.add(fName, gridBagConstraints);

        lName.setMinimumSize(new java.awt.Dimension(200, 19));
        lName.setPreferredSize(new java.awt.Dimension(200, 19));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 10;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 0, 0);
        modelInformationPanel.add(lName, gridBagConstraints);

        organisation.setMinimumSize(new java.awt.Dimension(200, 19));
        organisation.setPreferredSize(new java.awt.Dimension(200, 19));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 11;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 0, 0);
        modelInformationPanel.add(organisation, gridBagConstraints);

        email.setMinimumSize(new java.awt.Dimension(200, 19));
        email.setPreferredSize(new java.awt.Dimension(200, 19));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 12;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 0, 0);
        modelInformationPanel.add(email, gridBagConstraints);

        saveModeller.setText("Save Modeller");
        saveModeller.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                saveModellerActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 13;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.insets = new java.awt.Insets(5, 0, 0, 0);
        modelInformationPanel.add(saveModeller, gridBagConstraints);

        jLabel14.setText("History");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 15;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.insets = new java.awt.Insets(10, 0, 0, 0);
        modelInformationPanel.add(jLabel14, gridBagConstraints);

        jScrollPane1.setPreferredSize(new java.awt.Dimension(259, 80));

        modellersList.setModel(new DefaultListModel());
        modellersList.setPreferredSize(new java.awt.Dimension(200, 100));
        modellersList.addMouseListener(new ModellersMouseListener(ap, modellersList));
        jScrollPane1.setViewportView(modellersList);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 14;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(5, 0, 0, 0);
        modelInformationPanel.add(jScrollPane1, gridBagConstraints);

        jScrollPane3.setPreferredSize(new java.awt.Dimension(259, 80));

        datesList.setModel(new DefaultListModel());
        datesList.setPreferredSize(new java.awt.Dimension(200, 100));
        datesList.addMouseListener(new DatesMouseListener(ap, datesList));
        jScrollPane3.setViewportView(datesList);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 18;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(5, 0, 0, 0);
        modelInformationPanel.add(jScrollPane3, gridBagConstraints);

        saveDate.setText("Save Date");
        saveDate.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                saveDateActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 17;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.insets = new java.awt.Insets(5, 0, 0, 0);
        modelInformationPanel.add(saveDate, gridBagConstraints);

        jPanel4.setLayout(new java.awt.GridBagLayout());

        jLabel16.setText("Year:");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 5);
        jPanel4.add(jLabel16, gridBagConstraints);

        jLabel17.setText("Month:");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.insets = new java.awt.Insets(0, 5, 0, 5);
        jPanel4.add(jLabel17, gridBagConstraints);

        jLabel18.setText("Day:");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 4;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.insets = new java.awt.Insets(0, 5, 0, 5);
        jPanel4.add(jLabel18, gridBagConstraints);

        year.setMinimumSize(new java.awt.Dimension(40, 19));
        year.setPreferredSize(new java.awt.Dimension(40, 19));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 5);
        jPanel4.add(year, gridBagConstraints);

        month.setMinimumSize(new java.awt.Dimension(25, 19));
        month.setPreferredSize(new java.awt.Dimension(25, 19));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 3;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 5);
        jPanel4.add(month, gridBagConstraints);

        day.setMinimumSize(new java.awt.Dimension(25, 19));
        day.setPreferredSize(new java.awt.Dimension(25, 19));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 5;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        jPanel4.add(day, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 16;
        gridBagConstraints.gridwidth = 2;
        modelInformationPanel.add(jPanel4, gridBagConstraints);

        jLabel15.setText("Qualifier:");
        jLabel15.setToolTipText("A list of all available MIRIAM qualifiers.");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_START;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 5, 0);
        modelInformationPanel.add(jLabel15, gridBagConstraints);

        modelQualifier.setModel(new javax.swing.DefaultComboBoxModel());
        modelQualifier.setMinimumSize(new java.awt.Dimension(200, 24));
        modelQualifier.setPreferredSize(new java.awt.Dimension(200, 24));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 5, 0);
        modelInformationPanel.add(modelQualifier, gridBagConstraints);

        jLabel19.setText("Database:");
        jLabel19.setToolTipText("A list of all databases, a identifier can come from");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_START;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 5, 0);
        modelInformationPanel.add(jLabel19, gridBagConstraints);

        miriamRegistryModel.setModel(new javax.swing.DefaultComboBoxModel());
        miriamRegistryModel.setMinimumSize(new java.awt.Dimension(200, 24));
        miriamRegistryModel.setPreferredSize(new java.awt.Dimension(200, 24));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 5, 0);
        modelInformationPanel.add(miriamRegistryModel, gridBagConstraints);

        jScrollPane4.setMinimumSize(new java.awt.Dimension(237, 90));
        jScrollPane4.setPreferredSize(new java.awt.Dimension(237, 90));

        miriamIdentifiersModel.setModel(new DefaultListModel());
        miriamIdentifiersModel.setToolTipText("A list of all MIRIAM identifiers assigned to the model");
        miriamIdentifiersModel.setMinimumSize(new java.awt.Dimension(150, 150));
        miriamIdentifiersModel.setName("model"); // NOI18N
        miriamIdentifiersModel.setPreferredSize(new java.awt.Dimension(150, 150));
        miriamIdentifiersModel.addMouseListener(new MiriamIdentifiersMouseListener(miriamIdentifiersModel, ap));
        jScrollPane4.setViewportView(miriamIdentifiersModel);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 7;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 5, 0);
        modelInformationPanel.add(jScrollPane4, gridBagConstraints);

        addMiriamIdentifierModel.setText("Add");
        addMiriamIdentifierModel.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                addMiriamIdentifierModelActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 5;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.insets = new java.awt.Insets(5, 0, 5, 0);
        modelInformationPanel.add(addMiriamIdentifierModel, gridBagConstraints);

        jLabel20.setText("ID:");
        jLabel20.setToolTipText("The ID from the selected database");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_START;
        modelInformationPanel.add(jLabel20, gridBagConstraints);

        uriModel.setMinimumSize(new java.awt.Dimension(200, 19));
        uriModel.setPreferredSize(new java.awt.Dimension(200, 19));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 5, 0);
        modelInformationPanel.add(uriModel, gridBagConstraints);

        jLabel7.setText("List of Identifier");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 6;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.insets = new java.awt.Insets(5, 0, 5, 0);
        modelInformationPanel.add(jLabel7, gridBagConstraints);

        jButton1.setText("Close");
        jButton1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton1ActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 19;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.insets = new java.awt.Insets(15, 0, 0, 0);
        modelInformationPanel.add(jButton1, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.weightx = 0.85;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        getContentPane().add(modelInformationPanel, gridBagConstraints);
    }// </editor-fold>//GEN-END:initComponents

    private void saveModellerActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_saveModellerActionPerformed
        LOGGER.info("Saving Modeller action for model");
        if(!fName.getText().isEmpty() && !lName.getText().isEmpty() && !organisation.getText().isEmpty() && !email.getText().isEmpty()) {

            if(selectedModellerIndex != -1) {
                modellersModel.add(selectedModellerIndex, new ModellerWrapper(fName.getText(), lName.getText(), organisation.getText(), email.getText()));
                modellersModel.remove(selectedModellerIndex+1);
            } else {
                modellersModel.addElement(new ModellerWrapper(fName.getText(), lName.getText(), organisation.getText(), email.getText()));
            }

            selectedModellerIndex = -1;

            email.setText("");
            fName.setText("");
            lName.setText("");
            organisation.setText("");
        }
        saveModelInformations();
        LOGGER.info("Successfully saved modeller action for model");
    }//GEN-LAST:event_saveModellerActionPerformed

    private void saveDateActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_saveDateActionPerformed
        LOGGER.info("Saving date action for model");
        if(!month.getText().isEmpty() && !year.getText().isEmpty() && !day.getText().isEmpty()) {
            if(selectedDateIndex != -1) {
                datesModel.add(selectedDateIndex, new DateWrapper(year.getText(), month.getText(), day.getText()));
                datesModel.remove(selectedDateIndex+1);
            } else {
                datesModel.addElement(new DateWrapper(year.getText(), month.getText(), day.getText()));
            }

            selectedDateIndex = -1;

            year.setText("");
            month.setText("");
            day.setText("");
        }
        saveModelInformations();
        LOGGER.info("Successfully saved date action for model");
    }//GEN-LAST:event_saveDateActionPerformed

    private void addMiriamIdentifierModelActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_addMiriamIdentifierModelActionPerformed
        LOGGER.info("Adding MIRIAM identifier for model");
        if(!uriModel.getText().isEmpty()) {
            if(editMiriamIdentifierModel == true) {
                LOGGER.info("Editing a MIRIAM identifier for model");
                MiriamRegistryWrapper mrw = (MiriamRegistryWrapper) miriamRegistryModel.getSelectedItem();

                Matcher m = mrw.getPattern().matcher(uriModel.getText().trim());
                if(!m.matches()) {
                    LOGGER.warn("Invalid accession number");
                    JOptionPane.showMessageDialog(this.netViewer, "Invalid accession number");
                    return;
                }

                List<CVTerm> cvts = petriNet.getProperty(MIRIAM_MODEL_QUALIFIERS);

                // Same identifier = update the uri
                if(identifierInEditModel.getCVTerm().getModelQualifierType().equals((Qualifier) modelQualifier.getSelectedItem())) {
                    LOGGER.info("Same identifier, updating the uri");
                    for(CVTerm cvt : cvts) {
                        if((identifierInEditModel.getCVTerm().getModelQualifierType()).equals(cvt.getModelQualifierType())) {
                            cvt.getResources().set(cvt.getResources().indexOf(identifierInEditModel.getURI()), mrw.getURL()+uriModel.getText().trim());
                            break;
                        }
                    }
                } else { // new identifier = update the identifier and the uri
                    LOGGER.info("New identifier, updating identifier and uri");
                    // first: delete the old one
                    CVTerm toRemove = null;
                    for(CVTerm cvt : cvts) {
                        if((identifierInEditModel.getCVTerm().getModelQualifierType()).equals(cvt.getModelQualifierType())) {
                            cvt.getResources().remove(cvt.getResources().indexOf(identifierInEditModel.getURI()));
                            if(cvt.getResourceCount() == 0) {
                                toRemove = cvt;
                            }
                            break;
                        }
                    }
                    if(toRemove != null) {
                        LOGGER.info("Remove the old one");
                        cvts.remove(toRemove);
                        petriNet.putProperty(MIRIAM_MODEL_QUALIFIERS, cvts);
                    }

                    // now add the new ones
                    LOGGER.info("Adding new ones");
                    boolean qualifierWasThere = false;
                    for(CVTerm cvt : cvts) {
                        if(((Qualifier) modelQualifier.getSelectedItem()).equals(cvt.getModelQualifierType())) {
                            qualifierWasThere = true;
                            cvt.addResource(uriModel.getText().trim());
                        }
                        petriNet.putProperty(MIRIAM_MODEL_QUALIFIERS, cvts);
                    }

                    if(!qualifierWasThere) {
                        MiriamWrapper mw = new MiriamWrapper((Qualifier) modelQualifier.getSelectedItem(), mrw.getURL()+uriModel.getText().trim());
                        ((List<CVTerm>)petriNet.getProperty(MIRIAM_MODEL_QUALIFIERS)).add(mw.getCVTerm());
                    }
                }

                identifierInEditModel.setQualifier((Qualifier) modelQualifier.getSelectedItem());
                identifierInEditModel.setURI(mrw.getURL()+uriModel.getText().trim());

                editMiriamIdentifierModel = false;
                addMiriamIdentifierModel.setText("Add");
                identifierInEditModel = null;
                miriamIdentifiersModel.repaint();
                LOGGER.info("Finished editing MIRIAM identifier for model");
            } else if(editMiriamIdentifierModel == false) {
                LOGGER.info("Adding new MIRIAM identifier for model");
                MiriamRegistryWrapper mrw = (MiriamRegistryWrapper) miriamRegistryModel.getSelectedItem();

                Matcher m = mrw.getPattern().matcher(uriModel.getText().trim());
                if(!m.matches()) {
                    LOGGER.warn("Invalid accession number");
                    JOptionPane.showMessageDialog(this.netViewer, "Invalid accession number");
                    return;
                }

                MiriamWrapper mw = new MiriamWrapper((Qualifier) modelQualifier.getSelectedItem(), mrw.getURL()+uriModel.getText().trim());
                miModelModel.addElement(mw);

                if(!petriNet.hasProperty(MIRIAM_MODEL_QUALIFIERS)) {
                    petriNet.putProperty(MIRIAM_MODEL_QUALIFIERS, new ArrayList<CVTerm>());
                }

                List<CVTerm> cvts = (List<CVTerm>) petriNet.getProperty(MIRIAM_MODEL_QUALIFIERS);
                boolean qualifierWasThere = false;
                for(CVTerm cvt : cvts) {
                    if((mw.getCVTerm().getModelQualifierType()).equals(cvt.getModelQualifierType())) {
                        qualifierWasThere = true;
                        cvt.addResource(uriModel.getText().trim());
                    }
                    petriNet.putProperty(MIRIAM_MODEL_QUALIFIERS, cvts);
                }

                if(!qualifierWasThere) {
                    ((List<CVTerm>)petriNet.getProperty(MIRIAM_MODEL_QUALIFIERS)).add(mw.getCVTerm());
                }
            }
            uriModel.setText("");
        }
        saveModelInformations();
        LOGGER.info("Finished adding new MIRIAM identifier for model");
    }//GEN-LAST:event_addMiriamIdentifierModelActionPerformed

    private void jButton1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton1ActionPerformed
        saveModelInformations();
        netViewer.hideMenu();
    }//GEN-LAST:event_jButton1ActionPerformed


    // Variables declaration - do not modify//GEN-BEGIN:variables
    protected javax.swing.JButton addMiriamIdentifierModel;
    protected javax.swing.JList<DateWrapper> datesList;
    protected javax.swing.JTextField day;
    protected javax.swing.JTextField email;
    protected javax.swing.JTextField fName;
    private javax.swing.JButton jButton1;
    private javax.swing.JLabel jLabel10;
    private javax.swing.JLabel jLabel11;
    private javax.swing.JLabel jLabel12;
    private javax.swing.JLabel jLabel14;
    private javax.swing.JLabel jLabel15;
    private javax.swing.JLabel jLabel16;
    private javax.swing.JLabel jLabel17;
    private javax.swing.JLabel jLabel18;
    private javax.swing.JLabel jLabel19;
    private javax.swing.JLabel jLabel20;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JLabel jLabel9;
    private javax.swing.JPanel jPanel4;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane3;
    private javax.swing.JScrollPane jScrollPane4;
    protected javax.swing.JTextField lName;
    protected javax.swing.JList miriamIdentifiersModel;
    protected javax.swing.JComboBox miriamRegistryModel;
    private javax.swing.JPanel modelInformationPanel;
    protected javax.swing.JTextField modelName;
    protected javax.swing.JComboBox modelQualifier;
    protected javax.swing.JList<ModellerWrapper> modellersList;
    protected javax.swing.JTextField month;
    protected javax.swing.JTextField organisation;
    private javax.swing.JButton saveDate;
    private javax.swing.JButton saveModeller;
    protected javax.swing.JTextField uriModel;
    protected javax.swing.JTextField year;
    // End of variables declaration//GEN-END:variables
}
