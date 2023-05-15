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

import java.awt.Color;
import java.io.*;
import java.util.Properties;
import java.util.Map;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 *
 * This file is part of MonaLisa.
 *
 * Implements a static class that manages program settings. It supports loading
 * them from and saving them to a text file in 'key = value' format.
 *
 * @author Tim Schaefer
 * @author Jens Einloft
 */
public class Settings {

    private static final Logger LOGGER = LogManager.getLogger(Settings.class);
    /**
     * The settings which are currently in use.
     */
    static private Properties cfg;

    /**
     * The default settings.
     */
    static private Properties def;

    private static final String defaultFile = System.getProperty("user.home") + "/.monalisaSettings";
    private static final String configFile = System.getProperty("user.home") + "/.monalisaSettings";

    public static void init() {
        LOGGER.info("Initializing settings");
        cfg = new Properties();
        def = new Properties();

        setDefaults();
    }

    /**
     * Loads the properties from the file 'file'. Should be called at the start
     * of main to init the settings. These default values could then be
     * overwritten by command line arguments.
     *
     * @param file the configuration file to load
     * @return whether the settings could be loaded from the specified file
     */
    public static Boolean load() {
        LOGGER.info("Trying to load settings from file");
        init();

        Boolean res = false;

        cfg = new Properties();

        try {
            try (BufferedInputStream stream = new BufferedInputStream(new FileInputStream(new File(configFile)))) {
                cfg.load(stream);
            }
            res = true;
        } catch (Exception e) {
            LOGGER.warn("Could not load settings from properties file '" + configFile + "'.");
            res = false;
        }

        LOGGER.info("Loaded " + cfg.size() + " settings from properties file '" + configFile + "'.");

        return (res);
    }

    /**
     * Deletes all currently loaded properties. Note that the settings file is
     * NOT deleted or emptied (unless you call writeToFile() afterwards).
     */
    public static void empty() {
        LOGGER.warn("Deleting currently loaded properties");
        cfg = new Properties();
    }

    /**
     * Deletes all default properties. Note that the settings file is NOT
     * deleted or emptied (unless you call writeToFile() afterwards).
     */
    public static void defEmpty() {
        LOGGER.warn("Deleting default properties");
        def = new Properties();
    }

    /**
     * Reloads the settings from the default settings.
     *
     * @return always true
     */
    public static Boolean resetAll() {
        LOGGER.info("Trying to reset properties to default values");
        cfg = new Properties();
        setDefaults();
        LOGGER.info("Creating deep copy of default to assign to settings");
        // make a deep copy of the default settings and assign it to cfg
        for (Map.Entry<Object, Object> entry : def.entrySet()) {
            String key = (String) entry.getKey();
            String value = (String) entry.getValue();
            cfg.setProperty(key, value);
        }
        LOGGER.info("Successfully reset all properties to default");
        return (true);
    }

    /**
     * Resets all properties to the default values.
     *
     * @return always true
     */
    public static Boolean setDefaults() {
        LOGGER.info("Setting properties to default values");
        def = new Properties();

        defSet("tinvColorR", "255");
        defSet("tinvColorG", "0");
        defSet("tinvColorB", "0");

        defSet("pinvColorR", "255");
        defSet("pinvColorG", "0");
        defSet("pinvColorB", "0");
        
        defSet("minvColorR", "255");
        defSet("minvColorG", "0");
        defSet("minvColorB", "0");

        defSet("mctsColorR", "0");
        defSet("mctsColorG", "0");
        defSet("mctsColorB", "255");

        defSet("heatMapColorR", "0");
        defSet("heatMapColorG", "255");
        defSet("heatMapColorB", "0");

        defSet("knockedOutColorR", "255");
        defSet("knockedOutColorG", "0");
        defSet("knockedOutColorB", "0");

        defSet("alsoKnockedOutColorR", "255");
        defSet("alsoKnockedOutColorG", "200");
        defSet("alsoKnockedOutColorB", "0");

        defSet("notKnockedOutColorR", "0");
        defSet("notKnockedOutColorG", "255");
        defSet("notKnockedOutColorB", "0");

        defSet("mcsObjectivColorR", "255");
        defSet("mcsObjectivColorG", "200");
        defSet("mcsObjectivColorB", "0");

        defSet("mcsColorR", "0");
        defSet("mcsColorG", "0");
        defSet("mcsColorB", "255");

        defSet("backgroundColorR", "255");
        defSet("backgroundColorG", "255");
        defSet("backgroundColorB", "255");

        defSet("transitionColorR", "0");
        defSet("transitionColorG", "0");
        defSet("transitionColorB", "0");

        defSet("placeColorR", "255");
        defSet("placeColorG", "255");
        defSet("placeColorB", "255");

        defSet("egdeColorR", "255");
        defSet("egdeColorG", "255");
        defSet("egdeColorB", "255");

        defSet("completeResultR", "132");
        defSet("completeResultG", "182");
        defSet("completeResultB", "31");

        defSet("hasResultsR", "132");
        defSet("hasResultsG", "182");
        defSet("hasResultsB", "31");

        defSet("warningR", "233");
        defSet("warningG", "180");
        defSet("warningB", "76");

        defSet("errorR", "176");
        defSet("errorG", "61");
        defSet("errorB", "61");

        defSet("notFinishedR", "128");
        defSet("notFinishedG", "128");
        defSet("notFinishedB", "128");

        defSet("recentlyProjects", "");

        defSet("latestDirectory", "");
        LOGGER.info("Finished setting properties to default values");
        return (true);
    }

    /**
     * Tries to set the key 'key' in the currently used settings to the default
     * value.
     *
     * @param key the key to set from the defaults
     * @return true if it worked out, i.e., such a key exists in the default
     * settings and it was used. False if no such key exists in the default
     * settings hashmap.
     */
    public static Boolean initSingleSettingFromDefault(String key) {
        if (defContains(key)) {
            LOGGER.info("Setting property for " + key + "to default value");
            cfg.setProperty(key, def.getProperty(key));
            return (true);
        } else {
            LOGGER.error("No default value found for key " + key);
            return (false);
        }
    }

    public static void setColorOption(String key, Color value) throws FileNotFoundException, IOException {
        LOGGER.info("Setting color properties for RGB");
        cfg.setProperty(key + "R", (new Integer(value.getRed())).toString());
        cfg.setProperty(key + "G", (new Integer(value.getGreen())).toString());
        cfg.setProperty(key + "B", (new Integer(value.getBlue())).toString());
    }

    /**
     * Creates a new config file in the default location and fills it with the
     * default values defined in the resetAll() function.
     *
     * @return true if it worked out, false otherwise
     */
    public static Boolean createDefaultConfigFile() {
        LOGGER.info("Trying to create new config file with default values");
        if (resetAll()) {
            if (writeToFile(defaultFile)) {
                LOGGER.info("Successfully created new config file with default values");
                return (true);
            }
        }
        LOGGER.error("Failed to create new config file with default values");
        return (false);
    }

    /**
     * Tries to cast the value of the property key 'key' to Integer and return
     * it. If this fails it is considered a fatal error.
     *
     * @param key the key of the properties hashmap
     * @return the value of the key as an Integer
     */
    public static Integer getInteger(String key) {
        LOGGER.debug("Trying to cast '" + key + "' to Integer");
        Integer i = null;
        String s = get(key);

        try {
            i = Integer.valueOf(s);
        } catch (Exception e) {
            LOGGER.fatal("Could not load setting '" + key + "' from settings as an Integer, invalid format.");
            System.exit(1);
        }
        LOGGER.debug("Successfully cast '" + key + "' to Integer");
        return (i);
    }

    /**
     * Determines whether the key 'key' in the currently used settings is at the
     * default setting.
     *
     * @return true if it is in default setting, false if this setting has been
     * changed by the user (via command line or config file)
     */
    public static Boolean isAtDefaultSetting(String key) {
        LOGGER.info("Checking whether value for '" + key + "' equals default value");
        if (get(key).equals(defGet(key))) {
            LOGGER.info("Value for " + key + " matches default value");
            return (true);
        }
        LOGGER.info("Value for '" + key + "' doesn't match default value");
        return (false);
    }

    /**
     * Tries to cast the value of the property key 'key' to Float and return it.
     * If this fails it is considered a fatal error.
     *
     * @param key the key of the properties hashmap
     * @return the value of the key as a Float
     */
    public static Float getFloat(String key) {
        LOGGER.debug("Trying to cast '" + key + "' to Float");
        Float f = null;
        String s = get(key);

        try {
            f = Float.valueOf(s);
        } catch (Exception e) {
            LOGGER.fatal("Could not load setting '" + key + "' from settings as a float, invalid format.");
            System.exit(1);
        }
        LOGGER.debug("Successfully cast '" + key + "' to Float");
        return (f);
    }

    /**
     * Tries to extract the value of the property key 'key' as a Boolean and
     * return it. If this fails it is considered a fatal error. The only
     * accepted string representations of Booleans are "true" and "false".
     *
     * @param key the key of the properties hashmap
     * @return the value of the key as a Boolean
     */
    public static Boolean getBoolean(String key) {
        LOGGER.debug("Trying to cast '" + key + "' to Boolean");
        Boolean b = null;
        String s = null;

        s = get(key);
        switch (s.toLowerCase()) {
            case "true":
                LOGGER.debug("Successfully cast '" + key + "' to Boolean");
                return (true);
            case "false":
                LOGGER.debug("Successfully cast '" + key + "' to Boolean");
                return (false);
            default:
                LOGGER.fatal("Could not load setting '" + key + "' from settings as a boolean, invalid format.");
                System.exit(1);
                return (false);
        }
    }

    /**
     * Tries to extract the value of the property key 'key' as a Color and
     * return it. If this fails it is considered a fatal error.
     *
     * @param key the key of the properties hashmap
     * @return the value of the key as a Color
     */
    public static Color getAsColor(String key) {
        LOGGER.debug("Casting '" + key + "' to Color");
        try {
            return new Color(Integer.parseInt((String) get(key + "R")), Integer.parseInt((String) get(key + "G")), Integer.parseInt((String) get(key + "B")));
        } catch (NullPointerException e) {
            LOGGER.fatal("Could not load setting '" + key + "' from settings as a Color, invalid format.");
            System.exit(1);
            return Color.BLACK; // Never reached because of System.exit(1), compiler needs it
        }
    }

    /**
     * Returns the path to the currently used config file as a String.
     *
     * @return The config file path.
     */
    public static String getConfigFile() {
        return (configFile);
    }

    /**
     * Prints all settings to STDOUT.
     */
    public static void printAll() {
        LOGGER.debug("Printing all " + cfg.size() + " settings");

        for (Object key : cfg.keySet()) {
            LOGGER.info((String) key + "=" + cfg.get(key));
        }

        LOGGER.debug("Printing of all " + cfg.size() + " settings done.");
    }

    /**
     * Prints all settings to STDOUT.
     */
    public static void defPrintAll() {
        LOGGER.debug("Printing all " + def.size() + " default settings.");

        for (Object key : def.keySet()) {
            LOGGER.info((String) key + "=" + def.get(key));
        }

        LOGGER.debug("Printing of all " + def.size() + " default settings done.");
    }

    /**
     * Retrieves the setting with key 'key' from the settings and returns it as
     * a String. Note that it is considered a fatal error if no such key exists.
     * Ask first using 'contains()' if you're not sure. :)
     *
     * @param key the key to get
     * @return the value of the specified key
     */
    public static String get(String key) {
        LOGGER.debug("Trying to return key '" + key + "' as String");
        if (cfg.containsKey(key)) {
            LOGGER.debug("Key '" + key + "' found, returning as String.");
            return ((String) cfg.getProperty(key));
        } else {
            LOGGER.warn("Setting '" + key + "' not defined in config file. Trying internal default.");

            if (initSingleSettingFromDefault(key)) {
                String s = defGet(key);
                cfg.put(key, s);
                writeToFile(configFile);
                LOGGER.warn("Using internal default value '" + s + "' for setting '" + key + "'. Edit config file to override.");
                return (s);
            } else {
                LOGGER.fatal("No config file or default value for setting '" + key + "' exists, setting invalid.");
                System.exit(1);
                return ("ERROR");    // Never reached because of System.exit(1), compiler needs it
            }
        }

    }

    /**
     * Retrieves the setting with key 'key' from the default settings and
     * returns it as a String. Note that it is considered a fatal error if no
     * such key exists. Ask first using 'contains()' if you're not sure. :)
     *
     * @param key the key to get
     * @return the value of the specified key
     */
    public static String defGet(String key) {
        LOGGER.debug("Trying to return default value for key '" + key + "' as a String");
        if (def.containsKey(key)) {
            LOGGER.debug("Default value found, returning as a String");
            return ((String) def.getProperty(key));
        } else {
            LOGGER.error("Could not load default setting '" + key + "' from default settings, no such setting.");
            System.exit(1);
            return (null);        // never reached, for the IDE
        }

    }

    /**
     * Adds a setting 'key' with value 'value' to the properties object. If a
     * settings with key 'key' already exists, its value gets overwritten.
     *
     * @param key the key which should be set
     * @param value the value for the entry with the given key
     */
    public static void set(String key, String value) {
        cfg.setProperty(key, value);
    }

    /**
     * Adds a setting 'key' with value 'value' to the default properties object.
     * If a settings with key 'key' already exists, its value gets overwritten.
     *
     * @param key the key which should be set
     * @param value the value for the entry with the given key
     */
    public static void defSet(String key, String value) {
        def.setProperty(key, value);
    }

    /**
     * Determines whether the properties object contains the key 'key'.
     *
     * @param key the key to check for
     * @return true if it contains such a key, false otherwise
     */
    public static Boolean contains(String key) {
        return (cfg.containsKey(key));
    }

    /**
     * Determines whether the default properties object contains the key 'key'.
     *
     * @param key the key to check for
     * @return true if it contains such a key, false otherwise
     */
    public static Boolean defContains(String key) {
        return (def.containsKey(key));
    }

    /**
     * Saves the current properties to the file 'file' or the default file if
     * 'file' is the empty string ("").
     *
     * @param file the file to write to. If this is the empty String (""), the
     * default file is used instead.
     * @return True if the file was written successfully, false if an error
     * occurred.
     */
    public static Boolean writeToFile(String file) {
        LOGGER.info("Writing current properties to file '" + file + "'");
        Boolean res = false;

        if (file.equals("")) {
            LOGGER.info("Empty filepath, using default path");
            file = defaultFile;
        }

        try {
            cfg.store(new FileOutputStream(file), "These are the settings for MonaLisa.");
            res = true;
        } catch (Exception e) {
            LOGGER.error("Could not write current properties to file '" + file + "'.");
            res = false;
        }

        return (res);
    }
}
