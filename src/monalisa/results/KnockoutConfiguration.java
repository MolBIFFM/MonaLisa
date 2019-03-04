/*
 *
 *  This file ist part of the software MonaLisa.
 *  MonaLisa is free software, dependend on non-free software. For more information read LICENCE and README.
 *
 *  (c) Department of Molecular Bioinformatics, Institue of Computer Science, Johann Wolfgang
 *  Goethe-University Frankfurt am Main, Germany
 *
 */

package monalisa.results;

import java.util.Collections;
import java.util.List;

import monalisa.data.pn.UniquePetriNetEntity;
import monalisa.resources.StringResources;
import monalisa.tools.knockout.KnockoutAlgorithm;

public final class KnockoutConfiguration implements Configuration {
    private static final long serialVersionUID = -7979281931471497240L;
    private final Class<? extends KnockoutAlgorithm> algorithm;
    private final List<? extends UniquePetriNetEntity> entities;
    private final String userDefinedName;

    public KnockoutConfiguration(Class<? extends KnockoutAlgorithm> algorithm, String userDefinedName) {
        this(algorithm, Collections.<UniquePetriNetEntity>emptyList(), userDefinedName);
    }

    public KnockoutConfiguration(Class<? extends KnockoutAlgorithm> algorithm,
            List<? extends UniquePetriNetEntity> entities, String userDefinedName) {
        this.algorithm = algorithm;
        this.entities = entities;
        this.userDefinedName = userDefinedName;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this)
            return true;
        else if (obj instanceof KnockoutConfiguration) {
            KnockoutConfiguration other = (KnockoutConfiguration) obj;
            return algorithm == other.algorithm && entities.equals(other.entities);
        }
        else
            return false;
    }

    @Override
    public int hashCode() {
        return algorithm.hashCode() ^ entities.hashCode();
    }

    @Override
    public String toString() {
        if (entities.isEmpty())
            return algorithmName();
        else
            return String.format("%s-%d-entities", algorithmName(), entities.size());
    }

    @Override
    public String toString(StringResources strings) {
        return strings.get(userDefinedName);
    }

    private String algorithmName() {
        return algorithm.getSimpleName().replace("Knockout", "");
    }

    @Override
    public Boolean isExportable() {
        return true;
    }
}
