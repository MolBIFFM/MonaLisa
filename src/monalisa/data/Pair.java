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

import java.io.Serializable;
import java.util.Objects;

public final class Pair<T1, T2> implements Serializable {
    private static final long serialVersionUID = -7730842564970076343L;

    private final T1 first;
    private final T2 second;
    
    public Pair(T1 first, T2 second) {
        this.first = first;
        this.second = second;
    }
    
    public T1 first() { return first; }
    
    public T2 second() { return second; }
    
    @Override
    public String toString() {
        return String.format("{%s, %s}", first, second);
    }
    
    @SuppressWarnings("unchecked")
    @Override
    public boolean equals(Object obj) {
        if (obj == this)
            return true;
        else if (obj instanceof Pair<?, ?>) {
            Pair<T1, T2> other = (Pair) obj;
            return equals(first, other.first) && equals(second, other.second);
        }
        return false;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 17 * hash + Objects.hashCode(this.first);
        hash = 17 * hash + Objects.hashCode(this.second);
        return hash;
    }
        
    public static <T1, T2> Pair<T1, T2> of(T1 first, T2 second) {
        return new Pair<>(first, second);
    }
    
    private static boolean equals(Object a, Object b) {        
        return a == null ? b == null : a.equals(b);
    }
}
