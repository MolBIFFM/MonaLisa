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

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.event.*;
import java.io.*;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import javax.swing.*;
import javax.swing.filechooser.FileFilter;
import layout.TableLayout;
import monalisa.MonaLisa;
import monalisa.Project;
import monalisa.Settings;
import monalisa.ToolStatusUpdateEvent;
import monalisa.ToolStatusUpdateListener;
import monalisa.addons.AddonPanel;
import monalisa.addons.Addons;
import monalisa.addons.netviewer.NetViewer;
import monalisa.addons.netviewer.listener.NetChangedListener;
import monalisa.addons.treeviewer.TreeViewer;
import monalisa.util.OutputFileFilter;
import monalisa.data.Pair;
import monalisa.data.PropertyList;
import monalisa.data.input.InputHandler;
import monalisa.data.input.PetriNetInputHandlers;
import monalisa.data.input.TInputHandler;
import monalisa.data.input.TInputHandlers;
import monalisa.data.output.OutputHandler;
import monalisa.data.output.PetriNetOutputHandlers;
import monalisa.data.pn.PetriNetFacade;
import monalisa.gui.components.SplashScreen;
import monalisa.gui.components.StatusBar;
import monalisa.resources.ResourceManager;
import monalisa.resources.StringResources;
import monalisa.results.Configuration;
import monalisa.results.Result;
import monalisa.tools.ErrorLog;
import monalisa.tools.Tool;
import monalisa.tools.cluster.ClusterTool;
import monalisa.tools.mcs.McsTool;
import monalisa.tools.mcts.MctsTool;
import monalisa.tools.pinv.PInvariantTool;
import monalisa.tools.tinv.TInvariantTool;
import monalisa.util.FileUtils;
import monalisa.util.MonaLisaFileChooser;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

@SuppressWarnings("serial")
public final class MainDialog extends JFrame implements ActionListener, HierarchyBoundsListener, ToolStatusUpdateListener, NetChangedListener {

    private static final String MENU_FILE_NEW_ACTION = "MENU_FILE_NEW_ACTION";
    private static final String MENU_FILE_OPEN_ACTION = "MENU_FILE_OPEN_ACTION";
    private static final String MENU_FILE_SAVE_ACTION = "MENU_FILE_SAVE_ACTION";
    private static final String MENU_FILE_EXPORT_ACTION = "MENU_FILE_EXPORT_ACTION";
    private static final String MENU_FILE_EXPORT_PETRINET_ACTION = "MENU_FILE_EXPORT_PETRINET_ACTION";
    private static final String MENU_TFILE_LOAD_ACTION = "MENU_TFILE_LOAD_ACTION";
    private static final String MENU_FILE_EXIT_ACTION = "MENU_FILE_EXIT_ACTION";
    private static final String MENU_PROJECT_RUN = "MENU_PROJECT_RUN";
    private static final String MENU_PROJECT_STOP = "MENU_PROJECT_STOP";
    private static final String MENU_SHOW_NV = "MENU_SHOW_NV";
    private static final String MENU_SHOW_TV = "MENU_SHOW_TV";
    private static final String EMPTY_PROJECT = "EMPTY_PROJECT";
    private static final String MENU_FILE_SAVE_AS_ACTION = "MENU_FILE_SAVE_AS_ACTION";
    private static final String ABOUT_ACTION = "ABOUT_ACTION";
    private static final String HELP_ACTION = "HELP_ACTION";
    private static final String MENU_FILE_CONVERTER_ACTION = "MENU_FILE_CONVERTER_ACTION";

    private static final Dimension DEFAULT_DIMENSION = new Dimension(400, 500);

    private static final ResourceManager resources = ResourceManager.instance();
    private static final StringResources strings = resources.getDefaultStrings();

    private static final Logger LOGGER = LogManager.getLogger(MainDialog.class);

    // Addons
    private NetViewer netViewer;
    private TreeViewer treeViewer;

    private JMenuBar menuBar;
    private JMenu mainMenu, projectMenu, helpMenu, menuFileOpenRecently;
    private JMenuItem menuFileNew, menuFileImport, menuFileOpen, menuFileSave, menuFileSaveAs,
                      menuTFileLoad, menuFileExportResults, menuFileExportPetriNet,
                      menuFileExit, menuProjectRun, menuHelpAbout, menuHelpHelp, menuConverter;

    private JButton fileNewButton, fileOpenButton, fileSaveButton, fileExportResultsButton,
                    fileExportPetriNetButton, projectRunButton, showNVButton, showTVButton,
                    loadProject, emptyProject, newProject;

    private Icon fileNewIcon, fileOpenIcon, fileSaveIcon, fileExportResultsIcon,
                 fileExportPetriNetIcon, projectRunIcon, projectStopIcon, showNVIcon,
                 showTVIcon;

    private JToolBar toolBar;
    private JPanel mainContainer, contentPanel, buttonContainer;
    private JScrollPane scrollPane;
    private StatusBar statusBar;
    private SplashScreen splash;

    private String documentTitle;
    private Project project;

    public MainDialog() {
        LOGGER.info("Initializing MainDialog");
//        try {
//            System.setErr(new PrintStream(new FileOutputStream(System.getProperty("user.home")+"/MonaLisaErrorLog.text")));
//        } catch (FileNotFoundException ex) {
//            ex.printStackTrace();
//        }
        addWindowListener(new WindowAdapter (){
            @Override
            public void windowClosing(WindowEvent e) {
                exitApplication();
            }
        });

        initComponent();
        LOGGER.info("Successfully initialized MainDialog");
    }

    private void initComponent() {
        setDocumentTitle(null);
        setIconImage(resources.getImage("icon-16.png"));

        initIcons();
        initMenuBar();
        initToolBar();

        addKeyListener(new KeyListener() {
            private Integer keyCode;

            @Override
            public void keyTyped(KeyEvent e) { }

            @Override
            public void keyPressed(KeyEvent e) { }

            @Override
            public void keyReleased(KeyEvent e) {
                keyCode = e.getKeyCode();

                if(keyCode.equals(KeyEvent.VK_S) && e.isControlDown()) {
                    try {
                        project.save();
                    } catch (IOException ex) {
                        LOGGER.error("Caught IOException while trying to save project on Ctrl + S: ", ex);
                    }
                }
            }

        });

        splash = new SplashScreen();
        splash.setPreferredSize(DEFAULT_DIMENSION);

        loadProject = new JButton();
        loadProject.setText(strings.get("LoadProject"));
        loadProject.setActionCommand(MENU_FILE_OPEN_ACTION);
        loadProject.addActionListener(this);

        emptyProject = new JButton();
        emptyProject.setText(strings.get("CreateEmptyProject"));
        emptyProject.setActionCommand(EMPTY_PROJECT);
        emptyProject.addActionListener(this);

        newProject = new JButton();
        newProject.setText(strings.get("CreateNewProject"));
        newProject.setActionCommand(MENU_FILE_NEW_ACTION);
        newProject.addActionListener(this);

        double size[][] =
        {{TableLayout.FILL, TableLayout.PREFERRED, TableLayout.FILL},
        {5, TableLayout.PREFERRED, 5, TableLayout.PREFERRED, 5, TableLayout.PREFERRED}};
        splash.setLayout (new TableLayout(size));
        splash.add(emptyProject ,"1,1");
        splash.add(newProject ,"1,3");
        splash.add(loadProject ,"1,5");

        contentPanel = new JPanel();
        contentPanel.setLayout(new GroupLayout(contentPanel));
        scrollPane =
            new JScrollPane(contentPanel,
                JScrollPane.VERTICAL_SCROLLBAR_ALWAYS,
                JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scrollPane.setPreferredSize(DEFAULT_DIMENSION);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        scrollPane.getViewport().addHierarchyBoundsListener(this);

        mainContainer = new JPanel();
        mainContainer.setLayout(new BoxLayout(mainContainer,  BoxLayout.PAGE_AXIS));
        mainContainer.add(Box.createRigidArea(new Dimension(0, 3)));
        mainContainer.add(splash);

        statusBar = new StatusBar();

        buttonContainer = new JPanel();
        buttonContainer.setLayout(new BorderLayout());
        buttonContainer.add(projectRunButton, BorderLayout.WEST);
        buttonContainer.add(fileExportResultsButton, BorderLayout.CENTER);
        buttonContainer.add(this.fileExportPetriNetButton, BorderLayout.EAST);
        buttonContainer.add(statusBar, BorderLayout.SOUTH);

        Container contentPane = getContentPane();
        contentPane.setLayout(new BorderLayout());
        contentPane.add(toolBar, BorderLayout.NORTH);
        contentPane.add(mainContainer);
        contentPane.add(buttonContainer, BorderLayout.SOUTH);

        pack();
        projectClosed();

        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        setLocationRelativeTo(null);
        setVisible(true);
    }

    @Override
    public void ancestorResized(HierarchyEvent e) {
        if (e.getChanged() == scrollPane) {
            for (Component comp : contentPanel.getComponents()) {
                Dimension newSize = new Dimension(scrollPane.getViewport().getWidth(), comp.getHeight());
                comp.setSize(newSize);
                comp.setPreferredSize(newSize);
            }
        }
    }

    // Not needed.
    @Override
    public void ancestorMoved(HierarchyEvent e) { }

    private void initIcons() {
        fileNewIcon = resources.getIcon("new_project.png");
        fileOpenIcon = resources.getIcon("open_project.png");
        fileSaveIcon = resources.getIcon("save_project.png");
        fileExportResultsIcon = resources.getIcon("export_results.png");
        fileExportPetriNetIcon = resources.getIcon("export.png");
        projectRunIcon = resources.getIcon("run_tools.png");
        projectStopIcon = resources.getIcon("stop_tools.png");
        showNVIcon = resources.getIcon("netviewer.png");
        showTVIcon = resources.getIcon("treeviewer.png");
    }

    private void initMenuBar() {
        menuFileNew = new JMenuItem(strings.get("FileNew"), fileNewIcon);
        menuFileNew.setActionCommand(EMPTY_PROJECT);
        menuFileNew.addActionListener(this);

        menuFileImport = new JMenuItem(strings.get("CreateNewProject"), resources.getIcon("import.png"));
        menuFileImport.setActionCommand(MENU_FILE_NEW_ACTION);
        menuFileImport.addActionListener(this);

        menuFileOpen = new JMenuItem(strings.get("FileOpen"), fileOpenIcon);
        menuFileOpen.setActionCommand(MENU_FILE_OPEN_ACTION);
        menuFileOpen.addActionListener(this);

        menuFileOpenRecently = new JMenu(strings.get("FileOpenRecently"));
        menuFileOpenRecently.setIcon(fileOpenIcon);

        String recentlyProjects = Settings.get("recentlyProjects");

        if(!recentlyProjects.isEmpty()) {
            String projects[] = recentlyProjects.split(",");

            for(int i = projects.length-1; i >= 0; i--) {
                JMenuItem item = new JMenuItem();

                if(projects[i].length() > 40 ) {
                    String subString = projects[i].substring(projects[i].lastIndexOf(System.getProperty("file.separator"))+1, projects[i].length());
                     if(subString.length() <= 40) {
                        item.setText(".../"+subString);
                    } else {
                        item.setText("..."+projects[i].substring(projects[i].length()-31, projects[i].length()));
                    }
                } else {
                   item.setText(projects[i]);
                }

                item.setToolTipText(projects[i]);

                item.addActionListener(new ActionListener() {
                     @Override
                     public void actionPerformed(ActionEvent e) {
                        File file = new File(((JMenuItem)e.getSource()).getToolTipText());
                        if(file.exists()) {
                            try {
                                openProject(file);
                            } catch (IOException | ClassNotFoundException | InterruptedException ex) {
                                LOGGER.error("Caught Exception while trying to open project from file: ", ex);
                            }
                        } else {
                            JOptionPane.showMessageDialog(((JMenuItem)e.getSource()).getRootPane(), "This project file is not existing");
                        }
                     }
                });

                menuFileOpenRecently.add(item);
            }
        }

        menuConverter = new JMenuItem(strings.get("FileConverter"), resources.getIcon("open_project.png"));
        menuConverter.setActionCommand(MENU_FILE_CONVERTER_ACTION);
        menuConverter.addActionListener(this);

        menuFileSave = new JMenuItem(strings.get("FileSave"), fileSaveIcon);
        menuFileSave.setActionCommand(MENU_FILE_SAVE_ACTION);
        menuFileSave.addActionListener(this);

        menuFileSaveAs = new JMenuItem(strings.get("FileSaveAs"), fileSaveIcon);
        menuFileSaveAs.setActionCommand(MENU_FILE_SAVE_AS_ACTION);
        menuFileSaveAs.addActionListener(this);

        menuTFileLoad = new JMenuItem(strings.get("TFileLoad"), fileOpenIcon);
        menuTFileLoad.setActionCommand(MENU_TFILE_LOAD_ACTION);
        menuTFileLoad.addActionListener(this);

        menuFileExportResults = new JMenuItem(strings.get("FileExportTT"), fileExportResultsIcon);
        menuFileExportResults.setActionCommand(MENU_FILE_EXPORT_ACTION);
        menuFileExportResults.addActionListener(this);

        menuFileExportPetriNet = new JMenuItem(strings.get("ExportPetriNetTT"), fileExportPetriNetIcon);
        menuFileExportPetriNet.setActionCommand(MENU_FILE_EXPORT_PETRINET_ACTION);
        menuFileExportPetriNet.addActionListener(this);

        menuFileExit = new JMenuItem(strings.get("FileExit"), resources.getIcon("delete.png"));
        menuFileExit.setActionCommand(MENU_FILE_EXIT_ACTION);
        menuFileExit.addActionListener(this);

        mainMenu = new JMenu(strings.get("File"));
        mainMenu.add(menuFileNew);
        mainMenu.add(menuFileImport);
        mainMenu.add(menuFileOpen);
        mainMenu.add(menuFileOpenRecently);
        mainMenu.add(menuConverter);
        mainMenu.add(menuTFileLoad);
        mainMenu.add(menuFileSave);
        mainMenu.add(menuFileSaveAs);
        mainMenu.addSeparator();
        mainMenu.add(menuFileExportPetriNet);
        mainMenu.add(menuFileExportResults);
        mainMenu.addSeparator();
        mainMenu.add(menuFileExit);

        menuProjectRun = new JMenuItem(strings.get("ProjectRun"), projectRunIcon);
        menuProjectRun.setActionCommand(MENU_PROJECT_RUN);
        menuProjectRun.addActionListener(this);

        projectMenu = new JMenu(strings.get("Project"));
        projectMenu.add(menuProjectRun);

        menuHelpHelp = new JMenuItem(strings.get("Help"), resources.getIcon("help.png"));
        menuHelpHelp.setActionCommand(HELP_ACTION);
        menuHelpHelp.addActionListener(this);

        menuHelpAbout = new JMenuItem(strings.get("About"), resources.getIcon("about.png"));
        menuHelpAbout.setActionCommand(ABOUT_ACTION);
        menuHelpAbout.addActionListener(this);

        helpMenu = new JMenu(strings.get("Help"));
        helpMenu.add(menuHelpHelp);
        helpMenu.add(menuHelpAbout);

        menuBar = new JMenuBar();
        menuBar.add(mainMenu);
        menuBar.add(projectMenu);
        menuBar.add(helpMenu);

        setJMenuBar(menuBar);
    }

    private void initToolBar() {
        fileNewButton = new JButton(fileNewIcon);
        fileNewButton.setToolTipText(strings.get("FileNew"));
        fileNewButton.setActionCommand(EMPTY_PROJECT);
        fileNewButton.addActionListener(this);

        fileOpenButton = new JButton(fileOpenIcon);
        fileOpenButton.setToolTipText(strings.get("FileOpen"));
        fileOpenButton.setActionCommand(MENU_FILE_OPEN_ACTION);
        fileOpenButton.addActionListener(this);

        fileSaveButton = new JButton(fileSaveIcon);
        fileSaveButton.setToolTipText(strings.get("FileSave"));
        fileSaveButton.setActionCommand(MENU_FILE_SAVE_ACTION);
        fileSaveButton.addActionListener(this);

        fileExportResultsButton = new JButton(strings.get("FileExport"), fileExportResultsIcon);
        fileExportResultsButton.setToolTipText(strings.get("FileExportTT"));
        fileExportResultsButton.setActionCommand(MENU_FILE_EXPORT_ACTION);
        fileExportResultsButton.addActionListener(this);

        fileExportPetriNetButton = new JButton(strings.get("ExportPetriNet"), fileExportPetriNetIcon);
        fileExportPetriNetButton.setToolTipText(strings.get("ExportPetriNetTT"));
        fileExportPetriNetButton.setActionCommand(MENU_FILE_EXPORT_PETRINET_ACTION);
        fileExportPetriNetButton.addActionListener(this);

        projectRunButton = new JButton(strings.get("ProjectRun"), projectRunIcon);
        projectRunButton.setToolTipText(strings.get("ProjectRun"));
        projectRunButton.setActionCommand(MENU_PROJECT_RUN);
        projectRunButton.addActionListener(this);

        showNVButton = new JButton(showNVIcon);
        showNVButton.setToolTipText(strings.get("NVButton"));
        showNVButton.setActionCommand(MENU_SHOW_NV);
        showNVButton.addActionListener(this);

        showTVButton = new JButton(showTVIcon);
        showTVButton.setToolTipText(strings.get("TVButton"));
        showTVButton.setActionCommand(MENU_SHOW_TV);
        showTVButton.addActionListener(this);
        showTVButton.setEnabled(false);

        toolBar = new JToolBar();
        toolBar.setFloatable(false);
        toolBar.setRollover(true);
        toolBar.add(fileNewButton);
        toolBar.add(fileOpenButton);
        toolBar.add(fileSaveButton);
        toolBar.addSeparator();
        toolBar.addSeparator();
        toolBar.add(showNVButton);
        toolBar.add(showTVButton);
        toolBar.add(Box.createHorizontalGlue());
    }

    public String getDocumentTitle() {
        return documentTitle;
    }

    public void setDocumentTitle(String documentTitle) {
        this.documentTitle = documentTitle;
        String doc = getDocumentTitle() == null ? strings.get("NoProject") : getDocumentTitle().equals("") ? strings.get("EmptyProject") : getDocumentTitle();
        setTitle(MonaLisa.APPLICATION_TITLE + " – " + doc);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        final String action = e.getActionCommand();
        LOGGER.debug("Action registered in MainDialog: " + action);
        switch (action) {
            case MENU_FILE_EXIT_ACTION:
                exitApplication();
                break;
            case MENU_FILE_NEW_ACTION:
                try {
                    createNewProject();
                } catch (InterruptedException | ClassNotFoundException | IOException ex) {
                    LOGGER.error("Caught exception while trying to create new project from Petri net: ", ex);
                }
                break;
            case MENU_FILE_OPEN_ACTION:
                try {
                    openProject();
                } catch (InterruptedException | ClassNotFoundException | IOException ex) {
                    LOGGER.error("Caught exception while trying to open project from file: ", ex);
                }
                break;
            case MENU_FILE_SAVE_ACTION:
                try {
                    saveProject();
                } catch (ClassNotFoundException | IOException ex) {
                    LOGGER.error("Caught exception while trying to save project: ", ex);
                }
                break;
            case MENU_FILE_SAVE_AS_ACTION:
                try {
                    createNewProject(project);
                } catch (InterruptedException | IOException | ClassNotFoundException ex) {
                    LOGGER.error("Caught exception while trying to save project as: ", ex);
                }
                break;
            case MENU_FILE_EXPORT_ACTION:
                exportResults();
                break;
            case MENU_FILE_EXPORT_PETRINET_ACTION:
                try {
                    exportPetriNet();
                } catch (IOException ex) {
                    LOGGER.error("Caught IOException while trying to export Petri net: ", ex);
                }
                break;
            case MENU_PROJECT_RUN:
                runSelectedTools();
                break;
            case MENU_PROJECT_STOP:
                stopRunningTools();
                break;
            case MENU_SHOW_NV:
                showNV();
                break;
            case MENU_SHOW_TV:
                showTV();
                break;
            case EMPTY_PROJECT:
                try {
                    createEmptyProject();
                } catch (ClassNotFoundException | IOException | InterruptedException ex) {
                    LOGGER.error("Caught exception while trying to create empty project: ", ex);
                }
                break;
            case MENU_TFILE_LOAD_ACTION:
                try {
                    loadTFile();
                } catch (InterruptedException ex) {
                    LOGGER.error("Caught InterruptedException while trying to load EM file");
                }
                break;
            case HELP_ACTION:
                HelpDialog hd = new HelpDialog();
                hd.setVisible(true);
                break;
            case MENU_FILE_CONVERTER_ACTION:
                converter();
                break;
            case ABOUT_ACTION:
                AboutDialog ad = new AboutDialog();
                ad.setVisible(true);
                break;
        }
    }

    private void converter() {
        LOGGER.info("Started ConverterDialog");
        ConverterDialog dialog = new ConverterDialog(MonaLisa.appMainWindow());
        LOGGER.info("Ended ConverterDialog");
    }

    private void runSelectedTools() {
        if(!this.project.getPetriNet().places().isEmpty() && !this.project.getPetriNet().transitions().isEmpty()) {
            setRunButtons(MENU_PROJECT_STOP, strings.get("StopRunningTools"), projectStopIcon);
            statusBar.setProgressValue(0);
            statusBar.setIndeterminateProgress(true);
            statusBar.setProgressVisible(true);
            project.runSelectedTools();
            project.setProjectChanged(true);
        }
    }

    private void stopRunningTools() {
        project.stopRunningTools();
        restoreRunButtons();
    }

    private void restoreRunButtons() {
        setRunButtons(MENU_PROJECT_RUN, strings.get("ProjectRun"), projectRunIcon);
        statusBar.setProgressVisible(false);
    }

    private void setRunButtons(String action, String text, Icon icon) {
        menuProjectRun.setIcon(icon);
        menuProjectRun.setText(text);
        menuProjectRun.setActionCommand(action);
        projectRunButton.setIcon(icon);
        projectRunButton.setToolTipText(text);
        projectRunButton.setActionCommand(action);
    }

    private void exitApplication() {
        LOGGER.info("Starting shutdown process for MonaLisa");
        if(this.project != null) {
            if(this.project.isProjectChanged()) {
                JOptionPane optionPane = new JOptionPane(strings.get("SaveQuestion"), JOptionPane.QUESTION_MESSAGE, JOptionPane.YES_NO_CANCEL_OPTION);
                JDialog dialog = optionPane.createDialog(this, "Wait a moment...");
                dialog.setVisible(true);
                Integer selectedValue = (Integer) optionPane.getValue();

                if(selectedValue != null) {
                    if(selectedValue == JOptionPane.YES_OPTION) {
                        try {
                            LOGGER.info("Saving project on application exit");
                            if(project.getPath() != null)  {
                                project.save(project.getPath());
                            } else {
                                project.save();
                            }
                            LOGGER.info("Successfully saved project on application exit");
                        } catch (IOException ex) {
                            LOGGER.error("Caught IOException while trying to save project on application exit: ", ex);
                        }
                    } else if(selectedValue == JOptionPane.CANCEL_OPTION) {
                        LOGGER.info("Stopped shutdown process for MonaLisa");
                        return;
                    }
                }
                else {
                    LOGGER.info("Stopped shutdown process for MonaLisa");
                    return;
                }
            }
        }
        LOGGER.info("Shutdown process successful");
        System.exit(0);
    }

    private boolean askForSavingTheProject() {
        if(project != null) {
            JOptionPane optionPane = new JOptionPane(strings.get("SaveQuestion"), JOptionPane.QUESTION_MESSAGE, JOptionPane.YES_NO_OPTION);
            JDialog dialog = optionPane.createDialog(this, "Wait a moment...");
            dialog.setVisible(true);
            Integer selectedValue = (Integer) optionPane.getValue();

            if (selectedValue == JOptionPane.YES_OPTION) {
                return true;
            } else {
                return false;
            }
        }
        return false;
    }

    /**
     * Create a new and empty project
     */
    private void createEmptyProject() throws ClassNotFoundException, IOException, InterruptedException {
        LOGGER.info("Creating new empty project");
        askForSavingTheProject();
        project = new Project();
        projectLoaded();
        LOGGER.info("Successfully created new empty project");
    }

    /***
     * Creates a new project from a chosen Petri net file
     * @throws IOException
     * @throws ClassNotFoundException
     * @throws InterruptedException
     */
    private void createNewProject() throws IOException, ClassNotFoundException, InterruptedException {
        LOGGER.info("Creating new project from a chosen Petri net file");
        if(askForSavingTheProject()) {
            saveProject();
        }

        // PN File
        MonaLisaFileChooser petriNetFileChooser = new MonaLisaFileChooser();

        for (final InputHandler handler : PetriNetInputHandlers.getHandlers()) {
            petriNetFileChooser.addChoosableFileFilter(new FileFilter() {
                @Override
                public String getDescription() {
                    return strings.get(handler.getClass().getSimpleName()
                        .replaceAll("InputHandler", "FileType"));
                }
                @Override
                public boolean accept(File f) {
                    try {
                        return f.isDirectory() || handler.isKnownFile(f);
                    } catch (IOException e) {
                        LOGGER.error("Caught IOException while checking whether file is acceptable: ", e);
                        return false;
                    }
                }
            });
        }

        petriNetFileChooser.setFileFilter(new FileFilter() {
            @Override
            public String getDescription() {
                return strings.get("PetriNetFileType");
            }

            @Override
            public boolean accept(File f) {
                try {
                    return f.isDirectory() || PetriNetInputHandlers.isKnownFile(f);
                } catch (IOException e) {
                    LOGGER.error("Caught IOException while checking whether file is acceptable: ", e);
                    return false;
                }
            }
        });

        petriNetFileChooser.setDialogTitle(strings.get("ChooseAPetriNetFile"));
        if (petriNetFileChooser.showOpenDialog(this) != JFileChooser.APPROVE_OPTION)
            return;

        File petriNetFile = petriNetFileChooser.getSelectedFile();

        project = Project.create(petriNetFile);
        project.getPetriNet().putProperty("new_imported", true);

        projectLoaded();
        LOGGER.info("Successfully created new project from a chosen Petri net file");
    }

    /**
     * "Save project as...". Creates a new project from the current project.
     * @param oldProject
     */
    public void createNewProject(Project oldProject) throws IOException, ClassNotFoundException, InterruptedException {
        File newProjectFile = null;

        if(oldProject != null) {
            LOGGER.info("Creating new project from current project to save project as");
            MonaLisaFileChooser projectLocationChooser = new MonaLisaFileChooser();
                    projectLocationChooser.setFileFilter(new FileFilter() {
                        @Override
                        public boolean accept(File f) {
                            return f.isDirectory()
                                || Project.FILENAME_EXTENSION.equalsIgnoreCase(
                                    FileUtils.getExtension(f));
                        }

                        @Override
                        public String getDescription() {
                            return strings.get("ProjectFileType");
                        }
                    });

            projectLocationChooser.setDialogTitle(strings.get("NewProjectLocation"));
            if (projectLocationChooser.showDialog(this, strings.get("ChooseProjectLocationButton")) != JFileChooser.APPROVE_OPTION)
                return;

            newProjectFile = projectLocationChooser.getSelectedFile();

            if (!Project.FILENAME_EXTENSION.equalsIgnoreCase(FileUtils.getExtension(newProjectFile)))
                newProjectFile = new File(newProjectFile.getAbsolutePath() + "." + Project.FILENAME_EXTENSION);

            Project newProject = Project.create(oldProject, newProjectFile);
            this.project = newProject;
            newProject.save(newProjectFile);
            projectLoaded();
            LOGGER.info("Successfully created new project from current project to save project as");
        }
    }

    private void loadTFile() throws InterruptedException {
        LOGGER.info("Loading T-Invariant file");
        MonaLisaFileChooser TfileLocationChooser = new MonaLisaFileChooser();

        for (final TInputHandler handler : TInputHandlers.getHandlers()) {
            TfileLocationChooser.addChoosableFileFilter(new FileFilter() {
                @Override
                public String getDescription() {
                    return strings.get(handler.getClass().getSimpleName()
                        .replaceAll("TInputHandler", "FileType"));
                }

                @Override
                public boolean accept(File f) {
                    try {
                        return f.isDirectory() || handler.isKnownFile(f);
                    } catch (IOException e) {
                        LOGGER.error("Caught IOException while checking whether file is acceptable: ", e);
                        return false;
                    }
                }
            });
        }

        TfileLocationChooser.setDialogTitle(strings.get("LoadTFile"));
        if (TfileLocationChooser.showOpenDialog(this) != JFileChooser.APPROVE_OPTION)
            return;

        File TFile = TfileLocationChooser.getSelectedFile();

        try {
            project.loadTFile(TFile);
        } catch (IOException ex) {
            LOGGER.error("Caught IOException while trying to import T-Invariant file");
            JOptionPane.showMessageDialog(this, strings.get("ErrorReadingFileMessage", TFile), strings.get("ErrorReadingFileTitle"), JOptionPane.ERROR_MESSAGE);
        }

        try {
            updateNetViewer();
        } catch (IOException ex) {
            LOGGER.error("Caught IOException while trying to update NetViewer after importing T-Invariant file");
        }
    }

    private void openProject() throws IOException, ClassNotFoundException, InterruptedException {
        LOGGER.info("Gathering file to open project from");
        askForSavingTheProject();

        MonaLisaFileChooser projectLocationChooser = new MonaLisaFileChooser();

        projectLocationChooser.setFileFilter(new FileFilter() {
            @Override
            public boolean accept(File f) {
                return f.isDirectory()
                    || Project.FILENAME_EXTENSION.equalsIgnoreCase(
                        FileUtils.getExtension(f));
            }

            @Override
            public String getDescription() {
                return strings.get("ProjectFileType");
            }
        });

        projectLocationChooser.setDialogTitle(strings.get("LoadProject"));
        if (projectLocationChooser.showOpenDialog(this) != JFileChooser.APPROVE_OPTION)
            return;

        File projectFile = projectLocationChooser.getSelectedFile();
        LOGGER.info("Successfully gathered file to open project from");
        openProject(projectFile);
    }

    public void openProject(File projectFile) throws IOException, ClassNotFoundException, InterruptedException {
        LOGGER.info("Opening project from file");
        try {
            project = Project.load(projectFile);
        } catch (IOException ex) {
            LOGGER.error("Caught IOException while trying to open project from file");
            JOptionPane.showMessageDialog(this, strings.get("ErrorReadingFileMessage", projectFile), strings.get("ErrorReadingFileTitle"), JOptionPane.ERROR_MESSAGE);
            return;
        }

        if(project.getPropertyList() == null) {
            project.setPropertyList(new PropertyList());
        }

        String recentlyProjects = Settings.get("recentlyProjects");

        // If project are in the list, set it to the last place
        if(recentlyProjects.contains(projectFile.getAbsolutePath())) {
            recentlyProjects = recentlyProjects.replace(projectFile.getAbsolutePath()+",", "");
        }
        else {
            // If not, check if the list is to large and delte the first element
            String projects[] = recentlyProjects.split(",");
            if(projects.length == 10) {
                String tmp = "";
                for(int i = 1; i < 10; i++) {
                    tmp += projects[i] + ",";
                }
                recentlyProjects = tmp;
            }
        }
        // Add the new project at the last place
        recentlyProjects += projectFile.getAbsolutePath()+",";

        Settings.set("recentlyProjects", recentlyProjects);
        Settings.writeToFile(Settings.getConfigFile());

        projectLoaded();

        project.setResults(project.getAllResults());
        LOGGER.info("Successfully opened project from file");
    }

    private void saveProject() throws ClassNotFoundException, IOException {
        if(project != null) {
//            try {
                if(netViewer != null) {
                    netViewer.updatePetriNet();
                }
                project.save();
//            } catch (IOException ex) {
//                JOptionPane.showMessageDialog(this, strings.get("ErrorWritingFileMessage"), strings.get("ErrorWritingFileTitle"), JOptionPane.ERROR_MESSAGE);
//            }
        }
    }

    public void exportPetriNet() throws IOException {
        LOGGER.info("Exporting Petri net");
        JFileChooser petriNetFileChooser = new MonaLisaFileChooser();

        petriNetFileChooser.setDialogType(JFileChooser.SAVE_DIALOG);
        petriNetFileChooser.setDialogTitle(strings.get("ChooseAPetriNetFileName"));
        petriNetFileChooser.setAcceptAllFileFilterUsed(false);

        for (final OutputHandler handler : PetriNetOutputHandlers.getHandlers()) {
            petriNetFileChooser.addChoosableFileFilter(new OutputFileFilter(handler));
        }

        if (petriNetFileChooser.showDialog(this, "Export") != JFileChooser.APPROVE_OPTION)
            return;

        File petriNetFile = petriNetFileChooser.getSelectedFile();
        OutputFileFilter selectedFileFilter = ((OutputFileFilter)petriNetFileChooser.getFileFilter());
        petriNetFile = selectedFileFilter.checkFileNameForExtension(petriNetFile);

        selectedFileFilter.getHandler().save(new FileOutputStream(petriNetFile), project.getPetriNet());
        LOGGER.info("Succesfully exported Petri net");
    }

    private void exportResults() {
        LOGGER.info("Exporting tool results");
        ExportDialog exportDialog = new ExportDialog(this, project);

        if(exportDialog.isCancelled())
            return;

        Map<Pair<Class<? extends Tool>,Configuration>,String> exports = exportDialog.exportPaths();
        boolean retainAll = false;
        boolean deleteAll = false;

        for (Pair<Class<? extends Tool>, Configuration> export : exports.keySet()) {
            // Determine output file name.
            Result result = project.getResult(export.first(), export.second());
            File outputFile = new File(exportDialog.exportPaths().get(export));

            // See whether file already exists.
            if (outputFile.exists()) {
                if (retainAll)
                    continue;
                else if (deleteAll) {
                    // Do nothing.
                }
                else {
                    ConfirmOverwriteDialog dialog = new ConfirmOverwriteDialog(
                        this, outputFile.getName());
                    switch (dialog.getResult()) {
                        case ConfirmOverwriteDialog.YES:
                            break;
                        case ConfirmOverwriteDialog.YES_FOR_ALL:
                            deleteAll = true;
                            break;
                        case ConfirmOverwriteDialog.NO:
                            continue;
                        case ConfirmOverwriteDialog.NO_FOR_ALL:
                            retainAll = true;
                            continue;
                    }
                }
            }

            try {
                result.export(outputFile, export.second(), project);
                LOGGER.info("Successfully exported tool results to '" + outputFile.getName() + "'");
            } catch (IOException ex) {
                // TODO Proper error handling.
                LOGGER.error("Caught IOException while trying to write tool results to output file '" + outputFile.getName() + "'");
                System.out.printf("Unable to write output file %s\n", outputFile);
            }
        }
    }

    public void projectLoaded() throws ClassNotFoundException, IOException, InterruptedException {
        // Unload previous project, if any.
        projectClosed();
        LOGGER.info("Loading new project");
        mainContainer.removeAll();
        mainContainer.add(scrollPane);
        project.addToolStatusUpdateListener(this);
        setDocumentTitle(project.getName());
        setProjectRelatedEnabled(true);
        project.createUI(contentPanel, strings);
        recursivelyDoLayout(mainContainer);
        menuTFileLoad.setEnabled(true);

        if(project.getPath() == null)
            menuFileSaveAs.setEnabled(false);
        else
            menuFileSaveAs.setEnabled(true);

        // --- START NetViewer ToolBar Plugins ---

        if(netViewer != null) {
            netViewer.closeAllFrames();
            netViewer.dispose();
        }

        netViewer = new NetViewer(this, project);
        netViewer.addNetChangedListener(this);

        PetriNetFacade pnFacade = this.project.getPNFacade();
        List<AddonPanel> addonPanels = new ArrayList<>();
        // Load all AddonPanels
        for(Class<? extends AddonPanel> c : Addons.addons) {
            try {
                addonPanels.add(c.getConstructor(NetViewer.class, PetriNetFacade.class).newInstance(netViewer, pnFacade));
            } catch (InstantiationException | IllegalAccessException | IllegalArgumentException
                    | InvocationTargetException | NoSuchMethodException | SecurityException ex) {
                LOGGER.error("Caught exception while trying to load all AddonPanels: ", ex);
            }
        }
        project.registerAddOns(addonPanels);
        project.transferStorageToAddOns(addonPanels);

        Settings.writeToFile(System.getProperty("user.home")+"/.monalisaSettings");

        // --- STOP NetViewer ToolBar Plugins ---

        // Load TreeViewer
        if(treeViewer != null)
            treeViewer.dispose();
        treeViewer = new TreeViewer(project);
        if(project.hasResults(new ClusterTool()))
            enableTVButton(true);
        else
            enableTVButton(false);
        LOGGER.info("Successfully loaded new project");
    }

    private void projectClosed() {
        LOGGER.info("Unloading any existing previous projects");
        setDocumentTitle(null);
        setProjectRelatedEnabled(false);
        contentPanel.removeAll();
        contentPanel.doLayout();
        LOGGER.info("Successfully unloaded any existing previous projects");
    }

    private void setProjectRelatedEnabled(boolean enabled) {
        menuFileSave.setEnabled(enabled);
        menuFileSaveAs.setEnabled(enabled);
        fileSaveButton.setEnabled(enabled);
        menuFileExportResults.setEnabled(enabled);
        menuFileExportPetriNet.setEnabled(enabled);
        menuTFileLoad.setEnabled(enabled);
        fileExportResultsButton.setEnabled(enabled);
        fileExportPetriNetButton.setEnabled(enabled);
        projectMenu.setEnabled(enabled);
        projectRunButton.setEnabled(enabled);
        showNVButton.setEnabled(enabled);
    }

    private void recursivelyDoLayout(JPanel panel) {
        panel.doLayout();
        for (Component child : panel.getComponents()) {
            if (child instanceof JPanel)
                recursivelyDoLayout((JPanel) child);
        }
    }

    @Override
    public void updated(final ToolStatusUpdateEvent e) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                switch (e.getStatus()) {
                    case STARTED:
                        statusBar.setStatusText(e.getToolName() + " started");
                        statusBar.setProgressValue(0);
                        statusBar.setIndeterminateProgress(true);
                        break;
                    case FINISHED:
                        statusBar.setIndeterminateProgress(false);
                        statusBar.setProgressValue(100);
                        statusBar.setStatusText(" ");

                        // NetViewer
                        try {
                            updateNetViewer();
                        } catch (IOException | InterruptedException ex) {
                            LOGGER.error("Caught exception while trying to update NetViewer on tool status update");
                        }

                        updateTreeViewer();

                        break;
                    case ABORTED:
                        statusBar.setStatusText(" ");
                        restoreRunButtons();
                        ErrorLog interruptedMessages = project.getToolMessages();
                        interruptedMessages.log("#ToolsInterruptedByUser", ErrorLog.Severity.WARNING);
                        MessageDialog.show(MainDialog.this, interruptedMessages);
                        break;
                    case PROGRESS:
                        statusBar.setIndeterminateProgress(false);
                        statusBar.setProgressValue(e.getProgress());
                        break;
                    case FINISHED_ALL:
                        statusBar.setStatusText(" ");
                        restoreRunButtons();
                        ErrorLog messages = project.getToolMessages();
                        if (messages.has(ErrorLog.Severity.WARNING) || messages.has(ErrorLog.Severity.ERROR))
                            MessageDialog.show(MainDialog.this, messages);
                        else
                            JOptionPane.showMessageDialog(null,
                                strings.get("ToolCalculationFinishedMessage"),
                                strings.get("ToolCalculationFinishedTitle"),
                                JOptionPane.INFORMATION_MESSAGE);
                        break;
                    default:
                        break;
                }
            }
        });
    }

    private void showNV()  {
        netViewer.showMe();
    }

    private void showTV()  {
        treeViewer.showMe();
    }

    private void updateNetViewer() throws InterruptedException, IOException {
        if(project.hasResults(TInvariantTool.class)) {
            netViewer.addTinvsToListDisplay();
        }
        if(project.hasResults(PInvariantTool.class)) {
            netViewer.addPinvsToComboBox();
        }
        if(project.hasResults(MctsTool.class)) {
            netViewer.addMctsToComboBox();
        }
        if(project.hasResults(McsTool.class)) {
            netViewer.addMcsToComboBox();
        }

        netViewer.setNetChanged(false);
        netViewer.netChanged();
    }

     private void updateTreeViewer() {
         if(project.hasResults(ClusterTool.class)) {
            treeViewer.updateClusterResults();
            enableTVButton(true);
         }
     }

    /**
     * Enable or disable the showTVButton in the MainDialog Frame
     * @param b
     */
    public void enableTVButton(Boolean b) {
        showTVButton.setEnabled(b);
    }

    public void setExportEnabled(Boolean value) {
        menuFileExportPetriNet.setEnabled(value);
        fileExportPetriNetButton.setEnabled(value);
    }

    public void updateUI() {
        contentPanel.removeAll();
        project.createUI(contentPanel, strings);
    }

    @Override
    public void netChanged() {
        this.treeViewer.reset();
        this.showTVButton.setEnabled(false);
    }
}
