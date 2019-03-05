/*
 *
 *  This file is part of the software MonaLisa.
 *  MonaLisa is free software, dependent on non-free software. For more information read LICENCE and README.
 *
 *  (c) Department of Molecular Bioinformatics, Institute of Computer Science, Johann Wolfgang
 *  Goethe-University Frankfurt am Main, Germany
 *
 */

package monalisa.resources;

import java.awt.Image;
import java.awt.Toolkit;
import java.net.URL;

import javax.swing.Icon;
import javax.swing.ImageIcon;

/**
 * The global resource manager. Requests to resources should all go through the
 * (singleton) instance of this class.
 * @author Konrad Rudolph
 */
public final class ResourceManager {
    public static final String DEFAULT_STRINGS = "monalisa.resources.Strings";
    private static final Class<ResourceManager> TYPE = ResourceManager.class;
    
    private static class InstanceHolder {
        public static ResourceManager INSTANCE = new ResourceManager();
    }
 
    /**
     * Returns the singleton instance of this class.
     */
    public static ResourceManager instance() { return InstanceHolder.INSTANCE; }
    
    private ResourceManager() { }

    /**
     * Reads an icon from an icon resource.
     * @param name The name of the icon resource file.
     * @return The icon.
     */
    public Icon getIcon(String name) {
        return new ImageIcon(getResourceUrl(name));
    }
    
    public Image getImage(String name) {
        return Toolkit.getDefaultToolkit().getImage(getResourceUrl(name));
    }
    
    /**
     * Creates a URL object for a given resource.
     * @param name The name of the resource file.
     * @return Returns a URL pointing to that resource.
     */
    public URL getResourceUrl(String name) {
        return TYPE.getResource(name);
    }
    
    /**
     * Returns the default string localization file.
     */
    public StringResources getDefaultStrings() {
        return StringResources.create(DEFAULT_STRINGS);
    }
}
