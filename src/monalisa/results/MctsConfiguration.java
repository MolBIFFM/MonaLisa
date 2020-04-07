/*
 *
 *  This file is part of the software MonaLisa.
 *  MonaLisa is free software, dependent on non-free software. For more information read LICENCE and README.
 *
 *  (c) Department of Molecular Bioinformatics, Institute of Computer Science, Johann Wolfgang
 *  Goethe-University Frankfurt am Main, Germany
 *
 */
package monalisa.results;

import monalisa.resources.StringResources;

public final class MctsConfiguration implements Configuration {

    private static final long serialVersionUID = -8414384576164538792L;
    private final boolean occurrenceOriented;
    private final boolean includeTrivialTInvariants;

    public MctsConfiguration(boolean strong, boolean includeTrivialTInvariants) {
        this.occurrenceOriented = strong;
        this.includeTrivialTInvariants = includeTrivialTInvariants;
    }

    public boolean isStrong() {
        return occurrenceOriented;
    }

    public boolean hasTrivialTInvariants() {
        return includeTrivialTInvariants;
    }

    @Override
    public String toString() {
        return String.format("%s-%s", occurrenceOriented
                ? "elementary-subset" : "support",
                (includeTrivialTInvariants ? "with" : "without") + "-trivial-em");
    }

    @Override
    public String toString(StringResources strings) {
        String kind = strings.get(occurrenceOriented
                ? "MctsOccurrenceOriented" : "MctsSupportOriented");
        String with = strings.get(includeTrivialTInvariants
                ? "With" : "Without");
        return strings.get("Mcts", kind, with);
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        return prime * (includeTrivialTInvariants ? 1231 : 1237) + (occurrenceOriented ? 1231 : 1237);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        MctsConfiguration other = (MctsConfiguration) obj;
        return occurrenceOriented == other.occurrenceOriented
                && includeTrivialTInvariants == other.includeTrivialTInvariants;
    }

    @Override
    public Boolean isExportable() {
        return true;
    }
}
