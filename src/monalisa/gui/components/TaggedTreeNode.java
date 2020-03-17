/*
 *
 *  This file is part of the software MonaLisa.
 *  MonaLisa is free software, dependent on non-free software. For more information read LICENCE and README.
 *
 *  (c) Department of Molecular Bioinformatics, Institute of Computer Science, Johann Wolfgang
 *  Goethe-University Frankfurt am Main, Germany
 *
 */

package monalisa.gui.components;

import javax.swing.tree.DefaultMutableTreeNode;

public class TaggedTreeNode extends DefaultMutableTreeNode {
    private static final long serialVersionUID = -3349411122033846082L;
    private String displayTag;

    public TaggedTreeNode() {
        this(null, null);
    }

    public TaggedTreeNode(String displayTag) {
        this(null, displayTag);
    }
    
    public TaggedTreeNode(Object userObject) {
        this(userObject, userObject.toString());
    }

    public TaggedTreeNode(Object userObject, String displayTag) {
        super(userObject);
        setDisplayTag(displayTag);
    }

    public String getDisplayTag() {
        return displayTag;
    }

    public void setDisplayTag(String displayTag) {
        this.displayTag = displayTag;
    }

    @Override
    public String toString() {
        return displayTag == null || displayTag.equals("") ? super.toString()
                : displayTag;
    }
}
