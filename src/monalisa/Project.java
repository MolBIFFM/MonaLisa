/*
 *
 *  This file ist part of the software MonaLisa.
 *  MonaLisa is free software, dependend on non-free software. For more information read LICENCE and README.
 *
 *  (c) Department of Molecular Bioinformatics, Institue of Computer Science, Johann Wolfgang
 *  Goethe-University Frankfurt am Main, Germany
 *
 */

package monalisa;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.*;
import javax.swing.GroupLayout.ParallelGroup;
import javax.swing.GroupLayout.SequentialGroup;
import javax.swing.filechooser.FileFilter;

import monalisa.ToolStatusUpdateEvent.Status;
import monalisa.addons.AddonPanel;
import monalisa.data.Pair;
import monalisa.data.PropertyList;
import monalisa.data.input.PetriNetInputHandlers;
import monalisa.data.input.TInputHandlers;
import monalisa.data.pn.PetriNet;
import monalisa.data.pn.PetriNetFacade;
import monalisa.gui.ConfirmOverwriteDialog;
import monalisa.gui.ExportDialog;
import monalisa.gui.components.CollapsiblePanel;
import monalisa.resources.ResourceManager;
import monalisa.resources.StringResources;
import monalisa.results.Result;
import monalisa.tools.BooleanChangeEvent;
import monalisa.tools.BooleanChangeListener;
import monalisa.tools.ErrorLog;
import monalisa.tools.ProgressEvent;
import monalisa.tools.ProgressListener;
import monalisa.tools.Tool;
import monalisa.tools.ToolRunner;
import monalisa.tools.Tools;
import monalisa.tools.tinv.TInvariantTool;
import monalisa.results.Configuration;
import monalisa.results.TInvariantsConfiguration;
import monalisa.synchronisation.Synchronizer;
import monalisa.util.FileUtils;
import monalisa.util.MonaLisaFileChooser;
import monalisa.util.MonaLisaObjectInputStream;

/**
 * A MonaLisa project folder. This saves all the information, configurations
 * and calculated results belonging to a project.
 * 
 * @author Konrad Rudolph
 * @author Anja Thormann
 * @author Jens Einloft
 */
public final class Project implements Serializable, ProgressListener, BooleanChangeListener {
    private static final long serialVersionUID = -7900422748294040894L;

    public static final String FILENAME_EXTENSION = "mlproject";
    public static final String TFILENAME_EXTENSION = "res";

    private static final StringResources strings = ResourceManager.instance().getDefaultStrings();
    
    private static final Color COMPLETE_COLOR = Settings.getAsColor("completeResult");
    private static final Color HAS_RESULT_COLOR = Settings.getAsColor("hasResults");
    private static final Color WARNING_COLOR = Settings.getAsColor("warning");
    private static final Color ERROR_COLOR = Settings.getAsColor("error");
    private static final Color NOT_FINISHED_COLOR = Settings.getAsColor("notFinished");
    
    private final PetriNet petriNet;
    private Map<Class<? extends Tool>, Map<Configuration, Result> > results;

    transient private File projectPath;
    transient private List<Tool> tools;
    transient private Map<Class<? extends Tool>, CollapsiblePanel> toolPanels;
    transient private List<ToolStatusUpdateListener> toolStatusUpdateListeners;
 
    transient private Thread toolsThread;
    transient private ErrorLog toolMessages;
    
    private Boolean projectChanged;
    
    private PropertyList properties;
    
    private Synchronizer synchronizer;
    
    private transient List<AddonPanel> registeredAddOns;
    private Map<String , Map<String, Object>> addonStorage;

    /**
     * Create a new Project out of a external file. For the given file the correct file handler is searched. 
     * If no file handler is found an IOException is thrown. 
     * @param petriNetFile The external file that should be loaded
     * @throws IOException 
     */
    private Project(File petriNetFile) throws IOException {
        this.petriNet = PetriNetInputHandlers.load(petriNetFile);
        this.results = new HashMap<>();
        this.toolStatusUpdateListeners = new ArrayList<>();;
        this.addonStorage = new HashMap<>();
        
        initTools();
        
        for (Class<? extends Tool> toolType : Tools.toolTypes())
            results.put(toolType, new HashMap<Configuration, Result>());
        
        this.properties = new PropertyList();
        
        this.projectChanged = false;
        
        this.synchronizer = new Synchronizer(this.petriNet);
    }
    
    /**
     * Creates a new, empty project.
     */
    public Project() {
        this.petriNet = new PetriNet();
        this.results = new HashMap<>();
        this.toolStatusUpdateListeners = new ArrayList<>();
        this.addonStorage = new HashMap<>();
        
        initTools();
        
        for (Class<? extends Tool> toolType : Tools.toolTypes())
            results.put(toolType, new HashMap<Configuration, Result>());
        
        this.projectChanged = false;
        
        this.properties = new PropertyList();
        
        this.synchronizer = new Synchronizer(this.petriNet);        
    }
    
    /**
     * Create a new Project out of a given Petri net.
     * @param pn 
     */
    private Project(PetriNet pn) {
        this.petriNet = pn;
        this.results = new HashMap<>();
        this.toolStatusUpdateListeners = new ArrayList<>();
        this.addonStorage = new HashMap<>();
        
        initTools();

        for (Class<? extends Tool> toolType : Tools.toolTypes())
            results.put(toolType, new HashMap<Configuration, Result>());
        
        this.projectChanged = false;
        
        this.synchronizer = new Synchronizer(this.petriNet);
    }     
    
    /**
     * Clones a given project. Used for the "Save project as..." function
     * @param oldProject
     * @param newProjectFile 
     */
    private Project(Project oldProject, File newProjectFile) {
        this.petriNet = oldProject.petriNet;
        this.results = oldProject.results;
        this.toolStatusUpdateListeners = oldProject.toolStatusUpdateListeners;
        this.addonStorage = oldProject.addonStorage;
        this.results = oldProject.results;
        this.properties = oldProject.properties;
        this.synchronizer = oldProject.synchronizer;            
        this.tools = oldProject.tools;    
        this.projectPath = newProjectFile;
    }
    
    /**
     * Returns the instance of the Synchronizer of the project.
     * @return 
     */
    public Synchronizer getSynchronizer() {
        return this.synchronizer;
    }
   
    /**
     * Add a new ToolStatusUpdateListener to the project.
     * @param listener 
     */
    public synchronized void addToolStatusUpdateListener(ToolStatusUpdateListener listener) {
        if (!toolStatusUpdateListeners.contains(listener))
            toolStatusUpdateListeners.add(listener);
    }
    
    /**
     * Removes a ToolStatusUpdateListener to the project.
     * @param listener 
     */
    public synchronized void removeToolStatusUpdateListener(
            ToolStatusUpdateListener listener) {
        toolStatusUpdateListeners.remove(listener);
    }
    
    /**
     * Returns the Petri net of the project.
     * @return The instance of the PetriNet class of the project.
     */
    public PetriNet getPetriNet() {
        return petriNet;
    }
    
    /**
     * Returns the path of the file of the project.
     * @return 
     */
    public File getPath() {
        return projectPath;
    }


    /**
     * Returns the name of the file of the project. Cuts out the path and the extension of the path.
     * @return 
     */
    public String getName() {
         String dirName;
        if(projectPath == null)
            dirName = "Empty Project";
        else
            dirName = projectPath.getName();
        // Cut file extension.
        return dirName.replace("\\..*$", "");
    }
    
    /**
     * Returns all error messages and warnings generated by the last run of the
     * tools, or {@code null} if the tools have not yet been run.
     */
    public ErrorLog getToolMessages() {
        return toolMessages;
    }
    
    /**
     * Tests whether a specific result has already been calculated.
     * A result is identified by the {@link Tool} calculating it, as well as a
     * {@link Configuration}.
     * @param tool The type of the tool class that calculated the result.
     * @param config The configuration to reproduce the result.
     * @return Returns <code>true</code> if the result exists, else
     *          <code>false</code>.
     */
    public boolean hasResult(Class<? extends Tool> tool, Configuration config) {
        return results.get(tool).containsKey(config);
    }
    
    /**
     * Tests whether a specific result has already been calculated.
     * A result is identified by the {@link Tool} calculating it, as well as a
     * {@link Configuration}.
     * @param tool The tool that calculated the result.
     * @param config The configuration to reproduce the result.
     * @return Returns <code>true</code> if the result exists, else
     *          <code>false</code>.
     * @see #hasResult(Class, Configuration)
     */
    public boolean hasResult(Tool tool, Configuration config) {
        return hasResult(tool.getClass(), config);
    }
    
    /**
     * Tests whether the specified tool already has calculated some results.
     * @param tool The type of the tool class.
     * @return Returns <code>true</code> if at least one result is associated
     *          with the tool, otherwise <code>false</code>.
     */
    public boolean hasResults(Class<? extends Tool> tool) {        
        Map<Configuration, Result> res = results.get(tool);
        return res != null && res.size() > 0;
    }
    
    /**
     * Tests whether the specified tool already has calculated some results.
     * @param tool The tool.
     * @return Returns <code>true</code> if at least one result is associated
     *          with the tool, otherwise <code>false</code>.
     * @see #hasResults(Class)
     */
    public boolean hasResults(Tool tool) {
        return hasResults(tool.getClass());
    }
    
    /**
     * Test whether all possible results have been calculated for a given tool.
     * Since the project doesn't know which results are possible, the expected
     * number of results is passed to this method and is compared against the
     * number of available results.
     * @param tool The type of the tool class that calculated the results.
     * @param expectedNumber The expected number of overall results for that
     *          tool.
     * @return Returns <code>true</code> if the number of results matches the
     *          expected number, else <code>false</code>.
     */
    public boolean hasAllResults(Class<? extends Tool> tool, int expectedNumber) {
        return results.get(tool).size() == expectedNumber;
    }
    
    /**
     * Test whether all possible results have been calculated for a given tool.
     * Since the project doesn't know which results are possible, the expected
     * number of results is passed to this method and is compared against the
     * number of available results.
     * @param tool The tool that calculated the results.
     * @param expectedNumber The expected number of overall results for that
     *          tool.
     * @return Returns <code>true</code> if the number of results matches the
     *          expected number, else <code>false</code>.
     * @see #hasAllResults(Class, int)
     */
    public boolean hasAllResults(Tool tool, int expectedNumber) {
        return hasAllResults(tool.getClass(), expectedNumber);
    }
    
    /**
     * Retrieves the result specified by a tool and a configuration.
     * @param <T> The type of the result.
     * @param tool The type of the tool class that calculated the result.
     * @param config The configuration that calculated the result.
     * @return Returns the requested result, or <code>null</code> if
     *          <code>{@link #hasResult}(tool, config)</code> returns
     *          <code>false</code>.
     */
    @SuppressWarnings("unchecked")
    public <T> T getResult(Class<? extends Tool> tool, Configuration config) {
        if(!results.containsKey(tool))
            putResult(tool, config, null);
        return (T) results.get(tool).get(config);
    }
    
    /**
     * Retrieves the result specified by a tool and a configuration.
     * @param <T> The type of the result.
     * @param tool The tool that calculated the result.
     * @param config The configuration that calculated the result.
     * @return Returns the requested result, or <code>null</code> if
     *          <code>{@link #hasResult}(tool, config)</code> returns
     *          <code>false</code>.
     * @see #getResult(Class, Configuration)
     */
    public <T> T getResult(Tool tool, Configuration config) {
        return (T)getResult(tool.getClass(), config);
    }
    
    /**
     * Stores a new result calculated by the given tool class and configuration.
     * @param tool The type of the tool class that calculated the result.
     * @param config The configuration that calculated the result.
     * @param result The result.
     */
    public void putResult(Class<? extends Tool> tool, Configuration config, Result result) {
        if(results.containsKey(tool))
            results.get(tool).put(config, result);
        else {
            Map<Configuration, Result> map = new HashMap<>();
            map.put(config, result);
            results.put(tool, map);
        }            
    }
    
    /**
     * Stores a new result calculated by the given tool class and configuration.
     * @param tool The tool that calculated the result.
     * @param config The configuration that calculated the result.
     * @param result The result.
     * @see #putResult(Class, Configuration, Result)
     */
    public void putResult(Tool tool, Configuration config, Result result) {
        putResult(tool.getClass(), config, result);
    }
    
    /**
     * Returns all results calculated by the specified tool.
     * @param tool The type of the tool class that calculated the results.
     * @return Returns a map from configurations to results, or
     *          <code>null</code> if <code>{@link #hasResults}(tool)</code>
     *          returns <code>false</code>.
     */
    public Map<Configuration, Result> getResults(Class<? extends Tool> tool) {
        return Collections.unmodifiableMap(results.get(tool));
    }
    
    /**
     * Returns all results calculated by the specified tool.
     * @param tool The tool that calculated the results.
     * @return Returns a map from configurations to results, or
     *          <code>null</code> if <code>{@link #hasResults}(tool)</code>
     *          returns <code>false</code>.
     * @see #getResults(Class)
     */
    public Map<Configuration, Result> getResults(Tool tool) {
        return getResults(tool.getClass());
    }

    /**
     * Returns all calculated results of all tools
     * @return Returns a map from configurations to results, off all tool
     */
    public Map<Class<? extends Tool>, Map<Configuration, Result> > getAllResults() {
        return results;
    }

    /**
     * Sets the reults of the project to the given results
     * @param results
     */
    public void setResults(Map<Class<? extends Tool>, Map<Configuration, Result> > results) {
        this.results = results;

        // Check for existing results and mark these tools as calculated
        Tool t;
        for(Class<? extends Tool> toolClass : Tools.toolTypes()) {            
            t = getTool(toolClass);
            if(hasResults(t)) {
                setHasResults(t);
            } else {
                setHasNoResults(t);
            }
        }
    }

    /**
     * Create a new Project from a Petri net file
     * @param petriNetFile
     * @return
     * @throws IOException
     */
    public static Project create(File petriNetFile) throws IOException {
        Project project = new Project(petriNetFile);
        return project;
    }

    /**
     * Create a new Project from a given Project (Save as...)
     * @param pn
     * @param oldProject
     * @return
     * @throws IOException
     */
    public static Project create(Project oldProject, File newProjectFile) throws IOException {
        Project newProject = new Project(oldProject, newProjectFile);
        return newProject;
    }

    /**
     * Create a new Project Object
     * @param pn
     * @return
     */
    public static Project create(PetriNet pn) {
        Project project = new Project(pn);
        return project;
    }

    /**
     * Saves the whole Project to a given File
     * @param location
     * @throws IOException
     */
    public void save(File location) throws IOException {
        projectPath = location;
        try (ObjectOutputStream oout = new ObjectOutputStream(new FileOutputStream(location))) {
            oout.writeObject(this);
        }
    }

    /**
     * Save the whole Project with asking for a path
     * @return
     * @throws IOException
     */
    public boolean save() throws IOException {
        boolean ret = false;

        if(projectPath == null) {
            File projectFile;
            MonaLisaFileChooser projectLocationChooser = new MonaLisaFileChooser();
                     
            projectLocationChooser.setFileFilter(new FileFilter() {
                @Override
                public boolean accept(File f) {
                    return f.isDirectory() || Project.FILENAME_EXTENSION.equalsIgnoreCase(FileUtils.getExtension(f));
                }
                @Override
                public String getDescription() {
                    return strings.get("ProjectFileType");
                }
            });

            projectLocationChooser.setDialogTitle(strings.get("SaveEmptyProjectLocation"));
            if (projectLocationChooser.showDialog(null, strings.get("NVSave")) != JFileChooser.APPROVE_OPTION)
                return false;

            projectFile = projectLocationChooser.getSelectedFile();
            
            if (!Project.FILENAME_EXTENSION.equalsIgnoreCase(FileUtils.getExtension(projectFile)))
                projectFile = new File(projectFile.getAbsolutePath() + "." + Project.FILENAME_EXTENSION);
            
            projectPath = projectFile;
            
            String recentlyProjects = Settings.get("recentlyProjects");
            // If project are in the list, set it to the last place
            if(recentlyProjects.contains(projectFile.getAbsolutePath())) {
                recentlyProjects = recentlyProjects.replace(projectFile.getAbsolutePath()+",", "");      
            }
            else {               
                // Check if the list is to large and delte the first element
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
            
            ret = true;
        }

        save(projectPath);
        return ret;
    }

    /**
     * Load a Project from a given File
     * @param location
     * @return
     * @throws IOException
     */
    public static Project load(File location) throws IOException {
        try (MonaLisaObjectInputStream oin = new MonaLisaObjectInputStream(new FileInputStream(location))) {
            Project project = (Project) oin.readObject();

            // Explicitly set projectPath.
            project.projectPath = location;
            project.toolStatusUpdateListeners = new ArrayList<>();
            
            project.initTools();
                        
            // Add tools that have been newly added to the MonaLisa
            // application since the project has been saved.            
            for (Class<? extends Tool> toolType : Tools.toolTypes()) {
                if (!project.results.containsKey(toolType)) {
                    project.results.put(toolType, new HashMap<Configuration, Result>());
                }
            }                        
            
            return project;
        } catch (ClassNotFoundException e) {
            // Thrown if an older version is loaded with yet non existing classes / tools
            throw new RuntimeException(e);
        }
    }

    /**
     * Load the given file and tries to construct given invariants and matches them to the Petri net.
     * @param TFile
     * @throws IOException
     * @throws InterruptedException
     */
    public void loadTFile(File TFile) throws IOException, InterruptedException {
        Result loadTFileResults = TInputHandlers.load(TFile, this.petriNet);
        Configuration loadTFileConfig = new TInvariantsConfiguration();
        Tool tool = new TInvariantTool();
        this.putResult(tool, loadTFileConfig, loadTFileResults);        
        tool.finishedState(Project.this);
        setComplete(tool);
    }
    
    /**
     * Initialize all tools.
     */
    public void initTools() {
        this.tools = new ArrayList<>();        
        for (Class<? extends Tool> toolType : Tools.toolTypes()) {
            try {
                this.tools.add(toolType.newInstance());
            } catch (InstantiationException | IllegalAccessException e) {
                throw new RuntimeException(
                    "An unexpected error has occurred while trying to load " +
                    "the tools. Sorry! Please report this error.", e);
            }
        }
    }
    
    /**
     * Reset all results of all tools.
     */
    public void resetTools() {
        results.clear();
        initTools();        
        for (Class<? extends Tool> toolType : Tools.toolTypes())
            results.put(toolType, new HashMap<Configuration, Result>());
    }
    
    /**
     * Creates the GUI for the Analyze Frame.
     * @param container
     * @param strings 
     */
    public void createUI(final JComponent container, StringResources strings) {
        GroupLayout containerLayout = (GroupLayout) container.getLayout();
        ParallelGroup horizontal = containerLayout.createParallelGroup(GroupLayout.Alignment.LEADING);
        SequentialGroup vertical = containerLayout.createSequentialGroup();
        
        toolPanels = new HashMap<>();
        if(tools != null) {
            JPanel[] allPanels = new JPanel[tools.size()];
            int panelIndex = 0;
            boolean first = false;

            for (Tool tool : tools) {
                if (!first)
                    first = true;
                else {
                    Component space = Box.createRigidArea(new Dimension(0, 3));
                    horizontal.addComponent(space);
                    vertical.addComponent(space);
                }

                CollapsiblePanel panel = new CollapsiblePanel(strings.get(panelName(tool)));
                GroupLayout layout = new GroupLayout(panel);
                panel.setLayout(layout);

                layout.setHorizontalGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                    .addComponent(tool.getUI(this, strings)));
                layout.setVerticalGroup(layout.createSequentialGroup()
                    .addComponent(tool.getUI(this, strings)));

                panel.setAlignmentX(Component.LEFT_ALIGNMENT);
                panel.setPreferredSize(new Dimension(container.getWidth(), panel.getHeight()));
                horizontal.addComponent(panel,
                    GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE, Short.MAX_VALUE);
                vertical.addComponent(panel);
                allPanels[panelIndex++] = panel;
                toolPanels.put(tool.getClass(), panel);

                if(!tool.getClass().getSimpleName().equalsIgnoreCase("TInvariantTool"))
                    panel.setCollapsed(true);

                if (tool.finishedState(this)) {
                    panel.setCollapsed(true);
                    setComplete(tool);
                }
                else if (hasResults(tool))
                    setHasResults(tool);
                tool.addActivityChangedListener(this);
            }
            containerLayout.setHorizontalGroup(horizontal);
            containerLayout.setVerticalGroup(vertical);
            containerLayout.linkSize(SwingConstants.HORIZONTAL, allPanels);
        }       
    }
    
    /**
     * Starts all selected Tools.
     */
    public void runSelectedTools() {
        toolMessages = new ErrorLog();
        toolsThread = new Thread() {
            @Override
            public void run() {
                for (Tool tool : tools) {
                    if (toolsThread.isInterrupted())
                        return;
                    runSingleTool(tool);
                }
                fireToolStatusUpdate(new ToolStatusUpdateEvent(Project.this, null, Status.FINISHED_ALL));
            }
        };

        toolsThread.start();
    }
    
    /**
     * Starts all given tools.
     */
    public void runGivenTools(final List<String> givenTool) {
        toolMessages = new ErrorLog();
        toolsThread = new Thread() {
            @Override
            public void run() {
                for (Tool tool : tools) {                    
                    if(givenTool.contains(tool.getClass().getName())) {
                        tool.setActive(true);
                        if (toolsThread.isInterrupted())
                            return;                        
                        runSingleTool(tool);
                    }
                }
                fireToolStatusUpdate(new ToolStatusUpdateEvent(Project.this, null, Status.FINISHED_ALL));
            }
        };

        toolsThread.start();
    }
    
    /**
     * Stops all running tools.
     */
    public void stopRunningTools() {
        toolsThread.interrupt();
        fireToolStatusUpdate(
            new ToolStatusUpdateEvent(this, null, Status.ABORTED));
    }
    
    /**
     * Run a single tool.
     * @param tool 
     */
    private void runSingleTool(Tool tool) {
        if (tool.isActive()) {            
            if (!checkToolRequirements(tool)) {
                setFailed(tool, new ErrorLog());
                return;
            }
            
            tool.addProgressListener(Project.this);
            fireToolStatusUpdate(tool, Status.STARTED);
            System.out.println(panelName(tool) + " started");

            ErrorLog log = new ErrorLog();
            ToolRunner runner = new ToolRunner(tool, Project.this, log);
            
            runner.start();
            
            try {
                runner.join();
            } catch (InterruptedException e) {
                runner.interrupt();
                System.out.println("Interrupt caught.");
                Thread.currentThread().interrupt();
                System.out.println("Interrupt propagated.");
                return;
            }
            finally {
                tool.removeProgressListener(Project.this);
            }
            
            for (Map.Entry<Configuration, Result> entry : runner.results().entrySet())
                putResult(tool, entry.getKey(), entry.getValue());
            
            if (log.has(ErrorLog.Severity.ERROR))
                setFailed(tool, log);
            else if (log.has(ErrorLog.Severity.WARNING))
                setCompleteWithWarnings(tool, log);
            else
                setHasResults(tool);
            if (tool.finishedState(Project.this))
                setComplete(tool);

            toolMessages.logAll(log);
            fireToolStatusUpdate(tool, Status.FINISHED);
            System.out.println(panelName(tool) + " finished");
        }
    }
    
    /**
     * Checks if all requirements for a given tool are fulfilled. 
     * @param tool The Tool to be checked
     * @return <code> true </code> if all requirements are fulfilled, otherwise <code> false </code>
     */
    private boolean checkToolRequirements(Tool tool) {
        List<Pair<Class<? extends Tool>, Configuration>> requirements =
            tool.getRequirements();
        boolean requirementsSatisfied = true;
        for (Pair<Class<? extends Tool>, Configuration> requirement : requirements) {
            if (!hasResult(requirement.first(), requirement.second())) {
                requirementsSatisfied = false;
                break;
            }
        }
        
        if (!requirementsSatisfied) {
            String toolName = strings.get(Tools.name(tool));
            JOptionPane.showMessageDialog(
                MonaLisa.appMainWindow(),
                strings.get("NotAllRequirementsSatisfiedMessage", toolName),
                strings.get("NotAllRequirementsSatisfiedTitle", toolName),
                JOptionPane.ERROR_MESSAGE);
        }
        
        return requirementsSatisfied;
    }
    
    /**
     * Marks a given tool with the "has results" tag
     * @param tool The tool to be marked
     */
    private void setHasResults(Tool tool) {
        CollapsiblePanel panel = toolPanels.get(tool.getClass()); 
        panel.setComment(strings.get("ToolHasResults"));
        panel.setTitleShade(HAS_RESULT_COLOR);
    }
    
    /**
     * Marks a given tool with the "has no results" tag
     * @param tool The tool to be marked
     */
    private void setHasNoResults(Tool tool) {
        CollapsiblePanel panel = toolPanels.get(tool.getClass()); 
        panel.setComment("");
        panel.setTitleShade(NOT_FINISHED_COLOR);
    }
    
    /**
     *  Marks a given tool with the "complete" tag
     * @param tool The tool to be marked
     */
    private void setComplete(Tool tool) {
        CollapsiblePanel panel = toolPanels.get(tool.getClass()); 
        panel.setComment(strings.get("ToolComplete"));
        panel.setTitleShade(COMPLETE_COLOR);
    }
    
    /**
     *  Marks a given tool with the "complete with warnings" tag
     * @param tool The tool to be marked
     */    
    private void setCompleteWithWarnings(Tool tool, ErrorLog log) {
        CollapsiblePanel panel = toolPanels.get(tool.getClass()); 
        panel.setComment(strings.get("ToolCompleteWithWarnings"));
        panel.setTitleShade(WARNING_COLOR);
    }
    
    /**
     *  Marks a given tool with the "failed" tag
     * @param tool The tool to be marked
     */      
    private void setFailed(Tool tool, ErrorLog log) {
        CollapsiblePanel panel = toolPanels.get(tool.getClass());
        panel.setComment(strings.get("ToolFailure"));
        panel.setTitleShade(ERROR_COLOR);
    }

    @Override
    public void progressUpdated(ProgressEvent e) {
        fireToolStatusUpdate((Tool) e.getSource(), e.getPercent());
    }
    
    /**
     * Returns the name of the panel of a given tool.
     * @param tool
     * @return 
     */
    private String panelName(Tool tool) {
        return Tools.name(tool);
    }
    
    private void fireToolStatusUpdate(Tool tool, Status status) {
        fireToolStatusUpdate(new ToolStatusUpdateEvent(this, panelName(tool), status));
    }
    
    private void fireToolStatusUpdate(Tool tool, int progress) {
        fireToolStatusUpdate(new ToolStatusUpdateEvent(this, panelName(tool), progress));
    }
    
    private synchronized void fireToolStatusUpdate(ToolStatusUpdateEvent e) {
        List<ToolStatusUpdateListener> listeners =
            new ArrayList<>(toolStatusUpdateListeners);
        for (ToolStatusUpdateListener listener : listeners)
            listener.updated(e);
    }

    @Override
    public void changed(BooleanChangeEvent e) {
        Tool tool = (Tool) e.getSource();
        
        if (e.getNewValue()) {
            // Select all requirements for the tool configuration that are not
            // yet calculated.
            List<Pair<Class<? extends Tool>, Configuration>> requirements =
                tool.getRequirements();
            for (Pair<Class<? extends Tool>, Configuration> requirement : requirements) {
                if (hasResult(requirement.first(), requirement.second()))
                    continue;
                getTool(requirement.first()).setActive(requirement.second());
            }
        }
        else {
            // Deselect all tools that depend on this tool.
            for (Tool otherTool : tools) {
                List<Pair<Class<? extends Tool>, Configuration>> requirements =
                    otherTool.getRequirements();
                for (Pair<Class<? extends Tool>, ?> requirement : requirements) {
                    if (requirement.first().isInstance(tool)) {
                        otherTool.setActive(false);
                        break;
                    }
                }
            }
        }
    }
    
    /**
     * Returns the instance of a given Tool.
     * @param toolType The class of the requested tool.
     * @return 
     */
    private Tool getTool(Class<? extends Tool> toolType) {
        for (Tool tool : tools)
            if(toolType.isInstance(tool))
                return tool;
        
        return null;
    }
    
    /**
     * Opens a dialog to export the results. 
     * @param toolList A List of the tools to be exported
     * @throws IOException 
     */
    public void exportResults(List<Class<? extends Tool>> toolList) throws IOException {
        ExportDialog exportDialog = new ExportDialog(this, toolList);

        if (exportDialog.isCancelled())
            return;

        Map<Pair<Class<? extends Tool>,Configuration>,String> exports = exportDialog.exportPaths();
        boolean retainAll = false;
        boolean deleteAll = false;

        for (Pair<Class<? extends Tool>, Configuration> export : exports.keySet()) {
            // Determine output file name.
            Result result = getResult(export.first(), export.second());
            File outputFile = new File(exportDialog.exportPaths().get(export));

            // See whether file already exists.
            if (outputFile.exists()) {
                if (retainAll)
                    continue;
                else if (deleteAll) {
                    // Do nothing.
                }
                else {
                    ConfirmOverwriteDialog dialog = new ConfirmOverwriteDialog(new JFrame(), outputFile.getName());
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

            result.export(outputFile, export.second(), this);
        }
    }
       
    /**
     * Returns if the project has changed since loading. Changes are events like manipulating the Petri net.
     * @return Returns <code> true </code> if the projected has changed, otherwise <code> false </code>
     */
    public Boolean isProjectChanged() {
        if(projectChanged == null)
            return true;
        return projectChanged;
    }
    
    /**
     * Set the "changed" flag of the project.
     * @param projectChanged 
     */
    public void setProjectChanged(Boolean projectChanged) {
        this.projectChanged = projectChanged;
    }    
    
    /**
     * Test whether the entity has a given property.
     * @param key The key to look for.
     * @return <code>true</code>, if the key is present, otherwise <code>false</code>.
     */
    public boolean hasProperty(String key) {
        return properties.has(key);
    }  
    
    /**
     * Retrieve a strongly typed property, based on its key.
     * @param <T> The type of the property to retrieve.
     * @param key The key of the property.
     * @return The property, cast to type <code>T</code>.
     */
    public <T> T getProperty(String key) {
        return properties.<T>get(key);
    }
    
    /**
     * Removes a property from the properties list 
     * @param key 
     */
    public void removeProperty(String key) {
        if(properties.has(key)) {
            properties.remove(key);
        }
    }    
    
    /**
     * Retrieve the whole PropertyList
     * @return The PropertyList of the PetriNetEntity
     */
    public PropertyList getPropertyList() {
        return this.properties;
    } 
    
    /**
     * Set a new PropertyList
     * @param pl 
     */
    public void setPropertyList(PropertyList pl) {
        this.properties = pl;
    } 
        
    /**
     * Retrieve a strongly typed property, based on its key.
     * If the property key doesn't exist, return a default value instead.
     * @param <T> The type of the property to retrieve.
     * @param key The key of the property.
     * @param defaultValue The default value.
     * @return The property value, if {@code key} exists, else {@code defaultValue}.
     */
    public <T> T getValueOrDefault(String key, T defaultValue) {
        if (hasProperty(key)) {
            return (T)getProperty(key);
        }
        else
            return defaultValue;
    }
    
    /**
     * Add a property to the Petri net entity.
     * @param <T> The type of the property.
     * @param key The key of the property.
     * @param value The property.
     */
    public <T> void putProperty(String key, T value) {
        if(key.equals("name"))
            value = (T)((String)value).replace(" ", "_");
        properties.put(key, value);
    }     
    
    /**
     * Returns a facade for the current PetriNet object.
     * @return The Facade object
     */
    public PetriNetFacade getPNFacade() {
        return new PetriNetFacade(this.petriNet);
    }
    
    /**
     * Get a list off AddonPanels and register them in the project.
     * If the project is saved, all these AddOns get a request to send their data to store in the project.
     * @param addOns 
     */
    public void registerAddOns(List<AddonPanel> addOns) {
        this.registeredAddOns = addOns;
    }
    
    /**
     * Get a list of AddonPanels and send them their stored data. 
     * The list of AddOn Panels is needed, to get the current instances of the AddonPanels.
     * @param addOns 
     */
    public void transferStorageToAddOns(List<AddonPanel> addOns) {
        for(AddonPanel a : addOns) {
            if(addonStorage.get(a.getAddOnName()) != null) {
                a.reciveStoredObjects(addonStorage.get(a.getAddOnName()));
            }
        }
    }
    
    /**
     * Function is called if the Project is saved. 
     * First the function collects all data from AddOns, that should be saved, too.
     * @param objectOutput
     * @throws IOException 
     */
    private void writeObject(ObjectOutputStream objectOutput) throws IOException {         
        if(registeredAddOns != null) {
            for(AddonPanel a : registeredAddOns) {
                if(a.getObjectsForStorage() != null) {
                    this.addonStorage.put(a.getAddOnName(), a.getObjectsForStorage());
                }
            }
        }
        objectOutput.defaultWriteObject();      
        
    }    
    
    /**
     * Function is called, if a project is loaded.
     * @param objectInput
     * @throws IOException
     * @throws ClassNotFoundException 
     */
    private void readObject(ObjectInputStream objectInput) throws IOException, ClassNotFoundException {
        objectInput.defaultReadObject();
        
        // Short workaround for older versions.
        if(addonStorage == null) {
            addonStorage = new HashMap<>();
        } 
        
        // Workaround for older projects
        if(synchronizer == null) {
            synchronizer = new Synchronizer(petriNet);
        }        
    }    
}
