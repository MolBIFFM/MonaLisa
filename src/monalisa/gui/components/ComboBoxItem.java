/*
 *
 *  This file is part of the software MonaLisa.
 *  MonaLisa is free software, dependent on non-free software. For more information read LICENCE and README.
 *
 *  (c) Department of Molecular Bioinformatics, Institute of Computer Science, Johann Wolfgang
 *  Goethe-University Frankfurt am Main, Germany
 *
 */
package monalisa.gui.components;

/**
 * Encapsulates an arbitrary item along with a label.
 *
 * @author Konrad Rudolph
 */
public final class ComboBoxItem {

    private final Object item;
    private final String label;

    public ComboBoxItem(Object item) {
        this(item, item.toString());
    }

    public ComboBoxItem(Object item, String label) {
        this.item = item;
        this.label = label;
    }

    public Object getItem() {
        return item;
    }

    @Override
    public String toString() {
        return label;
    }
}
