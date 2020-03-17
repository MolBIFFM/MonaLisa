/*
 *
 *  This file is part of the software MonaLisa.
 *  MonaLisa is free software, dependent on non-free software. For more information read LICENCE and README.
 *
 *  (c) Department of Molecular Bioinformatics, Institute of Computer Science, Johann Wolfgang
 *  Goethe-University Frankfurt am Main, Germany
 *
 */

package monalisa.gui;

import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JList;
import javax.swing.JPanel;
import layout.TableLayout;
import monalisa.data.input.ApnnInputHandler;
import monalisa.data.input.InputHandler;
import monalisa.data.input.MetaToolInputHandler;
import monalisa.data.input.PetriNetInputHandlers;
import monalisa.data.input.Pipe2InputHandler;
import monalisa.data.input.Pipe3InputHandler;
import monalisa.data.input.Pipe4InputHandler;
import monalisa.data.input.SbmlInputHandler;
import monalisa.data.input.SppedInputHandler;
import monalisa.data.output.ApnnOutputHandler;
import monalisa.data.output.MetaToolOutputHandler;
import monalisa.data.output.OutputHandler;
import monalisa.data.output.PetriNetOutputHandlers;
import monalisa.data.output.Pipe3OutputHandler;
import monalisa.data.output.Pipe4OutputHandler;
import monalisa.data.output.PntOutputHandler;
import monalisa.data.output.SbmlOutputHandler;
import monalisa.data.pn.PetriNet;
import monalisa.util.MonaLisaFileChooser;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

/**
 *
 * @author Jens Einloft
 */
public class ConverterDialog extends JDialog implements ActionListener {

    private static final String ACTION_OK = "ACTION_OK";
    private static final String ACTION_CANCEL = "ACTION_CANCEL";
    private static final String CHOOSE_INPUT = "CHOOSE_INPUT";
    private static final String CHOOSE_OUTPUT = "CHOOSE_OUTPUT";

    private JPanel panel;
    private JButton chooseInput, chooseOutput, start, cancel;
    private JList inputFormat, outputFormat;
    private DefaultListModel inputListModel;
    private DefaultListModel outputListModel;

    private File inputDirectory, outputDirectory;

    private static final Logger LOGGER = LogManager.getLogger(ConverterDialog.class);

    public ConverterDialog(MainDialog owner) {
        super(owner, true);
        setTitle("MonaLisa - Petri net converter");
        initComponents();
        setLocationRelativeTo(owner);
        setVisible(true);
    }

    private void initComponents() {
        this.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                setVisible(false);
            }
        });

        chooseInput = new JButton("Choose input directory");
        chooseInput.setActionCommand(CHOOSE_INPUT);
        chooseInput.addActionListener(this);
        chooseInput.setForeground(Color.RED);

        chooseOutput = new JButton("Choose output directory");
        chooseOutput.setActionCommand(CHOOSE_OUTPUT);
        chooseOutput.addActionListener(this);
        chooseOutput.setForeground(Color.RED);

        start = new JButton("Start");
        start.setActionCommand(ACTION_OK);
        start.addActionListener(this);

        cancel = new JButton("Cancel");
        cancel.setActionCommand(ACTION_CANCEL);
        cancel.addActionListener(this);

        inputFormat = new JList();
        outputFormat = new JList();

        inputListModel = new DefaultListModel();
        outputListModel = new DefaultListModel();

        inputFormat = new JList(inputListModel);
        outputFormat = new JList(outputListModel);

        inputListModel.addElement("Choose input format:");
        for (final InputHandler handler : PetriNetInputHandlers.getHandlers()) {
            inputListModel.addElement(handler.getDescription());
        }

        outputListModel.addElement("Choose output format:");
        for (final OutputHandler handler : PetriNetOutputHandlers.getHandlers()) {
            outputListModel.addElement(handler.getDescription());
        }

        double size[][] = {{10, TableLayout.PREFERRED, 20, TableLayout.PREFERRED, 10},
                          {10, TableLayout.PREFERRED, 15, TableLayout.PREFERRED, 15, TableLayout.PREFERRED, 10}};

        TableLayout tl = new TableLayout(size);

        panel = new JPanel();
        panel.setLayout(tl);

        panel.add(chooseInput, "1,1");
        panel.add(chooseOutput, "3,1");

        panel.add(inputFormat, "1,3");
        panel.add(outputFormat, "3,3");

        panel.add(start, "1,5");
        panel.add(cancel, "3,5");

        add(panel);

        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        pack();
    }

    private void convert() throws IOException {
        LOGGER.info("Converting");
        File[] inputFileList;
        InputHandler ih = getInputHandler((String) inputFormat.getSelectedValue());
        OutputHandler oh = getOutputHandler((String) outputFormat.getSelectedValue());
        PetriNet pn;
        String outputFileName;
        if(outputDirectory != null || inputDirectory != null) {
            inputFileList = inputDirectory.listFiles();

            for(File f : inputFileList) {
                if(ih.isKnownFile(f)) {
                    outputFileName = outputDirectory.getAbsolutePath()+f.getAbsolutePath().substring(f.getAbsolutePath().lastIndexOf(System.getProperty("file.separator")), f.getAbsolutePath().lastIndexOf(".")) + "." + oh.getExtension();
                    pn = ih.load(new FileInputStream(f));
                    oh.save(new FileOutputStream(new File(outputFileName)), pn);
                }
            }
        }
        LOGGER.info("Successfully converted");
        setVisible(false);
    }

    private void chooseInput() {
        LOGGER.info("Choosing input directory for converter");
        MonaLisaFileChooser fc = new MonaLisaFileChooser();
        fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);

        if (fc.showOpenDialog(this) != JFileChooser.APPROVE_OPTION)
            return;

        inputDirectory = fc.getSelectedFile();
        chooseInput.setForeground(Color.GREEN);
        LOGGER.info("Successfully chosen input directory: " + inputDirectory.getName());
    }

    private void chooseOutput() {
        LOGGER.info("Choosing output directory for converter");
        MonaLisaFileChooser fc = new MonaLisaFileChooser();
        fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);

        if (fc.showOpenDialog(this) != JFileChooser.APPROVE_OPTION)
            return;

        outputDirectory = fc.getSelectedFile();
        chooseOutput.setForeground(Color.GREEN);
        LOGGER.info("Successfully chosen output directory: " + outputDirectory.getName());
    }

    public void actionPerformed(ActionEvent e) {
        final String action = e.getActionCommand();
        switch (action) {
            case ACTION_OK:
                try {
                    convert();
                } catch (IOException ex) {
                    LOGGER.error("Caught IOException while trying to convert Petri net", ex);
                }
                break;
            case ACTION_CANCEL:
                setVisible(false);
                break;
            case CHOOSE_INPUT:
                chooseInput();
                break;
            case CHOOSE_OUTPUT:
                chooseOutput();
                break;
        }
    }


    private InputHandler getInputHandler(String name) {
        InputHandler ih = null;
        LOGGER.debug("Getting input handler for name '" + name + "'");
        if(name.equalsIgnoreCase("MetaTool")) {
            ih = new MetaToolInputHandler();
        } else if (name.equals("Pipe2")) {
            ih = new Pipe2InputHandler();
        } else if (name.equals("Pipe3")) {
            ih = new Pipe3InputHandler();
        } else if (name.equals("Pipe4")) {
           ih = new Pipe4InputHandler();
        } else if (name.equals("Sbml")) {
            ih = new SbmlInputHandler();
        } else if (name.equals("Spped")) {
            ih = new SppedInputHandler();
        } else if (name.equals("Apnn")) {
            ih = new ApnnInputHandler();
        }
        LOGGER.debug("Successfully got input handler for name '" + name + "'");
        return ih;
    }

    private OutputHandler getOutputHandler(String name) {
        OutputHandler oh = null;
        LOGGER.debug("Getting output handler for name '" + name + "'");
        if(name.equalsIgnoreCase("MetaTool")) {
            oh = new MetaToolOutputHandler();
        } else if (name.equals("Pipe2")) {
            oh = new Pipe3OutputHandler();
        } else if (name.equals("Pipe3")) {
            oh = new Pipe3OutputHandler();
        } else if (name.equals("Pipe4")) {
           oh = new Pipe4OutputHandler();
        } else if (name.equals("Sbml")) {
            oh = new SbmlOutputHandler(3,1);
        } else if (name.equals("Apnn")) {
            oh = new ApnnOutputHandler();
        } else if (name.equals("Pnt")) {
            oh = new PntOutputHandler();
        }
        LOGGER.debug("Successfully got output handler for name '" + name + "'");
        return oh;
    }
}
