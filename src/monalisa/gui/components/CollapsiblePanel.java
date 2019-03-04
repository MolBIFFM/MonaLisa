/*
 *
 *  This file ist part of the software MonaLisa.
 *  MonaLisa is free software, dependend on non-free software. For more information read LICENCE and README.
 *
 *  (c) Department of Molecular Bioinformatics, Institue of Computer Science, Johann Wolfgang
 *  Goethe-University Frankfurt am Main, Germany
 *
 */

package monalisa.gui.components;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.*;

public class CollapsiblePanel extends JPanel {
    private static final long serialVersionUID = 7667885216918700635L;

    private final CollapsiblePanelBorder border;

    private boolean collapsed;
    private Dimension expandedSize;
    private Dimension expandedMaximumSize;
    private Dimension expandedMinimumSize;
    private Dimension expandedPreferredSize;

    public CollapsiblePanel() {
        this("", CollapsiblePanelBorder.DEFAULTSHADE);
    }

    public CollapsiblePanel(String title) {
        this(title, CollapsiblePanelBorder.DEFAULTSHADE);
    }

    public CollapsiblePanel(Color titleShade) {
        this("", titleShade);
    }

    public CollapsiblePanel(String title, Color titleShade) {
        border = new CollapsiblePanelBorder(title, titleShade);
        setBorder(border);

        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getButton() == MouseEvent.BUTTON1) {
                    int titleHeight = border.getBorderInsets(CollapsiblePanel.this).top;
                    if (e.getPoint().y <= titleHeight)
                        setCollapsed(!isCollapsed());
                }
                super.mouseClicked(e);
            }
        });
    }

    public boolean isCollapsed() {
        return collapsed;
    }

    public void setCollapsed(boolean collapsed) {
        if (collapsed == this.collapsed)
            return;

        this.collapsed = collapsed;

        if (collapsed) {
            expandedSize = getSize();
            expandedMaximumSize = getMaximumSize();
            expandedMinimumSize = getMinimumSize();
            expandedPreferredSize = getPreferredSize();

            int collapsedHeight = border.getBorderInsets(this).top;
            setMaximumSize(new Dimension(getMaximumSize().width, collapsedHeight));
            setMinimumSize(new Dimension(getMinimumSize().width, collapsedHeight));
            setPreferredSize(new Dimension(getPreferredSize().width, collapsedHeight));
            setSize(getWidth(), collapsedHeight);
        }
        else {
            // Restore the panel's height.
            setSize(expandedSize);
            setPreferredSize(expandedPreferredSize);
            setMinimumSize(expandedMinimumSize);
            setMaximumSize(expandedMaximumSize);
        }

        if (getParent() instanceof JComponent)
            ((JComponent) getParent()).revalidate();
        else
            getParent().validate();
        border.setCollapsed(collapsed);
        repaint();
        firePropertyChange("collapsed", !collapsed, collapsed);
    }

    public String getTitle() {
        return border.getTitle();
    }

    public void setTitle(String title) {
        String oldTitle = getTitle();
        border.setTitle(title);
        repaint();
        firePropertyChange("title", oldTitle, title);
    }

    public String getComment() {
        return border.getComment();
    }

    public void setComment(String comment) {
        String oldComment = getComment();
        border.setComment(comment);
        repaint();
        firePropertyChange("comment", oldComment, comment);
    }

    public Color getTitleShade() {
        return border.getShade();
    }

    public void setTitleShade(Color titleShade) {
        border.setShade(titleShade);
        repaint();
    }
}
