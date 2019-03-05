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

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

import monalisa.data.PropertyList;

/**
 * Any entity in a Petri net (place, transition, arc).
 * @author Konrad Rudolph
 */
public abstract class AbstractPetriNetEntity implements Externalizable {
    private static final long serialVersionUID = 6440439443439887965L;
    
    private PropertyList properties = new PropertyList();

    /**
     * Creates a new Petri net entity.
     */
    protected AbstractPetriNetEntity() { }
    
    /**
     * Creates a copy of the specified Petri net entity.
     * @param other The Petri net entity to copy.
     */
    protected AbstractPetriNetEntity(AbstractPetriNetEntity other) {
        for (String key : other.properties) {
            putProperty(key, other.getProperty(key));
        }
    }
    
    /**
     * Test whether the entity has a given property.
     * @param key The key to look for.
     * @return <code>true</code>, if the key is present, otherwise <code>false</code>.
     */
    public boolean hasProperty(String key) {
        return properties.has(key);
    }  
    
    /**
     * Retrieve a strongly typed property, based on its key.
     * @param <T> The type of the property to retrieve.
     * @param key The key of the property.
     * @return The property, cast to type <code>T</code>.
     */
    public <T> T getProperty(String key) {
        return properties.<T>get(key);
    }
    
    /**
     * Retrieve the whole PropertyList
     * @return The PropertyList of the PetriNetEntity
     */
    public PropertyList getPropertyList() {
        return this.properties;
    } 
    
    /**
     * Retrieve a strongly typed property, based on its key.
     * If the property key doesn't exist, return a default value instead.
     * @param <T> The type of the property to retrieve.
     * @param key The key of the property.
     * @param defaultValue The default value.
     * @return The property value, if {@code key} exists, else {@code defaultValue}.
     */
    public <T> T getValueOrDefault(String key, T defaultValue) {
        if (hasProperty(key)) {
            return (T)getProperty(key);
        }
        else
            return defaultValue;
    }
    
    /**
     * Add a property to the Petri net entity.
     * @param <T> The type of the property.
     * @param key The key of the property.
     * @param value The property.
     */
    public <T> void putProperty(String key, T value) {
        if(key.equals("name"))
            value = (T)((String)value).replace(" ", "_");
        properties.put(key, value);
    }
    
    /**
     * Removes a property from the properties list 
     * @param key 
     */
    public void removeProperty(String key) {
        if(properties.has(key)) {
            properties.remove(key);
        }
    }
    
    /**
     * Set a new PropertyList
     * @param pl 
     */
    public void setPropertyList(PropertyList pl) {
        this.properties = pl;
    }     
    
    @Override
    public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
        properties = (PropertyList) in.readObject();
    }
    
    @Override
    public void writeExternal(ObjectOutput out) throws IOException {
        out.writeObject(properties);
    }
}
