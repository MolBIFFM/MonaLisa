/*
 *
 *  This file is part of the software MonaLisa.
 *  MonaLisa is free software, dependent on non-free software. For more information read LICENCE and README.
 *
 *  (c) Department of Molecular Bioinformatics, Institute of Computer Science, Johann Wolfgang
 *  Goethe-University Frankfurt am Main, Germany
 *
 */

package monalisa.tools;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import monalisa.resources.ResourceManager;
import monalisa.resources.StringResources;

/**
 * A sink to log errors and warnings while a tool's execution.
 * @author Konrad Rudolph
 */
public final class ErrorLog {
    /**
     * The degree of severity of an error or a warning.
     * @author Konrad Rudolph
     */
    public enum Severity {
        /**
         * No error. Reserved, do not use. 
         */
        NONE,
        /**
         * A warning (i.e. something non-fatal), a result was created.
         */
        WARNING,
        /**
         * A fatal error, no result has been created.
         */
        ERROR
    }
    
    private static final StringResources strings =
        ResourceManager.instance().getDefaultStrings();

    private final List<String> warnings = new ArrayList<>();
    private final List<String> errors = new ArrayList<>();

    /**
     * An error message starting with this prefix denotes a key in the
     * localization dictionary of the application, and is translated
     * accordingly before the error message is stored internally.
     * @see #log(String, Severity)
     */
    public static final String LOCALIZED_PREFIX = "#";
    
    /**
     * Creates a new error log.
     */
    public ErrorLog() { }
    
    /**
     * Log an error or warning message.
     * @param message The error or warning message. If the message starts with
     *          a hash mark (<code>#</code>) it is assumed that the message is
     *          a key in the localization dictionary. This is then equivalent
     *          to invoking the following code:
     * 
     * <pre>StringResources strings = ResourceManager.instance().getDefaultStrings();
     *log(strings.get(message.SubString(1), severity);</pre>
     * @param severity The degree of severity, see {@link Severity} for an
     *          explanation of the different degrees.
     */
    public void log(String message, Severity severity) {
        if (message.startsWith(LOCALIZED_PREFIX)) {
            log(strings.get(message.substring(LOCALIZED_PREFIX.length())),
                severity);
            return;
        }
        
        switch (severity) {
            case NONE: break;
            case WARNING:
                warnings.add(message);
                break;
            case ERROR:
                errors.add(message);
                break;
        }
    }
    
    /**
     * Merges errors and warnings from another error log into this one.
     * Does not affect the other error log.
     * @param other The other error log.
     */
    public void logAll(ErrorLog other) {
        errors.addAll(other.getAll(Severity.ERROR));
        warnings.addAll(other.getAll(Severity.WARNING));
    }
    
    /**
     * Test whether there are any errors or warnings of a given degree of severity.
     * @param severity The degree of severity, see {@link Severity} for an
     *          explanation of the different degrees.
     * @return <code>true</code> if errors of the given degree of severity exist,
     *          <code>false</code> otherwise.
     */
    public boolean has(Severity severity) {
        return severity == Severity.WARNING ? !warnings.isEmpty() :
               severity == Severity.ERROR ? !errors.isEmpty() :
               false;
    }
    
    /**
     * Retrieve all error messages of a given degree of severity.
     * @param severity The degree of severity, see {@link Severity} for an
     *          explanation of the different degrees.
     * @return Returns a read-only list of strings that contain the error
     *          messages.
     */
    public List<String> getAll(Severity severity) {
        switch (severity) {
            case WARNING:
                return Collections.unmodifiableList(warnings);
            case ERROR:
                return Collections.unmodifiableList(errors);
            default:
                return null;
        }
    }
}
