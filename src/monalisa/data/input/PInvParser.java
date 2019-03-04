/*
 *
 *  This file ist part of the software MonaLisa.
 *  MonaLisa is free software, dependend on non-free software. For more information read LICENCE and README.
 *
 *  (c) Department of Molecular Bioinformatics, Institue of Computer Science, Johann Wolfgang
 *  Goethe-University Frankfurt am Main, Germany
 *
 */

package monalisa.data.input;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import monalisa.data.pn.PInvariant;
import monalisa.data.pn.PInvariantBuilder;


public final class PInvParser {
    private static final Pattern transitionPattern =
        Pattern.compile("(?:(\\d+)\\s*\\*\\s*)?T(\\d+)");
    private static final Pattern newInvariantPattern =
        Pattern.compile("^\\s*(\\d+).\\s*Invariant\\s*:");

    private final List<PInvariant> invariants = new ArrayList<>();

    public PInvParser(PInvariantBuilder invariantBuilder, InputStream input, Map<Integer, Integer> mapping) throws IOException {
        BufferedReader reader =
            new BufferedReader(new InputStreamReader(input));
        
        System.out.println("DEBUG: Import Pinv ----- START");

        String line = null;
        while ((line = reader.readLine()) != null) {
            int transitionScanStart = 0;
            Matcher newInvariantMatcher = newInvariantPattern.matcher(line);

            if (newInvariantMatcher.find()) {
                if (!invariantBuilder.isEmpty())
                    invariants.add(invariantBuilder.buildAndClear());

                int id = Integer.parseInt(newInvariantMatcher.group(1));
                invariantBuilder.setId(id);
                transitionScanStart = newInvariantMatcher.end();
            }

            Matcher transitionsMatcher =
                transitionPattern.matcher(line).region(transitionScanStart,
                    line.length());

            while (transitionsMatcher.find()) {
                int factor =
                    transitionsMatcher.group(1) == null ? 1 :
                        Integer.parseInt(transitionsMatcher.group(1));
                int transitionId =
                    Integer.parseInt(transitionsMatcher.group(2));
                if (mapping != null)
                    transitionId = mapping.get(transitionId);
                invariantBuilder.add(transitionId, factor);
            }
        }

        if (!invariantBuilder.isEmpty()) {
            invariants.add(invariantBuilder.build());
            System.out.println("DEBUG: Import Tinv: Größe der Invariante: "+invariants.get(invariants.size()-1).size());
        }
        
        System.out.println("DEBUG: Import Pinv ----- END");               
    }

    public PInvParser(PInvariantBuilder invariantBuilder, InputStream input) throws IOException {
        this(invariantBuilder, input, null);
    }

    public PInvParser(PInvariantBuilder invariantBuilder, File input) throws IOException {
        this(invariantBuilder, new FileInputStream(input));
    }
    
    public PInvParser(PInvariantBuilder invariantBuilder, File input, Map<Integer, Integer> mapping) throws IOException {
        this(invariantBuilder, new FileInputStream(input), mapping);
    }

    public List<PInvariant> invariants() {
        return Collections.unmodifiableList(invariants);
    }
}
