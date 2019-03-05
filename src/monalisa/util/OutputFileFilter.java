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
import java.io.IOException;
import javax.swing.filechooser.FileFilter;
import monalisa.data.output.OutputHandler;

/**
 * File filter
 * @author Jens Einloft
 */
public class OutputFileFilter extends FileFilter {
        private final OutputHandler handler;

        public OutputFileFilter(OutputHandler handler) {
            this.handler = handler;
        }

        public File checkFileNameForExtension(File file) {
            if(!handler.getExtension().equalsIgnoreCase(FileUtils.getExtension(file)))
                file = new File(file.getAbsolutePath()+"."+handler.getExtension());
            return file;
        }

        public String getExtension() {
            return handler.getExtension();
        }

        public OutputHandler getHandler() {
            return handler;
        }

        @Override
        public String getDescription() {
            return "*."+handler.getExtension()+" ("+handler.getDescription()+")";
        }

        @Override
        public boolean accept(File f) {
            try {
                if (f.isDirectory() || handler.isKnownFile(f))
                    return true;
                else 
                    return false;               
            } catch (IOException ex) {
                return false;
            }
        }
}
