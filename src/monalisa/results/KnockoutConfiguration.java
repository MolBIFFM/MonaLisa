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

import java.util.Collections;
import java.util.List;

import monalisa.data.pn.UniquePetriNetEntity;
import monalisa.resources.StringResources;
import monalisa.tools.knockout.KnockoutAlgorithm;

public final class KnockoutConfiguration implements Configuration {

    private static final long serialVersionUID = -7979281931471497240L;
    private final KnockoutAlgorithm algorithm;
    private final List<? extends UniquePetriNetEntity> entities;
    private final String userDefinedName;

    public KnockoutConfiguration(KnockoutAlgorithm algorithm, String userDefinedName) {
        this(algorithm, Collections.<UniquePetriNetEntity>emptyList(), userDefinedName);
    }

    public KnockoutConfiguration(KnockoutAlgorithm algorithm,
            List<? extends UniquePetriNetEntity> entities, String userDefinedName) {
        this.algorithm = algorithm;
        this.entities = entities;
        this.userDefinedName = userDefinedName;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        } else if (obj instanceof KnockoutConfiguration) {
            KnockoutConfiguration other = (KnockoutConfiguration) obj;
            return getAlgorithm() == other.getAlgorithm() && getEntities().equals(other.getEntities());
        } else {
            return false;
        }
    }

    @Override
    public int hashCode() {
        return getAlgorithm().hashCode() ^ getEntities().hashCode();
    }

    @Override
    public String toString() {
        if (getEntities().isEmpty()) {
            return algorithmName();
        } else {
            return String.format("%s-%d-entities", algorithmName(), getEntities().size());
        }
    }

    @Override
    public String toString(StringResources strings) {
        return strings.get(getUserDefinedName());
    }

    private String algorithmName() {
        return getAlgorithm().getClass().getSimpleName().replace("Knockout", "");
    }

    @Override
    public Boolean isExportable() {
        return true;
    }

    /**
     * @return the algorithm
     */
    public KnockoutAlgorithm getAlgorithm() {
        return algorithm;
    }

    /**
     * @return the entities
     */
    public List<? extends UniquePetriNetEntity> getEntities() {
        return entities;
    }

    /**
     * @return the userDefinedName
     */
    public String getUserDefinedName() {
        return userDefinedName;
    }
}
