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
 * Thrown when an invalid (= non-existing) ID is passed to a function expecting
 * a valid ID of a {@link UniquePetriNetEntity} in a Petri net.
 * @author Konrad Rudolph
 */
public final class InvalidIdException extends RuntimeException {
    private static final long serialVersionUID = -1937253918390279150L;
    private final int id;
    
    public InvalidIdException(int id) {
        super("Invalid id " + id);
        this.id = id;
    }
    
    public int getId() {
        return id;
    }
}
