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

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;

import monalisa.data.Pair;

/**
 * Takes care of loading and formatting string resources.
 * @author Konrad Rudolph
 */
// FIXME Make the string properties Unicode aware, and fuck Java's Latin-1.
public final class StringResources {
    private final ResourceBundle bundle;
    private static final Map<Pair<String, Locale>, StringResources> resources =
        new HashMap<>();
    
    /**
     * Load a string resource based on a name.
     * @param name The name of the resource class to load.
     */
    public static StringResources create(String name) {
        return create(name, Locale.getDefault());
    }
    
    /**
     * Load a string resource based on a name and a locale.
     * @param name The name of the resource class to load.
     * @param locale The locale to use for loading.
     */
    public static synchronized StringResources create(String name, Locale locale) {
        Pair<String, Locale> resIdentifier = Pair.of(name, locale);
        
        if (!resources.containsKey(resIdentifier)) {
            StringResources newRes = new StringResources(name, locale);
            resources.put(resIdentifier, newRes);
            return newRes;
        }
        return resources.get(resIdentifier);
    }
    
    private StringResources(String name) {
        bundle = ResourceBundle.getBundle(name);
    }

    private StringResources(String name, Locale locale) {
        bundle = ResourceBundle.getBundle(name, locale);
    }
    
    /**
     * Loads a string identified by {@code key}.
     * @param key The key of the string to load.
     * @return The loaded string.
     * @see ResourceBundle#getString(String)
     */
    public String get(String key) {
        if(!bundle.containsKey(key))
            return "";
        return bundle.getString(key);
    }
    
    /**
     * Loads and formats a string identified by {@code key}, using the current
     * locale, or the explicitly provided locale from the constructor.
     * @param key The key of the string to load.
     * @param values Values to insert into the string.
     * @return The formatted string.
     * @see #get(String)
     * @see String#format(Locale, String, Object...)
     */
    public String get(String key, Object... values) {
        // FIXME Add proper handling for format patterns.
        //return String.format(bundle.getLocale(), bundle.getString(key), values);
        String fixedValue = bundle.getString(key).replaceAll("%", "%%").replaceAll("\\{\\d+\\}", "%s");
        return String.format(bundle.getLocale(), fixedValue, values);
    }
}
