/*
 *
 *  This file is part of the software MonaLisa.
 *  MonaLisa is free software, dependent on non-free software. For more information read LICENCE and README.
 *
 *  (c) Department of Molecular Bioinformatics, Institute of Computer Science, Johann Wolfgang
 *  Goethe-University Frankfurt am Main, Germany
 *
 */

package monalisa.addons.annotations;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 *
 * @author Jens Einloft
 */
public class ModellerWrapper {

    private static final Logger LOGGER = LogManager.getLogger(ModellerWrapper.class);
    private final String fName;
    private final String lName;
    private final String organisation;
    private final String email;

    public ModellerWrapper(String fName, String lName, String organisation, String email) {
        LOGGER.debug("Creating new ModellerWrapper");
        this.fName = fName;
        this.lName = lName;
        this.organisation = organisation;
        this.email = email;
    }

    public String getfName() {
        return fName;
    }

    public String getlName() {
        return lName;
    }

    public String getOrganisation() {
        return organisation;
    }

    public String getEmail() {
        return email;
    }

    @Override
    public String toString() {
        return fName+" "+lName;
    }

}
