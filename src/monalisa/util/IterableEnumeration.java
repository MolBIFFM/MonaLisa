/*
 *
 *  This file is part of the software MonaLisa.
 *  MonaLisa is free software, dependent on non-free software. For more information read LICENCE and README.
 *
 *  (c) Department of Molecular Bioinformatics, Institute of Computer Science, Johann Wolfgang
 *  Goethe-University Frankfurt am Main, Germany
 *
 */

package monalisa.util;

import java.util.Enumeration;
import java.util.Iterator;

/**
 * An adaptor to make {@link Enumeration}&lt;T&gt; an iterable type.
 * @author Konrad Rudolph
 *
 * @param <T> The enumeration type.
 */
public final class IterableEnumeration<T> implements Iterable<T>, Iterator<T> {
    private final Enumeration<T> enumeration;
    
    public IterableEnumeration(Enumeration<T> enumeration) {
        this.enumeration = enumeration;
    }
    
    public static <T> IterableEnumeration<T> from(Enumeration<T> enumeration) {
        return new IterableEnumeration<>(enumeration);
    }
    
    @Override
    public Iterator<T> iterator() {
        return this;
    }

    @Override
    public boolean hasNext() {
        return enumeration.hasMoreElements();
    }

    @Override
    public T next() {
        return enumeration.nextElement();
    }

    @Override
    public void remove() {
        throw new UnsupportedOperationException();
    }
}
