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
 * A place in a Petri net.
 * @author Konrad Rudolph
 */
public final class Place extends UniquePetriNetEntity implements Comparable<Place> {
    private static final long serialVersionUID = 3704844653776835796L;
    
    private PetriNet container;
    
    /**
     * If a place is declared constant, the number of tokens on this place should not be changed by simulators.
     * No tokens should be added to or removed from it. Can be used to model a constant input.
     */
    private boolean constant = false;
    
    /**
     * Should be used only during deserialization.
     */
    @Deprecated
    public Place() { super(-1); }
    
    public Place(Place other) {
        super(other);
        // Does not belong to a Petri net, do not copy container.
    }

    public Place(int id) {
        super(id);
    }
    
    public void setContainer(PetriNet container) {
        this.container = container;
    }
    
    public PetriNet container() {
        return container;
    }

    public List<Transition> outputs() {
        return container.getTransitionsFor(this);
    }
    
    public List<Transition> inputs() {
        return container.getInputTransitionsFor(this);
    }
    
    public Compartment getCompartment() {
        return container.getCompartmentMap().get(this);       
    }
    
    public void setCompartment(Compartment c) {
        container.setCompartment(this, c);
        this.putProperty("compartment", c);
    }
    
    public void unsetCompartment(Compartment c) {
        container.unsetCompartment(this, c);
        this.putProperty("compartment", null);
    }
    
    /**
     * Returns true, if the place is constant and the number of tokens should not be modified, disregarding simulation steps.
     * @return true if number of tokens remains constant, false if number of tokens can be altered during simulations.
     */
    public boolean isConstant(){
        return this.constant;
    }
    
    /**
     * Set the constant state to true, if the number of tokens on this place should not be modified by simulators.
     * @param cons 
     */
    public void setConstant(boolean cons){
        this.constant = cons;
    }    
    
    @Override
    public int compareTo(Place o) {
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
        final Place other = (Place) obj;
        
        if(this.id() != other.id()) {
            return false;
        }
        
        return true;        
    }
    
    @Override
    public String toString() {
        return this.getProperty("name");
    }
}
