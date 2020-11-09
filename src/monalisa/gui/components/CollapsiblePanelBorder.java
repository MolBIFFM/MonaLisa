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

import java.awt.*;
import java.awt.geom.RoundRectangle2D;

import javax.swing.border.*;

public class CollapsiblePanelBorder implements Border {

    //public static final Color DEFAULTSHADE = UIManager.getColor("Button.light");
    public static final Color DEFAULTSHADE = Color.gray;
    private static final int FONTSPACING = 2;

    private String title;
    private String comment;
    private Color shade;
    private boolean collapsed;
    private Color lightShade;

    public CollapsiblePanelBorder() {
        this("", DEFAULTSHADE);
    }

    public CollapsiblePanelBorder(Color shade) {
        this("", shade);
    }

    public CollapsiblePanelBorder(String title) {
        this(title, DEFAULTSHADE);
    }

    public CollapsiblePanelBorder(String title, Color shade) {
        setTitle(title);
        setShade(shade);
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public Color getShade() {
        return shade;
    }

    public void setShade(Color shade) {
        this.shade = shade;
        calculateLightShade();
    }

    public boolean isCollapsed() {
        return collapsed;
    }

    public void setCollapsed(boolean collapsed) {
        this.collapsed = collapsed;
    }

    private Color getLightShade() {
        return lightShade;
    }

    @Override
    public Insets getBorderInsets(Component c) {
        FontMetrics fm = c.getFontMetrics(c.getFont());
        int top = fm.getAscent() + fm.getDescent() + 2 * FONTSPACING;
        return new Insets(top, 0, 0, 0);
    }

    @Override
    public boolean isBorderOpaque() {
        return true;
    }

    @Override
    public void paintBorder(Component c, Graphics g, int x, int y, int width,
            int height) {
        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        Insets insets = getBorderInsets(c);

        Font font = c.getFont();
        FontMetrics fm = c.getFontMetrics(font);
        Color foreground = c.getForeground();

        final int cornerHeight = Math.min(16, insets.top);
        final int cornerRadius = cornerHeight / 2;

        // Take care of the upper spare place behind the rounded corners:
        // Fill it with the first opaque parent's background color.
        Component root = c;

        while (root.getParent() != null) {
            root = root.getParent();
            if (root.isOpaque()) {
                break;
            }
        }

        g.setColor(root.getBackground());
        g.fillRect(0, 0, c.getWidth(), insets.top);

        // Draw the rounded corners caption bar.
        g2d.setPaint(new GradientPaint(0, 0, getLightShade(), 0, insets.top, getShade()));
        RoundRectangle2D rect
                = new RoundRectangle2D.Float(0, 0, c.getWidth(), insets.top, cornerHeight, cornerHeight);
        g2d.fill(rect);

        if (!isCollapsed()) {
            g.fillRect(0, cornerRadius, c.getWidth(), insets.top - cornerRadius);
        }

        final int arrowWidth = 9;
        final int arrowLeft = 5;
        final int arrowBaseline = fm.getAscent() + FONTSPACING;
        g.setColor(new Color(foreground.getRed(), foreground.getGreen(), foreground.getBlue(), 192));
        g.fillPolygon(getArrow(arrowWidth, arrowLeft, arrowBaseline));
        g.setColor(foreground);
        g.setFont(font);
        int titleLeft = arrowLeft + arrowWidth + FONTSPACING;
        g.drawString(getTitle(), titleLeft, arrowBaseline);

        if (getComment() != null) {
            Font commentFont = new Font(font.getName(),
                    font.getStyle(), //| Font.ITALIC,
                    (int) (font.getSize() * 0.9));
            int commentLeft = titleLeft + (int) fm.getStringBounds(getTitle() + " ", g).getWidth();
            g.setFont(commentFont);
            String comment = "(" + getComment() + ")";
            g.drawString(comment, commentLeft, arrowBaseline);
        }
    }

    private void calculateLightShade() {
        lightShade = new Color(lighten(shade.getRed()), lighten(shade.getGreen()), lighten(shade.getBlue()));
    }

    private int lighten(int a) {
        return (a + 255) / 2;
    }

    private Polygon getArrow(int size, int left, int baseline) {
        Polygon arrow = new Polygon();

        // sqrt(3) ~ 1.73205081
        int medianLength = (int) Math.round(1.73205081 / 2 * size);

        if (isCollapsed()) {
            arrow.addPoint(0, 0);
            arrow.addPoint(0, size);
            arrow.addPoint(medianLength, size / 2);
        } else {
            arrow.addPoint(0, 0);
            arrow.addPoint(size, 0);
            arrow.addPoint(size / 2, medianLength);
        }

        arrow.translate(left, baseline - size);

        return arrow;
    }
}
