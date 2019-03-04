/*
 *
 *  This file ist part of the software MonaLisa.
 *  MonaLisa is free software, dependend on non-free software. For more information read LICENCE and README.
 *
 *  (c) Department of Molecular Bioinformatics, Institue of Computer Science, Johann Wolfgang
 *  Goethe-University Frankfurt am Main, Germany
 *
 */

package monalisa.tools.mcs;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public final class Matrix<Row, Column, T> {
    private final Map<Row, Map<Column, T>> data;
    
    public Matrix(Collection<Row> rowHeaders, Collection<Column> columnHeaders) {
        data = new HashMap<>();
        
        for (Row rowHeader : rowHeaders) {
            Map<Column, T> row = new HashMap<>();
            for (Column columnHeader : columnHeaders)
                row.put(columnHeader, null);
            data.put(rowHeader, row);
        }
    }
    
    public T get(Row row, Column column) {
        return data.get(row).get(column);
    }
    
    public void set(Row row, Column column, T value) {
        data.get(row).put(column, value);
    }
}
