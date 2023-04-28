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
import org.sbml.jsbml.ext.qual.QualitativeSpecies;
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


        List<String> qual_transitions_list = new ArrayList<>();
        List<List <Integer>> qual_input_level_list = new ArrayList<>();
        List<List <String>> qual_input_species_list = new ArrayList<>();
        List<List <Integer>> qual_output_level_list = new ArrayList<>();
        List<List <String>> qual_output_species_list = new ArrayList<>();
        List<List <Input>> qual_inputs_list = new ArrayList<>();
        List<List <Output>> qual_outputs_list = new ArrayList<>();
        List<Input> qual_input = null;


        // get the list of qualitative species and transitions
        List<QualitativeSpecies> qual_species_list = qual_ext.getListOfQualitativeSpecies();
        List<Transition> qual_transitions_list_one = qual_ext.getListOfTransitions();


        // get the list of transition ID' as strings
        for (int i = 0; i < qual_transitions_list_one.size(); i++) {
            Transition t = qual_transitions_list_one.get(i);
            String transition_id = t.getId();
            qual_transitions_list.add(transition_id);
        }


        // get the input species and input edge weight of the transitions
        for (int i = 0; i < qual_transitions_list.size(); i++) {
            List<Integer> input_level = new ArrayList<>();
            List<String> input_species = new ArrayList<>();
            Transition qual_transition = qual_ext.getTransition(i);
            qual_input = qual_transition.getListOfInputs();
            qual_inputs_list.add(qual_input);

            for (int j = 0; j < qual_input.size(); j++) {
                Integer qual_input_level = null;
                if (qual_input.get(j).isSetThresholdLevel() == true) {
                    qual_input_level = qual_input.get(j).getThresholdLevel();
                }
                else {
                    qual_input_level = 1;
                }

                String qual_input_species = qual_input.get(j).getQualitativeSpecies();
                input_level.add(qual_input_level);
                input_species.add(qual_input_species);
            }
            qual_input_level_list.add(input_level);
            qual_input_species_list.add(input_species);
        }


        // get the output species and output edge weight of the transitions
        for (int i = 0; i < qual_transitions_list.size(); i++) {
            List<Integer> output_level = new ArrayList<>();
            List<String> output_species = new ArrayList<>();
            Transition qual_transition = qual_ext.getTransition(i);
            List<Output> qual_output = qual_transition.getListOfOutputs();
            qual_outputs_list.add(qual_output);

            for (int j = 0; j < qual_output.size(); j++) {
                Integer qual_output_level = null;
                if (qual_output.get(j).isSetOutputLevel() == true) {
                    qual_output_level = qual_output.get(j).getOutputLevel();
                }
                else {
                    qual_output_level = 1;
                }

                output_level.add(qual_output_level);
                String qual_output_species = qual_output.get(j).getQualitativeSpecies();
                output_species.add(qual_output_species);
            }

            qual_output_level_list.add(output_level);
            qual_output_species_list.add(output_species);
        }


        // get the initial token amount
        List<Integer> initial_level = new ArrayList<>();
        for (QualitativeSpecies species : qual_species_list) {
            Integer qual_initial_level = null;
            if (species.isSetInitialLevel() == true) {
                qual_initial_level = species.getInitialLevel();
            }
            else {
                qual_initial_level = 0;
            }
            initial_level.add(qual_initial_level);
        }


        // create a new sbml document to put in the sbmlqual document information
        SBMLDocument converted_doc = new SBMLDocument(document.getLevel(), document.getVersion());
        Model converted_model = converted_doc.createModel("petri_net");


        // set the model ID
        if (model.isSetId() == true) {
            converted_model.setId(model.getId());
        }


        // set the namespaces (except for qual)
        Map<String,String> namespaces = document.getDeclaredNamespaces();
        for (Map.Entry<String,String> entry : namespaces.entrySet()) {
            String prefix = entry.getKey();
            String uri = entry.getValue();
            if (!prefix.contains("qual")) {
                converted_doc.addDeclaredNamespace(prefix, uri);
            }
        }


        // set the qualitative species as species
        List<QualitativeSpecies> places = qual_species_list;
        int index = 0;
        for (QualitativeSpecies p : places) {
            Species s = converted_model.createSpecies();
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
        for (int t = 0; t < qual_transitions_list.size(); t++) {
            Reaction r = converted_model.createReaction();
            r.setId(qual_transitions_list.get(t));
            r.setReversible(false);

            if (!qual_transitions_list_one.get(t).getCVTerms().isEmpty()) {
                for (int cv = 0; cv < qual_transitions_list_one.get(t).getCVTerms().size(); cv++) {
                    r.addCVTerm(qual_transitions_list_one.get(t).getCVTerms().get(cv));
                }
            }

            if (!qual_transitions_list_one.get(t).getSBOTermID().isEmpty()) {
                r.setSBOTerm(qual_transitions_list_one.get(t).getSBOTermID());
            }

            for (int p = 0; p < qual_input_species_list.get(t).size(); p++) {
                SpeciesReference s = r.createReactant();
                s.setSpecies(qual_input_species_list.get(t).get(p));
                s.setStoichiometry(qual_input_level_list.get(t).get(p));
            }

            for (int p = 0; p < qual_output_species_list.get(t).size(); p++) {
                SpeciesReference s = r.createProduct();
                s.setSpecies(qual_output_species_list.get(t).get(p));
                s.setStoichiometry(qual_output_level_list.get(t).get(p));
            }
        }


        return converted_doc;

    }
}