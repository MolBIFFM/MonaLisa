/*
 *
 *  This file is part of the software MonaLisa.
 *  MonaLisa is free software, dependent on non-free software. For more information read LICENCE and README.
 *
 *  (c) Department of Molecular Bioinformatics, Institute of Computer Science, Johann Wolfgang
 *  Goethe-University Frankfurt am Main, Germany
 *
 */
package monalisa.results;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import monalisa.Project;
import monalisa.data.pn.PInvariant;
import monalisa.data.pn.Place;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public final class PInvariants implements Result, Collection<PInvariant> {
    private static final long serialVersionUID = 8293263678484610772L;
    private final List<PInvariant> pinvariants;
    private static final Logger LOGGER = LogManager.getLogger(PInvariants.class);

    public PInvariants(List<PInvariant> pinvariants) {
        this.pinvariants = Collections.unmodifiableList(pinvariants);
    }

    @Override
    public void export(File path, Configuration config, Project project) throws IOException {
        try (PrintWriter printer = new PrintWriter(path)) {
            LOGGER.info("Exporting P-Invariant results");
            Map<Place, Integer> placeMap = new HashMap<>();
            StringBuilder sb = new StringBuilder();

            sb.append("# species_id:name\n");

            int i = 1;
            for(Place p : project.getPetriNet().places()) {
                placeMap.put(p, i);
                sb.append(i++);
                sb.append(":");
                sb.append(p.<String>getProperty("name"));
                sb.append("\n");
            }

            sb.append("\n# pinvariant_id:factor*species_id; ...\n");

            for(PInvariant pinv : pinvariants) {
                sb.append(pinv.id()+1);
                sb.append(":");

                for(Place p : pinv.places()) {
                    sb.append(pinv.factor(p));
                    sb.append("*");
                    sb.append(placeMap.get(p));
                    sb.append(";");
                }
                sb.setLength(sb.length() - 1);
                sb.append("\n");
            }

            printer.print(sb.toString());
            printer.close();
            LOGGER.info("Successfully exported P-Invariant results");
        }
    }

    @Override
    public String toString() {
        return pinvariants.toString();
    }

    private static String placesToString(PInvariant pinvariant, Boolean printId) {
        StringBuilder ret = new StringBuilder();
        boolean first = true;

        for (Place place : pinvariant) {
            if (first)
                first = false;
            else
                ret.append(" ");
            if (pinvariant.factor(place) != 1)
                ret.append(String.format("%d*", pinvariant.factor(place)));

            if(printId)
                ret.append(place.id()+1);
            else
                ret.append(((String) place.getProperty("name")).replace(" ", "_"));
        }

        return ret.toString();
    }

    private static List<String> paragraphize(String text, String hangingIndent, int lineLength) {
        List<String> ret = new ArrayList<>();
        int startPos = 0;
        String indent = "";

        while (text.length() - startPos > lineLength) {
            // Go forward to the theoretical end of the line and walk backwards
            // to the beginning of the current word.
            int prev = startPos;
            startPos += lineLength;
            while (!Character.isWhitespace(text.charAt(startPos)) && startPos >= prev)
                startPos--;
            // Failsafe if the line has no spaces:
            if (startPos == prev)
                startPos += lineLength; // Don't care: cut the word.

            ret.add(indent + text.substring(prev, startPos));
            startPos++; // Skip whitespace.

            // Adjust line length by hanging indent.
            lineLength -= hangingIndent.length();
            indent = hangingIndent;
        }

        // Add the dangling line.
        if (startPos < text.length())
            ret.add(indent + text.substring(startPos));

        return ret;
    }

    @Override
    public String filenameExtension() {
        return "inv";
    }

//    public Collection<PInvariant> nonTrivialTInvariants() {
//        List<PInvariant> nonTrivial = new ArrayList<PInvariant>();
//        for(PInvariant pinvariant : this) {
//            if(!pinvariant.isTrivial())
//                nonTrivial.add(pinvariant);
//        }
//        return nonTrivial;
//    }

    @Override
    public Iterator<PInvariant> iterator() {
        return pinvariants.iterator();
    }

    @Override
    public boolean add(PInvariant o) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean addAll(Collection<? extends PInvariant> c) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void clear() {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean contains(Object o) {
        return pinvariants.contains(o);
    }

    @Override
    public boolean containsAll(Collection<?> c) {
        return pinvariants.containsAll(c);
    }

    @Override
    public boolean isEmpty() {
        return pinvariants.isEmpty();
    }

    @Override
    public boolean remove(Object o) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean removeAll(Collection<?> c) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean retainAll(Collection<?> c) {
        throw new UnsupportedOperationException();
    }

    @Override
    public int size() {
        return pinvariants.size();
    }

    @Override
    public Object[] toArray() {
        return pinvariants.toArray();
    }

    @Override
    public <T> T[] toArray(T[] a) {
        return pinvariants.toArray(a);
    }

    public PInvariant getTInvariant(int i) {
        return pinvariants.get(i);
    }
}