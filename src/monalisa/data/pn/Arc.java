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

import java.util.Objects;

/**
 * Any arc in a Petri net.
 *
 * @author Konrad Rudolph
 */
public final class Arc extends AbstractPetriNetEntity {

    private static final long serialVersionUID = -1924781466391771603L;

    private Object source;
    private Object aim;

    /**
     * Create a new default arc.
     */
    public Arc() {
        this.source = new Object();
        this.aim = new Object();
    }

    /**
     * Create a new arc.
     */
    public Arc(Object source, Object aim) {
        this.source = source;
        this.aim = aim;
    }

    /**
     * Create a copy of the given arc and all its properties.
     *
     * @param other The other arc.
     */
    public Arc(Arc other) {
        super(other);
        setWeight(other.weight());
    }

    /**
     * Create a new arc with a given weight.
     *
     * @param weight The weight of the arc.
     */
    public Arc(Object source, Object aim, int weight) {
        this.source = source;
        this.aim = aim;
        setWeight(weight);
    }

    /**
     * @return The weight of the arc.
     */
    public int weight() {
        return getValueOrDefault("weight", 1);
    }

    /**
     * Set a new weight for the arc.
     *
     * @param weight The new weight.
     */
    public void setWeight(int weight) {
        putProperty("weight", weight);
    }

    /**
     * Returns the source of the arc
     *
     * @return source
     */
    public Object source() {
        return source;
    }

    /**
     * Returns the aim of the arc
     *
     * @return aim
     */
    public Object aim() {
        return aim;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 53 * hash + Objects.hashCode(this.source);
        hash = 53 * hash + Objects.hashCode(this.aim);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final Arc other = (Arc) obj;
        return (this.source.equals(other.source) && this.aim.equals(other.aim));
    }

}
