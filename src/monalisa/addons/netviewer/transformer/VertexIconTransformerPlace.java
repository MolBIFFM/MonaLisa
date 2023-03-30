/*
 *
 *  This file is part of the software MonaLisa.
 *  MonaLisa is free software, dependent on non-free software. For more information read LICENCE and README.
 *
 *  (c) Department of Molecular Bioinformatics, Institute of Computer Science, Johann Wolfgang
 *  Goethe-University Frankfurt am Main, Germany
 *
 */
package monalisa.addons.netviewer.transformer;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.font.FontRenderContext;
import java.awt.font.TextLayout;
import java.awt.image.BufferedImage;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import monalisa.addons.netviewer.NetViewer;
import monalisa.addons.netviewer.NetViewerNode;
import monalisa.data.pn.Transition;
import org.apache.commons.collections15.Transformer;

/**
 * Renderer for the icon of a vertex
 *
 * @author Pavel Balazki.
 */
public class VertexIconTransformerPlace implements Transformer<NetViewerNode, Icon> {

    //BEGIN VARIABLES DECLARATION
    private int vertexSize;
    private int tokenSize;
    //END VARIABLES DECLARATION

    //BEGIN CONSTRUCTORS
    /**
     * Create new VertexIconTransformer.
     *
     * @param vSize
     */
    public VertexIconTransformerPlace(int vSize) {
        this.vertexSize = vSize;
        this.tokenSize = vertexSize / 4;
    }
    //END CONSTRUCTORS

    /**
     * Change the size of the vertices.
     *
     * @param vertexSizeN New size of vertices.
     */
    public void setVertexSize(int vertexSizeN) {
        this.vertexSize = vertexSizeN;
        this.tokenSize = this.vertexSize / 4;
    }

    /**
     * Get the size of vertices.
     *
     * @return
     */
    public int getTokenSize() {
        return tokenSize;
    }

    @Override
    public Icon transform(NetViewerNode n) {
        int id = n.getMasterNode().getId();

        /*
         * If the node is a transition return black square
         */
        if (n.getNodeType().equalsIgnoreCase(NetViewer.TRANSITION)) {
            BufferedImage image = new BufferedImage(this.vertexSize, this.vertexSize, BufferedImage.TYPE_INT_ARGB);
            for (int col = 0; col < this.vertexSize; col++) {
                for (int row = 0; row < this.vertexSize; row++) {
                    image.setRGB(col, row, 0x0);
                }
            }
            Graphics2D graphics = (Graphics2D) image.getGraphics();
            graphics.setColor(n.getColor());
            graphics.fillRect(0, 0, this.vertexSize - 1, this.vertexSize - 1);
            return new ImageIcon(image);
        }

        /*
         * If the node is a place, return a cirlce with the given number of tokens in it
         */
        if (n.getNodeType().equalsIgnoreCase(NetViewer.PLACE)) {
            BufferedImage image = new BufferedImage(vertexSize, vertexSize, BufferedImage.TYPE_INT_ARGB);
            for (int col = 0; col < vertexSize; col++) {
                for (int row = 0; row < vertexSize; row++) {
                    image.setRGB(col, row, 0x0);
                }
            }
            Graphics2D graphics = (Graphics2D) image.getGraphics();
            graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                    RenderingHints.VALUE_ANTIALIAS_ON);
            //decide whether the place is a masterNode, is logical or just a regular place
            if (n.isMasterNode()) {
                graphics.setColor(Color.GRAY);
            } else if (n.isLogical()) {
                graphics.setColor(Color.LIGHT_GRAY);
            } else {
                graphics.setColor(n.getColor());
            }
            graphics.fillOval(0, 0, vertexSize - 1, vertexSize - 1);  // draw outline of the icon
            graphics.setColor(n.getStrokeColor());
            graphics.drawOval(0, 0, vertexSize - 1, vertexSize - 1);

            graphics.setColor(Color.black);
            long tokenCount = n.getTokens();
            switch ((int) tokenCount) {
                case 0:
                    break;
                case 1:
                    graphics.fillOval(vertexSize / 2 - tokenSize / 2, vertexSize / 2 - tokenSize / 2, tokenSize, tokenSize);
                    break;
                case 2:
                    graphics.fillOval(vertexSize / 4 - tokenSize / 2, vertexSize / 2 - tokenSize / 2, tokenSize, tokenSize);
                    graphics.fillOval(vertexSize - vertexSize / 4 - tokenSize / 2, vertexSize / 2 - tokenSize / 2, tokenSize, tokenSize);
                    break;
                case 3:
                    graphics.fillOval(vertexSize / 2 - tokenSize / 2, vertexSize / 4 - tokenSize / 2, tokenSize, tokenSize);
                    graphics.fillOval(vertexSize / 4 - tokenSize / 2 + 2, vertexSize - vertexSize / 4 - tokenSize / 2 - 2, tokenSize, tokenSize);
                    graphics.fillOval(vertexSize - vertexSize / 4 - tokenSize / 2 - 2, vertexSize - vertexSize / 4 - tokenSize / 2 - 2, tokenSize, tokenSize);
                    break;
                case 4:
                    graphics.fillOval(vertexSize / 4 - tokenSize / 2 + 1, vertexSize / 4 - tokenSize / 2 + 1, tokenSize, tokenSize);
                    graphics.fillOval(vertexSize - vertexSize / 4 - tokenSize / 2 - 1, vertexSize / 4 - tokenSize / 2 + 1, tokenSize, tokenSize);
                    graphics.fillOval(vertexSize / 4 - tokenSize / 2 + 1, vertexSize - vertexSize / 4 - tokenSize / 2 - 1, tokenSize, tokenSize);
                    graphics.fillOval(vertexSize - vertexSize / 4 - tokenSize / 2 - 1, vertexSize - vertexSize / 4 - tokenSize / 2 - 1, tokenSize, tokenSize);
                    break;
                default:
                    graphics.setColor(Color.BLACK);
                    String tokenS = String.valueOf(tokenCount);
                    int l = tokenS.length();

                    graphics.setFont(new Font("Arial", Font.BOLD, (vertexSize) / l - 5 / l));
                    FontRenderContext frc = graphics.getFontRenderContext();
                    TextLayout mLayout = new TextLayout("" + tokenS, graphics.getFont(), frc);

                    float x = (float) (-.5 + (vertexSize - mLayout.getBounds().getWidth()) / 2);
                    float y = vertexSize - (float) ((vertexSize - mLayout.getBounds().getHeight()) / 2);

                    graphics.drawString("" + tokenS, x, y);
            }
            return new ImageIcon(image);
        }
        return new ImageIcon();
    }
}
