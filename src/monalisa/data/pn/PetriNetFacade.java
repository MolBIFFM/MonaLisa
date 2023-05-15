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

import java.util.Collection;
import java.util.List;
import java.util.Map;
import monalisa.data.PropertyList;

/**
 * A Facade for the PetriNet class. This class is used in all AddOns. The Facade
 * provides only a subset of the PetriNet class functions. That prevents that
 * AddOns can manipulate the PetriNet.
 *
 * @author Jens Einloft
 */
public class PetriNetFacade extends AbstractPetriNetEntity {

    private final PetriNet pn;

    public PetriNetFacade(PetriNet pn) {
        this.pn = pn;
    }

    /**
     * Return a copy of the actual PetriNet Instance
     *
     * @return
     */
    public PetriNet getPNCopy() {
        return new PetriNet(pn);
    }

    /**
     * @see monalisa.data.pn.PetriNet#getTokens(Place)
     */
    public Long getTokens(Place place) {
        return pn.getTokens(place);
    }

    /**
     * @see monalisa.data.pn.PetriNet#places()
     */
    public Collection<Place> places() {
        return pn.places();
    }

    /**
     * @see monalisa.data.pn.PetriNet#transitions()
     */
    public Collection<Transition> transitions() {
        return pn.transitions();
    }

    /**
     * @see monalisa.data.pn.PetriNet#findPlace(int)
     */
    public Place findPlace(int id) {
        return pn.findPlace(id);
    }

    /**
     * @see monalisa.data.pn.PetriNet#findTranistion(int)
     */
    public Transition findTransition(int id) {
        return pn.findTransition(id);
    }

    /**
     * @see monalisa.data.pn.PetriNet#marking(int)
     */
    public Map<Place, Long> marking() {
        return pn.marking();
    }

    /**
     * @see monalisa.data.pn.PetriNet#getTransitionsFor(Place)
     */
    public List<Transition> getTransitionsFor(Place from) {
        return pn.getTransitionsFor(from);
    }

    /**
     * @see monalisa.data.pn.PetriNet#getPlacesFor(Transition)
     */
    public List<Place> getPlacesFor(Transition from) {
        return pn.getPlacesFor(from);
    }

    /**
     * @see monalisa.data.pn.PetriNet#getInputTransitionsFor(Place)
     */
    public List<Transition> getInputTransitionsFor(Place to) {
        return pn.getInputTransitionsFor(to);
    }

    /**
     * @see monalisa.data.pn.PetriNet#getInputPlacesFor(Transition)
     */
    public List<Place> getInputPlacesFor(Transition to) {
        return pn.getInputPlacesFor(to);
    }

    /**
     * @see monalisa.data.pn.PetriNet#getArc(Place, Transition)
     */
    public Arc getArc(Place from, Transition to) {
        return pn.getArc(from, to);
    }

    /**
     * @see monalisa.data.pn.PetriNet#getArc(Transition, Place)
     */
    public Arc getArc(Transition from, Place to) {
        return pn.getArc(from, to);
    }

    /**
     * @see monalisa.data.pn.PetriNet#hasProperty(String)
     */
    @Override
    public boolean hasProperty(String key) {
        return pn.hasProperty(key);
    }

    /**
     * @see monalisa.data.pn.PetriNet#getProperty(String)
     */
    @Override
    public <T> T getProperty(String key) {
        return pn.getProperty(key);
    }

    @Override
    public <T> T getValueOrDefault(String key, T defaultValue) {
        return pn.getValueOrDefault(key, defaultValue);
    }

    /**
     * @see monalisa.data.pn.PetriNet#removeProperty(String)
     */
    @Override
    public void removeProperty(String key) {
        pn.removeProperty(key);
    }

    /**
     * @see monalisa.data.pn.PetriNet#putProperty(String,T)
     */
    @Override
    public <T> void putProperty(String key, T value) {
        pn.putProperty(key, value);
    }

    /**
     * @see
     * monalisa.data.pn.PetriNet#setPropertyList(monalisa.data.PropertyList)
     */
    @Override
    public void setPropertyList(PropertyList pl) {
        pn.setPropertyList(pl);
    }

    /**
     * @see monalisa.data.pn.PetriNet#getPropertyList()
     */
    @Override
    public PropertyList getPropertyList() {
        return pn.getPropertyList();
    }

    /**
     * @see monalisa.data.pn.PetriNet#getNumberOfEdges()
     */
    public int getNumberOfEdges() {
        return pn.getNumberOfEdges();
    }

}
