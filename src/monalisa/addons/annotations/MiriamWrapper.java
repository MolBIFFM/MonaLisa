/*
 *
 *  This file ist part of the software MonaLisa.
 *  MonaLisa is free software, dependend on non-free software. For more information read LICENCE and README.
 *
 *  (c) Department of Molecular Bioinformatics, Institue of Computer Science, Johann Wolfgang
 *  Goethe-University Frankfurt am Main, Germany
 *
 */

package monalisa.addons.annotations;

import org.sbml.jsbml.CVTerm;
import org.sbml.jsbml.CVTerm.Qualifier;
import org.sbml.jsbml.CVTerm.Type;

/**
 *
 * @author Jens Einloft
 */
public class MiriamWrapper {

    private Qualifier qualifier;
    private String uri;
    private CVTerm cvt;
    
    public MiriamWrapper(Qualifier qualifier, String uri) {
        this.qualifier = qualifier;
        this.uri = uri;            
        
        this.cvt = new CVTerm();  
        
        if(qualifier.isBiologicalQualifier()) {        
            this.cvt.setQualifierType(Type.BIOLOGICAL_QUALIFIER);
            this.cvt.setBiologicalQualifierType(qualifier);
        } else if(qualifier.isModelQualifier()) {
            this.cvt.setQualifierType(Type.MODEL_QUALIFIER);
            this.cvt.setModelQualifierType(qualifier);            
        }
        this.cvt.addResource(uri);
    }
    
    public MiriamWrapper(CVTerm cvt) {   
        if(cvt.getQualifierType().equals(Type.BIOLOGICAL_QUALIFIER)) {
            this.qualifier = cvt.getBiologicalQualifierType();
        } else {
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
    }
    
    public String getURI() {
        return this.uri;
    }    
    
    @Override
    public String toString() {
        return this.qualifier.name()+" : "+this.uri.substring(this.uri.lastIndexOf("/")+1);
    }
    
}
