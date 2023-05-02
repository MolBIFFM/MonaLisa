/*
 *
 *  This file is part of the software MonaLisa.
 *  MonaLisa is free software, dependent on non-free software. For more information read LICENCE and README.
 *
 *  (c) Department of Molecular Bioinformatics, Institute of Computer Science, Johann Wolfgang
 *  Goethe-University Frankfurt am Main, Germany
 *
 */

package monalisa.data.input;


import org.sbml.jsbml.ext.qual.QualModelPlugin;
import org.sbml.jsbml.SBMLDocument;
import org.sbml.jsbml.Species;
import org.sbml.jsbml.Reaction;
import org.sbml.jsbml.Model;
import org.sbml.jsbml.ext.qual.Transition;
import org.sbml.jsbml.ext.qual.Input;
import org.sbml.jsbml.SpeciesReference;
import org.sbml.jsbml.ext.qual.Output;
import org.sbml.jsbml.ext.layout.Layout;
import org.sbml.jsbml.ext.layout.LayoutModelPlugin;
import org.sbml.jsbml.ext.layout.LayoutConstants;
import org.sbml.jsbml.ext.qual.QualitativeSpecies;
import org.sbml.jsbml.ListOf;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.ArrayList;



/**
 * Conversion of an SBML qual file into an SBML standard format file.
 *
 * @author Ann-Kathrin Otto
 */

public class SbmlQualInputHandler {

    public static SBMLDocument convertqualtosbmlcore (SBMLDocument document) throws IOException {


        Model model = document.getModel();
        // get the QualModelPlugin object
        QualModelPlugin qual_ext = (QualModelPlugin) model.getPlugin("qual");

        
        // initialize variables
        List<String> qualTransitionsList = new ArrayList<>();
        List<List <Integer>> qualInputLevelList = new ArrayList<>();
        List<List <String>> qualInputSpeciesList = new ArrayList<>();
        List<List <Integer>> qualOutputLevelList = new ArrayList<>();
        List<List <String>> qualOutputSpeciesList = new ArrayList<>();
        List<List <Input>> qualInputsList = new ArrayList<>();
        List<List <Output>> qualOutputsList = new ArrayList<>();
        List<Input> qualInput = null;


        // get the list of qualitative species and transitions
        List<QualitativeSpecies> qualSpeciesList = qual_ext.getListOfQualitativeSpecies();
        List<Transition> qualTransitionsListOne = qual_ext.getListOfTransitions();


        // get the list of transition ID' as strings
        for (int i = 0; i < qualTransitionsListOne.size(); i++) {
            Transition t = qualTransitionsListOne.get(i);
            String transitionId = t.getId();
            qualTransitionsList.add(transitionId);
        }


        // get the input species and input edge weight of the transitions
        for (int i = 0; i < qualTransitionsList.size(); i++) {
            List<Integer> inputLevel = new ArrayList<>();
            List<String> inputSpecies = new ArrayList<>();
            Transition qualTransition = qual_ext.getTransition(i);
            qualInput = qualTransition.getListOfInputs();
            qualInputsList.add(qualInput);

            for (int j = 0; j < qualInput.size(); j++) {
                Integer qualInputLevel = null;
                if (qualInput.get(j).isSetThresholdLevel() == true) {
                    qualInputLevel = qualInput.get(j).getThresholdLevel();
                }
                else {
                    qualInputLevel = 1;
                }

                String qualInputSpecies = qualInput.get(j).getQualitativeSpecies();
                inputLevel.add(qualInputLevel);
                inputSpecies.add(qualInputSpecies);
            }
            qualInputLevelList.add(inputLevel);
            qualInputSpeciesList.add(inputSpecies);
        }


        // get the output species and output edge weight of the transitions
        for (int i = 0; i < qualTransitionsList.size(); i++) {
            List<Integer> outputLevel = new ArrayList<>();
            List<String> outputSpecies = new ArrayList<>();
            Transition qualTransition = qual_ext.getTransition(i);
            List<Output> qualOutput = qualTransition.getListOfOutputs();
            qualOutputsList.add(qualOutput);

            for (int j = 0; j < qualOutput.size(); j++) {
                Integer qualOutputLevel = null;
                if (qualOutput.get(j).isSetOutputLevel() == true) {
                    qualOutputLevel = qualOutput.get(j).getOutputLevel();
                }
                else {
                    qualOutputLevel = 1;
                }

                outputLevel.add(qualOutputLevel);
                String qualOutputSpecies = qualOutput.get(j).getQualitativeSpecies();
                outputSpecies.add(qualOutputSpecies);
            }

            qualOutputLevelList.add(outputLevel);
            qualOutputSpeciesList.add(outputSpecies);
        }


        // get the initial token amount
        List<Integer> initial_level = new ArrayList<>();
        for (QualitativeSpecies species : qualSpeciesList) {
            Integer qualInitialLevel = null;
            if (species.isSetInitialLevel() == true) {
                qualInitialLevel = species.getInitialLevel();
            }
            else {
                qualInitialLevel = 0;
            }
            initial_level.add(qualInitialLevel);
        }
        
      
        // create a new sbml document to put in the sbmlqual document information
        SBMLDocument convertedDoc = new SBMLDocument(document.getLevel(), document.getVersion());
        Model convertedModel = convertedDoc.createModel("petri_net");
        
        
        // get the list of layouts from the qual file and add it to the new file
        LayoutModelPlugin plugin = (LayoutModelPlugin) document.getModel().getPlugin(LayoutConstants.namespaceURI);
        ListOf<Layout> layoutList = plugin.getListOfLayouts();
        if (!layoutList.isEmpty()) {
            LayoutModelPlugin newplugin = (LayoutModelPlugin) convertedDoc.getModel().getPlugin(LayoutConstants.namespaceURI);
            newplugin.setListOfLayouts(layoutList);
        }
        

        // set the model ID
        if (model.isSetId() == true) {
            convertedModel.setId(model.getId());
        }


        // set the namespaces (except for qual)
        Map<String,String> namespaces = document.getDeclaredNamespaces();
        for (Map.Entry<String,String> entry : namespaces.entrySet()) {
            String prefix = entry.getKey();
            String uri = entry.getValue();
            if (!prefix.contains("qual")) {
                convertedDoc.addDeclaredNamespace(prefix, uri);
            }
        }

        
        // set the qualitative species as species
        List<QualitativeSpecies> places = qualSpeciesList;
        int index = 0;
        for (QualitativeSpecies p : places) {
            Species s = convertedModel.createSpecies();
            s.setId(p.getId());
            s.setCompartment(p.getCompartment());

            if (!p.getSBOTermID().isEmpty()) {
                s.setSBOTerm(p.getSBOTermID());
            }

            if (p.isSetConstant() == true) {
                s.setConstant(p.getConstant());
            }
            s.setInitialAmount(initial_level.get(index));

            if (!p.getCVTerms().isEmpty()) {
                for (int cv = 0; cv < p.getCVTerms().size(); cv++) {
                    s.addCVTerm(p.getCVTerms().get(cv));
                }
            }

            if (!p.getSBOTermID().isEmpty()) {
                s.setSBOTerm(p.getSBOTermID());
            }

            index += 1;
        }


        // set the transitions as reactions with reactants and products
        for (int t = 0; t < qualTransitionsList.size(); t++) {
            Reaction r = convertedModel.createReaction();
            r.setId(qualTransitionsList.get(t));
            r.setReversible(false);

            if (!qualTransitionsListOne.get(t).getCVTerms().isEmpty()) {
                for (int cv = 0; cv < qualTransitionsListOne.get(t).getCVTerms().size(); cv++) {
                    r.addCVTerm(qualTransitionsListOne.get(t).getCVTerms().get(cv));
                }
            }

            if (!qualTransitionsListOne.get(t).getSBOTermID().isEmpty()) {
                r.setSBOTerm(qualTransitionsListOne.get(t).getSBOTermID());
            }

            for (int p = 0; p < qualInputSpeciesList.get(t).size(); p++) {
                SpeciesReference s = r.createReactant();
                s.setSpecies(qualInputSpeciesList.get(t).get(p));
                s.setStoichiometry(qualInputLevelList.get(t).get(p));
            }

            for (int p = 0; p < qualOutputSpeciesList.get(t).size(); p++) {
                SpeciesReference s = r.createProduct();
                s.setSpecies(qualOutputSpeciesList.get(t).get(p));
                s.setStoichiometry(qualOutputLevelList.get(t).get(p));
            }
        }

        return convertedDoc;

    }
}