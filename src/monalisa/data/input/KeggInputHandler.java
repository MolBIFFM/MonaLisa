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

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.*;
import monalisa.addons.annotations.AnnotationUtils;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.jdom2.input.SAXBuilder;
import monalisa.data.pn.PetriNet;
import monalisa.data.pn.Place;
import monalisa.data.pn.Transition;
import monalisa.resources.ResourceManager;
import monalisa.util.FileUtils;
import org.sbml.jsbml.CVTerm;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

/**
 * Import KEGG networks in xml-format. http://www.kegg.jp/kegg/xml/docs/ There
 * are interaction (regulation) and reaction (metabolism) networks.
 *
 * entry: - id - name - type - reaction - graphics: - name - x, y, width, height
 * relation: - entry1 - entry2 - type - subtype: - name - value reaction: - name
 * - type - substrate: - name - product: - name
 *
 * @author Jens Einloft
 */
public class KeggInputHandler implements InputHandler {

    private HashMap<String, String> keggCompoundDefinitions;
    private int transitionCount, placeCount;
    private final Map<String, Transition> transitions = new HashMap<>();
    private final Map<String, Place> places = new HashMap<>();
    private static final Logger LOGGER = LogManager.getLogger(KeggInputHandler.class);

    @Override
    public boolean isKnownFile(File file) throws IOException {
        LOGGER.debug("Checking whether file is in KGML format");
        if (!"xml".equalsIgnoreCase(FileUtils.getExtension(file))) {
            return false;
        }
        try {
            SAXBuilder builder = new SAXBuilder();
            Document doc = builder.build(file);
            Element root = doc.getRootElement();
            String image = root.getAttributeValue("image");
            return image != null && image.contains("kegg");
        } catch (JDOMException ex) {
            LOGGER.debug("Caught JDOMException while checking for KGML format: ", ex);
            return false;
        }
    }

    @Override
    public PetriNet load(InputStream in, File file) throws IOException {
        LOGGER.info("Loading Petri net from KEGG file");
        PetriNet ret = new PetriNet();

        transitionCount = 0;
        placeCount = 0;
        transitions.clear();
        places.clear();

        try {
            keggCompoundDefinitions = readDefinitions(ResourceManager.instance().getResourceUrl("kegg/keggCompoundDefinition.txt"));
        } catch (IOException e) {
            LOGGER.error("Unable to read KEGG compound definition file", e);
            throw new IOException("Unable to read KEGG compound definition file", e);
        }

        SAXBuilder builder = new SAXBuilder();
        Document doc;
        try {
            doc = builder.build(in);
        } catch (JDOMException e) {
            LOGGER.error("Failed to parse the XML file", e);
            throw new IOException("Failed to parse the XML file.", e);
        }
        Element root = doc.getRootElement();

        ret.putProperty(AnnotationUtils.MODEL_NAME, root.getAttributeValue("title"));
        List<CVTerm> cvts = new ArrayList<>();
        CVTerm cvt = new CVTerm();
        cvt.setQualifierType(CVTerm.Type.MODEL_QUALIFIER);
        cvt.setModelQualifierType(CVTerm.Qualifier.BQM_IS);
        cvt.addResourceURI("http://identifiers.org/kegg.pathway/" + root.getAttributeValue("name").split(":")[1]);
        cvts.add(cvt);
        ret.putProperty(AnnotationUtils.MIRIAM_MODEL_QUALIFIERS, cvts);

        Map<String, Element> entryMap = new HashMap<>();
        for (Element element : root.getChildren("entry")) {
            entryMap.put(element.getAttributeValue("id"), element);
        }

        Transition t, t_rev = null;
        Place substrate, product;
        boolean reversible;
        String id;
        Element rev;
        for (Element reaction : root.getChildren("reaction")) {
            t = findTransition(entryMap.get(reaction.getAttributeValue("id")), ret);

            reversible = reaction.getAttributeValue("type").equals("reversible");

            if (reversible) {
                id = reaction.getAttributeValue("id");
                rev = entryMap.get(reaction.getAttributeValue("id"));
                rev.setAttribute("id", id + "_rev");
                t_rev = findTransition(rev, ret);
                t_rev.putProperty("name", ((String) t.getProperty("name")) + "_rev");
            }

            for (Element s : reaction.getChildren("substrate")) {
                substrate = findPlace(entryMap.get(s.getAttributeValue("id")), ret);
                ret.addArc(substrate, t);

                if (reversible) {
                    ret.addArc(t_rev, substrate);
                }

            }

            for (Element p : reaction.getChildren("product")) {
                product = findPlace(entryMap.get(p.getAttributeValue("id")), ret);
                ret.addArc(t, product);

                if (reversible) {
                    ret.addArc(product, t_rev);
                }
            }

        }

//        Transition map;
//        for(Element relation : root.getChildren("relation")) {
//            if(!relation.getAttributeValue("type").equals("ECrel")) {
//
//                if(relation.getAttributeValue("type").equals("maplink")) {
//                    boolean b = false;
//                    if(entryMap.get(relation.getAttributeValue("entry1")).getAttributeValue("type").equals("map")) {
//                        System.out.println("e1");
//                        map = findTransition(entryMap.get(relation.getAttributeValue("entry1")), ret);
//
//                        if(entryMap.get(relation.getAttributeValue("entry2")).getAttributeValue("type").equals("compound")) {
//                            System.out.println("\te1 compound");
//                        }
//                        else if(entryMap.get(relation.getChild("subtype").getAttributeValue("value")).getAttributeValue("type").equals("compound")) {
//                            System.out.println("\t subtype compound");
//
//                        }
//
//                    }
//                    else if(entryMap.get(relation.getAttributeValue("entry2")).getAttributeValue("type").equals("map")) {
//                        System.out.println("e2");
//                        map = findTransition(entryMap.get(relation.getAttributeValue("entry2")), ret);
//
//                        if(entryMap.get(relation.getAttributeValue("entry1")).getAttributeValue("type").equals("compound")) {
//                            System.out.println("\tcompound");
//                        }
//                        else if(entryMap.get(relation.getChild("subtype").getAttributeValue("value")).getAttributeValue("type").equals("compound")) {
//                            System.out.println("\t subtype compound");
//                        }
//                    }
//
////                    p1 = places.get(entryMap.get(relation.getAttributeValue("entry1")).getAttributeValue("id"));
////                    t1 = transitions.get(entryMap.get(relation.getAttributeValue("entry1")).getAttributeValue("id"));
////
////                    p2 = places.get(entryMap.get(relation.getAttributeValue("entry2")).getAttributeValue("id"));
////                    t2 = transitions.get(entryMap.get(relation.getAttributeValue("entry2")).getAttributeValue("id"));
//
//                }
//
//            }
//        }
        LOGGER.info("Probably successfully loaded Petri net from KEGG file");
        return ret;
    }

    private Transition findTransition(Element e, PetriNet pn) {
        String id = e.getAttributeValue("id");
        Transition t = transitions.get(id);

        if (t == null) {
            LOGGER.debug("Creating new transition with id '" + id + "' and name '" + e.getAttributeValue("name").replace(" ", "") + "'");
            t = new Transition(transitionCount++);
            transitions.put(id, t);

//            if(e.getChild("graphics") != null) {
//                if(e.getChild("graphics").getAttributeValue("name") != null) {
//                    t.putProperty("name", e.getChild("graphics").getAttributeValue("name").replace(" ", ""));
//                } else {
//                    t.putProperty("name", e.getAttributeValue("name").replace(" ", ""));
//                }
//            } else {
            t.putProperty("name", e.getAttributeValue("name").replace(" ", ""));
//            }

            if (e.getChild("graphics") != null) {
                if (e.getChild("graphics").getAttributeValue("x") != null) {
                    t.putProperty("posX", new Double(e.getChild("graphics").getAttributeValue("x")));
                    t.putProperty("posY", new Double(e.getChild("graphics").getAttributeValue("y")));
                }
            }

            t.putProperty(AnnotationUtils.MIRIAM_BIO_QUALIFIERS, getCVTerms(e));

            pn.addTransition(t);
        }
        LOGGER.debug("Successfully created new transition with id '" + id + "' and name '" + t.getProperty("name") + "'");
        return t;
    }

    private Place findPlace(Element e, PetriNet pn) {
        String id = e.getAttributeValue("id");
        Place p = places.get(id);

        if (p == null) {
            LOGGER.debug("Creating new place with id '" + id + "' and name '" + e.getAttributeValue("name").replace(" ", "_") + "'");
            p = new Place(placeCount++);
            places.put(id, p);

//            if(e.getChild("graphics") != null) {
//                if(e.getChild("graphics").getAttributeValue("name") != null) {
//                    p.putProperty("name", e.getChild("graphics").getAttributeValue("name").replace(" ", "_"));
//                } else {
//                    p.putProperty("name", e.getAttributeValue("name").replace(" ", "_"));
//                }
//            } else {
            p.putProperty("name", e.getAttributeValue("name").replace(" ", "_"));
//            }

            if (e.getChild("graphics") != null) {
                if (e.getChild("graphics").getAttributeValue("x") != null) {
                    p.putProperty("posX", new Double(e.getChild("graphics").getAttributeValue("x")));
                    p.putProperty("posY", new Double(e.getChild("graphics").getAttributeValue("y")));
                }
            }
            p.putProperty(AnnotationUtils.MIRIAM_BIO_QUALIFIERS, getCVTerms(e));

            pn.addPlace(p);
        }
        LOGGER.debug("Successfully created new place with id '" + id + "' and name '" + p.getProperty("name") + "'");
        return p;
    }

    private List<CVTerm> getCVTerms(Element e) {
        LOGGER.debug("Getting CV terms for element '" + e.getAttributeValue("name") + "'");
        List<CVTerm> cvts = new ArrayList<>();

        if (e.getAttributeValue("type").equals("gene")) {
            for (String s : e.getAttributeValue("name").split(" ")) {
                CVTerm cvt = new CVTerm();
                cvt.setQualifierType(CVTerm.Type.BIOLOGICAL_QUALIFIER);
                cvt.setBiologicalQualifierType(CVTerm.Qualifier.BQB_IS);
                cvt.addResourceURI("http://identifiers.org/kegg.genes/" + s);
                cvts.add(cvt);
            }
        } else if (e.getAttributeValue("type").equals("ortholog")) {
            for (String s : e.getAttributeValue("name").split(" ")) {
                CVTerm cvt = new CVTerm();
                cvt.setQualifierType(CVTerm.Type.BIOLOGICAL_QUALIFIER);
                cvt.setBiologicalQualifierType(CVTerm.Qualifier.BQB_IS);
                cvt.addResourceURI("http://identifiers.org/kegg.orthology/" + s.split(":")[1]);
                cvts.add(cvt);
            }
        } else if (e.getAttributeValue("type").equals("enzyme") || e.getAttributeValue("type").equals("reaction")) {
            for (String s : e.getAttributeValue("name").split(" ")) {
                CVTerm cvt = new CVTerm();
                cvt.setQualifierType(CVTerm.Type.BIOLOGICAL_QUALIFIER);
                cvt.setBiologicalQualifierType(CVTerm.Qualifier.BQB_IS);
                cvt.addResourceURI("http://identifiers.org/kegg.reaction/" + s.split(":")[1]);
                cvts.add(cvt);
            }
        } else if (e.getAttributeValue("type").equals("compound")) {
            for (String s : e.getAttributeValue("name").split(" ")) {
                CVTerm cvt = new CVTerm();
                cvt.setQualifierType(CVTerm.Type.BIOLOGICAL_QUALIFIER);
                cvt.setBiologicalQualifierType(CVTerm.Qualifier.BQB_IS);
                cvt.addResourceURI("http://identifiers.org/kegg.compound/" + s.split(":")[1]);
                cvts.add(cvt);
            }
        } else if (e.getAttributeValue("type").equals("map")) {
            for (String s : e.getAttributeValue("name").split(" ")) {
                CVTerm cvt = new CVTerm();
                cvt.setQualifierType(CVTerm.Type.BIOLOGICAL_QUALIFIER);
                cvt.setBiologicalQualifierType(CVTerm.Qualifier.BQB_IS);
                cvt.addResourceURI("http://identifiers.org/kegg.pathway/" + s.split(":")[1]);
                cvts.add(cvt);
            }
        }
        if (e.getAttributeValue("reaction") != null) {
            for (String s : e.getAttributeValue("reaction").split(" ")) {
                CVTerm cvt = new CVTerm();
                cvt.setQualifierType(CVTerm.Type.BIOLOGICAL_QUALIFIER);
                cvt.setBiologicalQualifierType(CVTerm.Qualifier.BQB_IS_VERSION_OF);
                cvt.addResourceURI("http://identifiers.org/kegg.reaction/" + s.split(":")[1]);
                cvts.add(cvt);
            }
        }
        LOGGER.debug("Successfully got CV terms for element '" + e.getAttributeValue("name") + "'");
        return cvts;
    }

    private HashMap<String, String> readDefinitions(URL resource) throws IOException {
        LOGGER.debug("Reading definitions for KEGG compounds");
        HashMap<String, String> ret = new HashMap<>();
        InputStream istream = resource.openStream();
        try (DataInputStream in = new DataInputStream(istream)) {
            BufferedReader br = new BufferedReader(new InputStreamReader(in));
            String line;
            StringTokenizer st;
            while ((line = br.readLine()) != null) {
                st = new StringTokenizer(line);
                String name = st.nextToken();
                String definition = "";
                while (st.hasMoreTokens()) {
                    definition = definition.concat(st.nextToken() + " ");
                }
                String[] split = definition.split(";");
                ret.put(name, split[0].trim());
            }
        }
        LOGGER.debug("Successfully read definitions for KEGG compounds");
        return ret;
    }

    @Override
    public String getDescription() {
        return "KEGG (KGML)";
    }
}
