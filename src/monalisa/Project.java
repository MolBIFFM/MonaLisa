/*
 *
 *  This file is part of the software MonaLisa.
 *  MonaLisa is free software, dependent on non-free software. For more information read LICENCE and README.
 *
 *  (c) Department of Molecular Bioinformatics, Institute of Computer Science, Johann Wolfgang
 *  Goethe-University Frankfurt am Main, Germany
 *
 */
package monalisa;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import monalisa.addons.netviewer.NetViewerStorage;

import monalisa.data.PropertyList;
import monalisa.data.input.PetriNetInputHandlers;
import monalisa.data.pn.PetriNet;
import monalisa.data.pn.PetriNetFacade;
import monalisa.resources.ResourceManager;
import monalisa.resources.StringResources;
import monalisa.synchronisation.Synchronizer;
import monalisa.util.MonaLisaObjectInputStream;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * A MonaLisa project folder. This saves all the information, configurations and
 * calculated results belonging to a project.
 *
 * @author Konrad Rudolph
 * @author Anja Thormann
 * @author Jens Einloft
 */
public final class Project implements Serializable {

    private static final long serialVersionUID = -7900422748294040894L;
    private static final Logger LOGGER = LogManager.getLogger(Project.class);

    public static final String FILENAME_EXTENSION = "mlproject";
    public static final String TFILENAME_EXTENSION = "res";

    private static final StringResources strings = ResourceManager.instance().getDefaultStrings();

    private final PetriNet petriNet;

    transient private File projectPath;

    private Boolean projectChanged;

    private PropertyList properties;

    private Synchronizer synchronizer;

    private NetViewerStorage nvs; // Replaces synchronizer's storage

    private Map<String, Map<String, Object>> addonStorage;
    private ToolManager toolMan;

    /**
     * Create a new Project out of a external file. For the given file the
     * correct file handler is searched. If no file handler is found an
     * IOException is thrown.
     *
     * @param petriNetFile The external file that should be loaded
     * @throws IOException
     */
    private Project(File petriNetFile) throws IOException {
        LOGGER.info("Creating new project from external file.");
        this.petriNet = PetriNetInputHandlers.load(petriNetFile);
        this.addonStorage = new HashMap<>();

        this.toolMan = new ToolManager();

        this.properties = new PropertyList();

        this.projectChanged = false;

        this.synchronizer = new Synchronizer(this.petriNet);

        this.nvs = new NetViewerStorage();
        LOGGER.info("Finished creating new project from external file.");
    }

    /**
     * Creates a new, empty project.
     */
    public Project() {
        LOGGER.info("Creating new empty project.");
        this.petriNet = new PetriNet();
        this.addonStorage = new HashMap<>();

        this.toolMan = new ToolManager();

        this.projectChanged = false;

        this.properties = new PropertyList();

        this.synchronizer = new Synchronizer(this.petriNet);

        this.nvs = new NetViewerStorage();
        LOGGER.info("Finished creating new empty project.");
    }

    /**
     * Create a new Project out of a given Petri net.
     *
     * @param pn
     */
    private Project(PetriNet pn) {
        LOGGER.info("Creating new project from Petri net.");
        this.petriNet = pn;
        this.addonStorage = new HashMap<>();

        this.toolMan = new ToolManager();

        this.projectChanged = false;

        this.synchronizer = new Synchronizer(this.petriNet);

        this.nvs = new NetViewerStorage();
        LOGGER.info("Finished creating new project from Petri net.");
    }

    /**
     * Clones a given project. Used for the "Save project as..." function
     *
     * @param oldProject
     * @param newProjectFile
     */
    private Project(Project oldProject, File newProjectFile) {
        LOGGER.info("Cloning project for 'Save as...'");
        this.petriNet = oldProject.petriNet;
        this.addonStorage = oldProject.addonStorage;
        this.properties = oldProject.properties;
        this.synchronizer = oldProject.synchronizer;
        this.toolMan = oldProject.toolMan;
        this.nvs = oldProject.nvs;
        this.projectPath = newProjectFile;
        LOGGER.info("Finished cloning project for 'Save as...'");
    }

    /**
     * Returns the instance of the Synchronizer of the project.
     *
     * @return
     */
    public Synchronizer getSynchronizer() {
        return this.synchronizer;
    }

    /**
     * Returns the instance of the NetViewerStorage of the project.
     *
     * @return
     */
    public NetViewerStorage getNvs() {
        return this.nvs;
    }

    /**
     * Returns the Petri net of the project.
     *
     * @return The instance of the PetriNet class of the project.
     */
    public PetriNet getPetriNet() {
        return petriNet;
    }

    /**
     * Returns the path of the file of the project.
     *
     * @return
     */
    public File getPath() {
        return projectPath;
    }

    /**
     * Changes projectPath
     *
     * @param projectFile new project path.
     */
    public void setPath(File projectFile) {
        this.projectPath = projectFile;
    }

    /**
     * Returns the name of the file of the project. Cuts out the path and the
     * extension of the path.
     *
     * @return
     */
    public String getName() {
        String dirName;
        if (projectPath == null) {
            dirName = "Empty Project";
        } else {
            dirName = projectPath.getName();
        }
        // Cut file extension.
        return dirName.replace("\\..*$", "");
    }

    /**
     * Create a new Project from a Petri net file
     *
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
     *
     * @param newProjectFile
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
     *
     * @param pn
     * @return
     */
    public static Project create(PetriNet pn) {
        Project project = new Project(pn);
        return project;
    }

    /**
     * Saves the whole Project to a given File
     *
     * @throws IOException
     */
    public void save() throws IOException { // Called on exitApplication, Save as.. and save
        LOGGER.info("Saving to given file: " + projectPath.getAbsolutePath());
        try (ObjectOutputStream oout = new ObjectOutputStream(new FileOutputStream(projectPath))) {
            oout.writeObject(this);
        }
        LOGGER.info("Finished saving to given file.");
    }

    /**
     * Load a Project from a given File
     *
     * @param location
     * @return
     * @throws IOException
     */
    public static Project load(File location) throws IOException {
        LOGGER.info("Loading project from file.");
        try (MonaLisaObjectInputStream oin = new MonaLisaObjectInputStream(new FileInputStream(location))) {
            Project project = (Project) oin.readObject();

            // Explicitly set projectPath.
            project.projectPath = location;

            project.toolMan = new ToolManager();
            LOGGER.info("Initializing tools.");

            // Add tools that have been newly added to the MonaLisa
            // application since the project has been saved.
            project.toolMan.updateTools();

            project.synchronizer = new Synchronizer(project.petriNet);
            LOGGER.info("Finished loading project from file.");
            return project;
        } catch (ClassNotFoundException e) {
            LOGGER.error("Error while loading project from file: ", e.getMessage());
            // Thrown if an older version is loaded with yet non existing classes / tools
            throw new RuntimeException(e);
        }
    }

    /**
     * Returns if the project has changed since loading. Changes are events like
     * manipulating the Petri net.
     *
     * @return Returns <code> true </code> if the projected has changed,
     * otherwise <code> false </code>
     */
    public Boolean isProjectChanged() {
        LOGGER.info("Checking whether project has changed");
        if (projectChanged == null) {
            LOGGER.info("projectChanged is null, defaulting to true");
            return true;
        }
        LOGGER.info("projectChanged: " + projectChanged.toString());
        return projectChanged;
    }

    /**
     * Set the "changed" flag of the project.
     *
     * @param projectChanged
     */
    public void setProjectChanged(Boolean projectChanged) {
        LOGGER.info("Setting projectChanged to " + projectChanged.toString());
        this.projectChanged = projectChanged;
    }

    /**
     * Test whether the entity has a given property.
     *
     * @param key The key to look for.
     * @return <code>true</code>, if the key is present, otherwise
     * <code>false</code>.
     */
    public boolean hasProperty(String key) {
        return properties.has(key);
    }

    /**
     * Retrieve a strongly typed property, based on its key.
     *
     * @param <T> The type of the property to retrieve.
     * @param key The key of the property.
     * @return The property, cast to type <code>T</code>.
     */
    public <T> T getProperty(String key) {
        LOGGER.info("Getting property for " + key);
        return properties.<T>get(key);
    }

    /**
     * Removes a property from the properties list
     *
     * @param key
     */
    public void removeProperty(String key) {
        LOGGER.info("Removing property for key " + key + "if it exists");
        if (properties.has(key)) {
            LOGGER.info(key + "property exists, removing");
            properties.remove(key);
        }
    }

    /**
     * Retrieve the whole PropertyList
     *
     * @return The PropertyList of the PetriNetEntity
     */
    public PropertyList getPropertyList() {
        return this.properties;
    }

    /**
     * Set a new PropertyList
     *
     * @param pl
     */
    public void setPropertyList(PropertyList pl) {
        this.properties = pl;
    }

    /**
     * Retrieve a strongly typed property, based on its key. If the property key
     * doesn't exist, return a default value instead.
     *
     * @param <T> The type of the property to retrieve.
     * @param key The key of the property.
     * @param defaultValue The default value.
     * @return The property value, if {@code key} exists, else
     * {@code defaultValue}.
     */
    public <T> T getValueOrDefault(String key, T defaultValue) {
        if (hasProperty(key)) {
            LOGGER.info("Getting property for " + key);
            return (T) getProperty(key);
        } else {
            LOGGER.warn("No property for " + key + "found, returning default value");
            return defaultValue;
        }
    }

    /**
     * Add a property to the Petri net entity.
     *
     * @param <T> The type of the property.
     * @param key The key of the property.
     * @param value The property.
     */
    public <T> void putProperty(String key, T value) {
        if (key.equals("name")) {
            value = (T) ((String) value).replace(" ", "_");
        }
        LOGGER.info("Adding value for " + key);
        properties.put(key, value);
    }

    /**
     * Returns a facade for the current PetriNet object.
     *
     * @return The Facade object
     */
    public PetriNetFacade getPNFacade() {
        return new PetriNetFacade(this.petriNet);
    }

    /**
     * Getter for project's ToolManager.
     *
     * @return the project's ToolManager.
     */
    public ToolManager getToolManager() {
        return toolMan;
    }

    /**
     * 
     * @return the storage for data from addons
     */
    public Map<String, Map<String, Object>> getStorage() {
        return addonStorage;
    }

    /**
     * Puts data toStore into addonStorage under the addon's name
     * @param addonName Name of the addon
     * @param toStore data to store
     */
    public void putStorage(String addonName, Map<String, Object> toStore) {
        addonStorage.put(addonName, toStore);
    }

    /**
     * Function is called when a project is saved and resets synchronizer.
     * @param objectOutput
     * @throws IOException 
     */
    private void writeObject(ObjectOutputStream objectOutput) throws IOException {
        this.synchronizer = null;
        objectOutput.defaultWriteObject();
    }

    
    /**
     * Function is called, if a project is loaded.
     *
     * @param objectInput
     * @throws IOException
     * @throws ClassNotFoundException
     */
    private void readObject(ObjectInputStream objectInput) throws IOException, ClassNotFoundException {
        LOGGER.info("Reading storage from input");
        objectInput.defaultReadObject();
        // Short workaround for older versions.
        if (addonStorage == null) {
            LOGGER.warn("Workaround for older versions, creating new HashMap");
            addonStorage = new HashMap<>();
        }        
        // Workaround for older projects
        if (nvs == null) {
            LOGGER.warn("Workaround for older projects, creating new NetViewerStorage");
            nvs = new NetViewerStorage();
        }
        if (synchronizer != null) {
            LOGGER.warn("Synchronizer found, older project. Old layout will be lost.");
            synchronizer = null;
        }
    }
}
