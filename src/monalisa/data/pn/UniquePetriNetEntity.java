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

/**
 * A Petri net entity that is identifiable by some unique identifier.
 *
 * Unique entities may be used meaningfully in equality comparisons, and as keys
 * in mapping relations. Notice that the object <em>must</em> have a unique
 * identifier, or else using it may result in undefined behavior.
 *
 * @author Konrad Rudolph
 */
public abstract class UniquePetriNetEntity extends AbstractPetriNetEntity {

    private static final long serialVersionUID = 2189444289799672226L;

    protected UniquePetriNetEntity(int id) {
        putProperty("id", id);
    }

    protected UniquePetriNetEntity(UniquePetriNetEntity other) {
        super(other);
        putProperty("id", other.id());
    }

    public int id() {
        return ((Integer) getProperty("id"));
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
        final UniquePetriNetEntity other = (UniquePetriNetEntity) obj;
        if (id() != ((UniquePetriNetEntity) obj).id()) {
            return false;
        }
        return true;
    }
}
