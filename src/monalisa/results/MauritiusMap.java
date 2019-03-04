/*
 *
 *  This file ist part of the software MonaLisa.
 *  MonaLisa is free software, dependend on non-free software. For more information read LICENCE and README.
 *
 *  (c) Department of Molecular Bioinformatics, Institue of Computer Science, Johann Wolfgang
 *  Goethe-University Frankfurt am Main, Germany
 *
 */

package monalisa.results;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;

import monalisa.Project;

public final class MauritiusMap implements Result {
    private static final long serialVersionUID = -8260517639800715422L;
    private final String sourceCode;
    
    public MauritiusMap(String sourceCode) {
        this.sourceCode = sourceCode;
    }

    @Override
    public void export(File path, Configuration config, Project project) throws IOException {
        try (PrintWriter out = new PrintWriter(path)) {
            out.write(sourceCode);
            out.close();
        }
    }

    @Override
    public String filenameExtension() {
        return "ps";
    }
}
