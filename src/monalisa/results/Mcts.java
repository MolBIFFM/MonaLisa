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
import monalisa.data.pn.PetriNet;
import monalisa.data.pn.TInvariant;
import monalisa.data.pn.Transition;

public final class Mcts implements Result, Collection<TInvariant> {
    private static final long serialVersionUID = -2642935086875477004L;
    private final List<TInvariant> mcts;

    public Mcts(List<TInvariant> mcts) {
        this.mcts = mcts;
    }

    @Override
    public void export(File path, Configuration config, Project project) throws IOException {
        MctsConfiguration mctsConfig = (MctsConfiguration) config;
        String comment = mctsConfig.isStrong() ? "# Enzyme Subsets (occurrence-oriented)" : "# Maximal Common Transition Sets (support-oriented)";
        mctSetExport(project, comment, path, mcts);
    }

    @Override
    public String filenameExtension() {
        return "mct";
    }
    
    @Override
    public String toString() {
        return mcts.toString();
    }

    @Override
    public boolean add(TInvariant o) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean addAll(Collection<? extends TInvariant> c) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void clear() {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean contains(Object o) {
        return mcts.contains(o);
    }

    @Override
    public boolean containsAll(Collection<?> c) {
        return mcts.containsAll(c);
    }

    @Override
    public boolean isEmpty() {
        return mcts.isEmpty();
    }

    @Override
    public Iterator<TInvariant> iterator() {
        return mcts.iterator();
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
        return mcts.size();
    }

    @Override
    public Object[] toArray() {
        return mcts.toArray();
    }

    @Override
    public <T> T[] toArray(T[] a) {
        return mcts.toArray(a);
    }
    
    private static List<Transition> sortedTransitions(PetriNet petriNet) {
        ArrayList<Transition> result = new ArrayList<>(petriNet.transitions());
        Collections.sort(result);
        return result;
    }

    private static void mctSetExport(Project project, String comment, File file, List<TInvariant> mctSet) {
        try (PrintWriter out = new PrintWriter(file)) {
            List<Transition> transitions = sortedTransitions(project.getPetriNet());
            Map<Transition, Integer> transitionMap = new HashMap<>();
            StringBuilder sb = new StringBuilder(); 
            
            sb.append("# reaction_id:name\n");
            
            int i = 1;
            for(Transition t : transitions) {
                transitionMap.put(t, i);
                sb.append(i++);
                sb.append(":");
                sb.append(t.<String>getProperty("name"));
                sb.append("\n");
            }            
            
            sb.append("\n# mcts_id:reaction_id; ...\n");             
            
            i = 1;
            Transition t;
            for (TInvariant inv : mctSet) {
                List<Integer> values = inv.asVector(transitions);
                
                sb.append(i++);
                sb.append(":");
                
                for (int j = 0; j < values.size(); j++) {
                    if (values.get(j) == 0)
                        continue;
                    t = transitions.get(j);
                    sb.append(transitionMap.get(t));
                    sb.append(";");
                }
                sb.setLength(sb.length() - 1);                
                sb.append("\n");
            }             
            
            out.print(sb.toString());
            out.close();
                       
        } catch (java.io.IOException e) {
            e.printStackTrace();
        }
    }
}