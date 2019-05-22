/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package monalisa.data.input;


import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import monalisa.data.pn.MInvariant;
import monalisa.data.pn.TInvariant;
import monalisa.data.pn.PInvariant;
import monalisa.data.pn.InvariantBuilder;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public final class InvParser {
    private static Pattern transitionPattern;
        
    private static Pattern newInvariantPattern;
    private static final Logger LOGGER = LogManager.getLogger(InvParser.class);

    private final List<MInvariant> minvariants = new ArrayList<>();
    private final List<TInvariant> tinvariants = new ArrayList<>();
    private final List<PInvariant> pinvariants = new ArrayList<>();
    
    private final String InvType;

    public InvParser(InvariantBuilder invariantBuilder, InputStream input,
            Map<Integer, Integer> mapping, String InvType) throws IOException {
        
        this.InvType = InvType;
        if(InvType.equals("PI")){
            transitionPattern =
            Pattern.compile("(?:(\\d+)\\s*\\*\\s*)?P(\\d+)");
            newInvariantPattern =
            Pattern.compile("^\\s*(\\d+).\\s*place invariant\\s*:");
        }else if(InvType.equals("MI")){
            transitionPattern =
            Pattern.compile("(?:(\\d+)\\s*\\*\\s*)?T(\\d+)");
            newInvariantPattern =
            Pattern.compile("^\\s*(\\d+).\\s*manatee invariant\\s*:");
        }else{
            transitionPattern =
            Pattern.compile("(?:(\\d+)\\s*\\*\\s*)?T(\\d+)");
            newInvariantPattern =
            Pattern.compile("^\\s*(\\d+).\\s*transition invariant\\s*:");
        }
        LOGGER.info("Importing Invariants");

        BufferedReader reader =
            new BufferedReader(new InputStreamReader(input));

        String line = null;
        while ((line = reader.readLine()) != null) {
            int transitionScanStart = 0;
            Matcher newInvariantMatcher = newInvariantPattern.matcher(line);

            if (newInvariantMatcher.find()) {
                if (!invariantBuilder.isEmpty())
                    if(InvType.equals("MI"))
                        minvariants.add(invariantBuilder.buildAndClear());
                    else if(InvType.equals("PI"))
                        pinvariants.add(invariantBuilder.buildAndClear());
                    else
                        tinvariants.add(invariantBuilder.buildAndClear());

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
            
            if(InvType.equals("MI"))
                minvariants.add(invariantBuilder.build());
            else if(InvType.equals("PI"))
                pinvariants.add(invariantBuilder.build());
            else
                tinvariants.add(invariantBuilder.build());
        }

        LOGGER.info("Successfully imported Invariants");
    }

    public InvParser(InvariantBuilder invariantBuilder, InputStream input,
            String InvType)
            throws IOException {
        this(invariantBuilder, input, null, InvType);
    }

    public InvParser(InvariantBuilder invariantBuilder, File input, String InvType)
            throws IOException {
        this(invariantBuilder, new FileInputStream(input), InvType);
    }

    public InvParser(InvariantBuilder invariantBuilder, File input,
            Map<Integer, Integer> mapping, String InvType) throws IOException {
        this(invariantBuilder, new FileInputStream(input), mapping, InvType);
    }

    public <T> T invariants() {       
        if(InvType.equals("MI"))
            return (T)minvariants;
        else if(InvType.equals("PI"))
            return (T)pinvariants;
        else
            return (T)tinvariants;
    }
}
