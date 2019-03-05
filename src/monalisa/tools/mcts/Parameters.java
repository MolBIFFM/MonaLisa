/*
 *
 *  This file is part of the software MonaLisa.
 *  MonaLisa is free software, dependent on non-free software. For more information read LICENCE and README.
 *
 *  (c) Department of Molecular Bioinformatics, Institute of Computer Science, Johann Wolfgang
 *  Goethe-University Frankfurt am Main, Germany
 *
 */

package monalisa.tools.mcts;

public final class Parameters {
    private final int invariantType;
    private final boolean deleteTrivialInvariants;
    private final boolean supportOriented;
    
    public Parameters(int invariantType, boolean deleteTrivialInvariants,
            boolean supportOriented) {
        this.invariantType = invariantType;
        this.deleteTrivialInvariants = deleteTrivialInvariants;
        this.supportOriented = supportOriented;
    }
    
    public int getInvariantType() {
        return invariantType;
    }
    
    public boolean canDeleteTrivialInvariants() {
        return deleteTrivialInvariants;
    }
    
    public boolean doesSupportOriented() {
        return supportOriented;
    }
}
