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
import javax.swing.JFileChooser;
import monalisa.Settings;

/**
 *
 * @author Jens Einloft
 */
public class MonaLisaFileChooser extends JFileChooser {
    
    private String latestDirectory;
    //private static Boolean blockSelectedFile;            
    
    public MonaLisaFileChooser() {
        super();
        
        latestDirectory = Settings.get("latestDirectory");
        
        if(!latestDirectory.isEmpty()) {
            this.setCurrentDirectory(new File(latestDirectory));
        }
    }
       
    public MonaLisaFileChooser(String currentDirectoryPath) {
        super(currentDirectoryPath);
    }

    public MonaLisaFileChooser(File currentDirectory) {
        super(currentDirectory);
    }    
    
    @Override
    public File getSelectedFile() {
        File selectedFile = super.getSelectedFile();        
        
        if(selectedFile != null) {
            Settings.set("latestDirectory", selectedFile.getAbsolutePath().substring(0, selectedFile.getAbsolutePath().lastIndexOf(System.getProperty("file.separator"))));                    
            Settings.writeToFile(Settings.getConfigFile());   
        }
        
        return selectedFile;
    }
    
}
