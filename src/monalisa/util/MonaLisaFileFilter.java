/*
 *
 *  This file is part of the software MonaLisa.
 *  MonaLisa is free software, dependent on non-free software. For more information read LICENCE and README.
 *
 *  (c) Department of Molecular Bioinformatics, Institute of Computer Science, Johann Wolfgang
 *  Goethe-University Frankfurt am Main, Germany
 *
 */
package monalisa.util;

import java.io.File;
import javax.swing.filechooser.FileFilter;

/**
 * File filter
 *
 * @author Jens Einloft
 */
public class MonaLisaFileFilter extends FileFilter {

    private final String extension;
    private final String description;

    public MonaLisaFileFilter(String extension, String description) {
        this.extension = extension;
        this.description = description;
    }

    public File checkFileNameForExtension(File file) {
        if (!extension.equalsIgnoreCase(FileUtils.getExtension(file))) {
            file = new File(file.getAbsolutePath() + "." + extension);
        }
        return file;
    }

    public String getExtension() {
        return extension;
    }

    @Override
    public String getDescription() {
        return "*." + extension + " (" + description + ")";
    }

    @Override
    public boolean accept(File f) {
        if (f.isDirectory() || extension.equalsIgnoreCase(FileUtils.getExtension(f))) {
            return true;
        } else {
            return false;
        }
    }
}
