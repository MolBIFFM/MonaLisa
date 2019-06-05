/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
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
import monalisa.data.pn.MInvariant;
import monalisa.data.pn.Transition;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
/**
 *
 * @author daniel
 */

public final class MInvariants implements Result, Collection<MInvariant> {
    private static final long serialVersionUID = 8293263678484610772L;
    private final List<MInvariant> minvariants;
    private static final Logger LOGGER = LogManager.getLogger(TInvariants.class);

    public MInvariants(List<MInvariant> tinvariants) {
        this.minvariants = Collections.unmodifiableList(tinvariants);
    }

    @Override
    public void export(File path, Configuration config, Project project) throws IOException {
        try (PrintWriter printer = new PrintWriter(path)) {
            LOGGER.info("Exporting M-Invariant results");
            Map<Transition, Integer> transitionMap = new HashMap<>();
            StringBuilder sb = new StringBuilder();

            sb.append("# reaction_id:name\n");

            int i = 1;
            for(Transition t : project.getPetriNet().transitions()) {
                transitionMap.put(t, i);
                sb.append(i++);
                sb.append(":");
                sb.append(t.<String>getProperty("name"));
                sb.append("\n");
            }

            sb.append("\n# em_id:factor*reaction_id; ...\n");

            for(MInvariant minv : minvariants) {
                sb.append(minv.id()+1);
                sb.append(":");

                for(Transition t : minv.transitions()) {
                    sb.append(minv.factor(t));
                    sb.append("*");
                    sb.append(transitionMap.get(t));
                    sb.append(";");
                }
                sb.setLength(sb.length() - 1);
                sb.append("\n");
            }

            printer.print(sb.toString());
            printer.close();
            LOGGER.info("Successfully exported M-Invariant results");
        }
    }

    @Override
    public String toString() {
        return minvariants.toString();
    }

    private static String transitionsToString(MInvariant tinvariant, Boolean printId) {
        StringBuilder ret = new StringBuilder();
        boolean first = true;

        for (Transition transition : tinvariant) {
            if (first)
                first = false;
            else
                ret.append(" ");
            if (tinvariant.factor(transition) != 1)
                ret.append(String.format("%d*", tinvariant.factor(transition)));

            if(printId)
                ret.append(transition.id()+1);
            else
                ret.append(((String) transition.getProperty("name")).replace(" ", "_"));
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

    @Override
    public Iterator<MInvariant> iterator() {
        return minvariants.iterator();
    }

    @Override
    public boolean add(MInvariant o) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean addAll(Collection<? extends MInvariant> c) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void clear() {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean contains(Object o) {
        return minvariants.contains(o);
    }

    @Override
    public boolean containsAll(Collection<?> c) {
        return minvariants.containsAll(c);
    }

    @Override
    public boolean isEmpty() {
        return minvariants.isEmpty();
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
        return minvariants.size();
    }

    @Override
    public Object[] toArray() {
        return minvariants.toArray();
    }

    @Override
    public <T> T[] toArray(T[] a) {
        return minvariants.toArray(a);
    }

    public MInvariant getTInvariant(int i) {
        return minvariants.get(i);
    }
}
