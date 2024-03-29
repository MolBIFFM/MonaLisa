/*
 *
 *  This file is part of the software MonaLisa.
 *  MonaLisa is free software, dependent on non-free software. For more information read LICENCE and README.
 *
 *  (c) Department of Molecular Bioinformatics, Institute of Computer Science, Johann Wolfgang
 *  Goethe-University Frankfurt am Main, Germany
 *
 */
package monalisa.data.pn;

import java.util.List;

/**
 * A transition in a Petri net.
 *
 * @author Konrad Rudolph
 */
public final class Transition extends UniquePetriNetEntity implements Comparable<Transition> {

    private static final long serialVersionUID = -7083156416077840731L;

    private PetriNet container;

    /**
     * Should be used only during deserialization.
     */
    @Deprecated
    public Transition() {
        super(-1);
    }

    public Transition(Transition other) {
        super(other);
        // Does not belong to a Petri net, do not copy container.
    }

    public Transition(int id) {
        super(id);
    }

    public void setContainer(PetriNet container) {
        this.container = container;
    }

    public PetriNet container() {
        return container;
    }

    public List<Place> outputs() {
        return container.getPlacesFor(this);
    }

    public List<Place> inputs() {
        return container.getInputPlacesFor(this);
    }

    @Override
    public int compareTo(Transition o) {
        return id() - o.id();
    }

    @Override
    public int hashCode() {
        return id();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final Transition other = (Transition) obj;

        if (this.id() != other.id()) {
            return false;
        }

        return true;
    }

    @Override
    public String toString() {
        return this.getProperty("name");
    }
}
