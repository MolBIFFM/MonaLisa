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
import org.sbml.jsbml.CVTerm;
import org.sbml.jsbml.CVTerm.Qualifier;
import org.sbml.jsbml.CVTerm.Type;

/**
 *
 * @author Jens Einloft
 */
public class MiriamWrapper {

    private final Logger LOGGER = LogManager.getLogger(MiriamWrapper.class);
    private Qualifier qualifier;
    private String uri;
    private CVTerm cvt;

    public MiriamWrapper(Qualifier qualifier, String uri) {
        LOGGER.debug("Creating new MiriamWrapper for qualifier '" + qualifier.toString() + "'");
        this.qualifier = qualifier;
        this.uri = uri;

        this.cvt = new CVTerm();

        if(qualifier.isBiologicalQualifier()) {
            LOGGER.debug("Qualifier type is biological");
            this.cvt.setQualifierType(Type.BIOLOGICAL_QUALIFIER);
            this.cvt.setBiologicalQualifierType(qualifier);
        } else if(qualifier.isModelQualifier()) {
            LOGGER.debug("Qualifier type is model");
            this.cvt.setQualifierType(Type.MODEL_QUALIFIER);
            this.cvt.setModelQualifierType(qualifier);
        }
        this.cvt.addResource(uri);
    }

    public MiriamWrapper(CVTerm cvt) {
        LOGGER.debug("Creating new MiriamWrapper for CVTerm '" + cvt.toString());
        if(cvt.getQualifierType().equals(Type.BIOLOGICAL_QUALIFIER)) {
            LOGGER.debug("Qualifier type is biological");
            this.qualifier = cvt.getBiologicalQualifierType();
        } else {
            LOGGER.debug("Qualifier type is model");
            this.qualifier = cvt.getModelQualifierType();
        }
        this.uri = (String) (cvt.getResources().toArray())[0];
        this.cvt = cvt;
    }

    public CVTerm getCVTerm() {
        return this.cvt;
    }

    public void setQualifier(Qualifier q) {
        this.qualifier = q;
    }

    public void setURI(String uri) {
        LOGGER.debug("Setting uri for MiriamWrapper '" + this.toString() + "'");
        this.uri = uri;

        this.cvt = new CVTerm();
        if(this.qualifier.isBiologicalQualifier()) {
            this.cvt.setQualifierType(Type.BIOLOGICAL_QUALIFIER);
            this.cvt.setBiologicalQualifierType(qualifier);
        } else if(this.qualifier.isModelQualifier()) {
            this.cvt.setQualifierType(Type.MODEL_QUALIFIER);
            this.cvt.setModelQualifierType(qualifier);
        }
        this.cvt.addResource(uri);
        LOGGER.debug("Finished setting uri for MiriamWrapper '" + this.toString() + "'");
    }

    public String getURI() {
        return this.uri;
    }

    @Override
    public String toString() {
        return this.qualifier.name()+" : "+this.uri.substring(this.uri.lastIndexOf("/")+1);
    }

}
