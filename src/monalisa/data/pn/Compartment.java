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

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.io.Serializable;

/**
 *
 * @author Jens Einloft
 */
public class Compartment extends AbstractPetriNetEntity implements Serializable {    
    private static final long serialVersionUID = -9006955431884512984L;
    
    private String name;
    
    public Compartment() {
        this.name = "default";
    }
    
    public Compartment(String name) {
        this.name = name;
    }
    
    /**
     * Returns the name of the compartment
     * @return 
     */
    public String getName() {
        return this.name;
    }      
    
    /**
     * Set the name for the compartment
     * @param name 
     */
    public void setName(String name) {
        this.name = name;
    }
    
    @Override
    public String toString() {
        return this.name;
    }
    
    @SuppressWarnings("unchecked")
    @Override
    public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
        super.readExternal(in);
        this.name = (String) in.readObject();
    }

    @Override
    public void writeExternal(ObjectOutput out) throws IOException {
        super.writeExternal(out);
        out.writeObject(this.name);
    }        
}
