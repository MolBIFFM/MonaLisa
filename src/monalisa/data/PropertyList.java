/*
 *
 *  This file ist part of the software MonaLisa.
 *  MonaLisa is free software, dependend on non-free software. For more information read LICENCE and README.
 *
 *  (c) Department of Molecular Bioinformatics, Institue of Computer Science, Johann Wolfgang
 *  Goethe-University Frankfurt am Main, Germany
 *
 */

package monalisa.data;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;

/**
 * A list of strongly typed properties, identified by unique keys.
 * They keys of the collection may be iterated over.
 * @author Konrad Rudolph
 * @see java.util.HashMap
 */
public final class PropertyList implements Iterable<String>, Externalizable {
    private static final long serialVersionUID = 3325754359047699066L;

    private HashMap<String, Object> store = new HashMap<>();
    
    /**
     * Test whether a given key is present in the property list.
     * @param key The key to look for.
     * @return <code>true</code>, if the key is present, otherwise <code>false</code>.
     */
    public boolean has(String key) {
        return store.containsKey(key);
    }
    
    /**
     * Retrieve a strongly typed property, based on its key.
     * @param <T> The type of the property to retrieve.
     * @param key The key of the property.
     * @return The property, cast to type <code>T</code>.
     */
    @SuppressWarnings("unchecked")
    public <T> T get(String key) {
        return (T) getRaw(key);
    }
    
    /**
     * Retrieve an untyped property, based on its key.
     * @param key The key of the property.
     * @return The property.
     */
    public Object getRaw(String key) {
        return store.get(key);
    }
    
    /**
     * Put a property into the property list.
     * @param <T> The type of the property.
     * @param key The key of the property.
     * @param value The property.
     */
    public <T> void put(String key, T value) {
        store.put(key, value);             
    }
    
    /**
     * Removes a property from the property list
     * @param key 
     */
    public void remove(String key) {
        store.remove(key);
    }
    
    /**
     * @see java.util.HashMap#equals(Object)
     */
    @Override
    public boolean equals(Object obj) {
        if (obj == this)
            return true;
        else if (obj.getClass() == getClass())
            return super.equals(((PropertyList) obj).store);
        else
            return false;
    }
    
    /**
     * @see java.util.HashMap#hashCode()
     */
    @Override
    public int hashCode() {
        return store.hashCode();
    }
    
    /**
     * @see java.util.HashMap#toString()
     */
    @Override
    public String toString() {
        return store.toString();
    }

    @SuppressWarnings("unchecked")
    @Override
    public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
        store = (HashMap<String, Object>) in.readObject();
    }

    @Override
    public void writeExternal(ObjectOutput out) throws IOException {
        out.writeObject(store);
    }

    @Override
    public Iterator<String> iterator() {
        return Collections.unmodifiableCollection(store.keySet()).iterator();
    }
}
