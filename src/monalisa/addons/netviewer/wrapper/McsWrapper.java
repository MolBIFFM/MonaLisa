/*
 *
 *  This file is part of the software MonaLisa.
 *  MonaLisa is free software, dependent on non-free software. For more information read LICENCE and README.
 *
 *  (c) Department of Molecular Bioinformatics, Institute of Computer Science, Johann Wolfgang
 *  Goethe-University Frankfurt am Main, Germany
 *
 */
package monalisa.addons.netviewer.wrapper;

import java.util.Set;
import monalisa.data.pn.Transition;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Wrapper Class for Mcs Combobox Items
 *
 * @author Jens Einloft
 */
public class McsWrapper {

    private final Transition objective;
    private final Set<Transition> mcs;
    private final int id;
    private static final Logger LOGGER = LogManager.getLogger(McsWrapper.class);

    public McsWrapper(Set<Transition> input, Transition objective, int id) {
        this.mcs = input;
        this.id = id;
        this.objective = objective;
        LOGGER.debug("Created new McsWrapper " + this.id + " (" + this.mcs.size() + ")");
    }

    public Transition getObjective() {
        return this.objective;
    }

    public Set<Transition> getMcs() {
        return mcs;
    }

    public int getId() {
        return this.id;
    }

    @Override
    public String toString() {
        return "MC-Set " + this.id + " (" + this.mcs.size() + ")";
    }
}
