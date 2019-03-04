/*
 *
 *  This file ist part of the software MonaLisa.
 *  MonaLisa is free software, dependend on non-free software. For more information read LICENCE and README.
 *
 *  (c) Department of Molecular Bioinformatics, Institue of Computer Science, Johann Wolfgang
 *  Goethe-University Frankfurt am Main, Germany
 *
 */

package monalisa.util;

import java.io.File;
import javax.swing.filechooser.FileFilter;

/**
 * File filter for image export
 * @author Jens Einloft
 */
public class ImageFileFilter extends FileFilter {
        private final String extension;

        public ImageFileFilter(String extension) {
            this.extension = extension;
        }

        public File checkFileNameForExtension(File file) {
            if(!extension.equalsIgnoreCase(FileUtils.getExtension(file)))
                file = new File(file.getAbsolutePath()+"."+extension);
            return file;
        }

        public String getExtension() {
            return extension;
        }

        @Override
        public String getDescription() {
            return "*."+extension;
        }

        @Override
        public boolean accept(File f) {
            if(f.isDirectory() || extension.equalsIgnoreCase(FileUtils.getExtension(f)))
                return true;
            else
                return false;
        }
}
