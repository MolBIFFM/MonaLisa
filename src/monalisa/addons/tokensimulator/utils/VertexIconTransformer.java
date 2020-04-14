/*
 *
 *  This file is part of the software MonaLisa.
 *  MonaLisa is free software, dependent on non-free software. For more information read LICENCE and README.
 *
 *  (c) Department of Molecular Bioinformatics, Institute of Computer Science, Johann Wolfgang
 *  Goethe-University Frankfurt am Main, Germany
 *
 */
package monalisa.addons.tokensimulator.utils;

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
import monalisa.addons.tokensimulator.TokenSimulator;
import monalisa.data.pn.Transition;
import org.apache.commons.collections15.Transformer;

/**
 * Renderer for the icon of a vertex
 *
 * @author Pavel Balazki.
 */
public class VertexIconTransformer implements Transformer<NetViewerNode, Icon> {

    //BEGIN VARIABLES DECLARATION
    private int vertexSize;
    private int tokenSize;
    private final TokenSimulator ts;
    //END VARIABLES DECLARATION

    //BEGIN CONSTRUCTORS
    /**
     * Create new VertexIconTransformer.
     *
     * @param tsN
     */
    public VertexIconTransformer(TokenSimulator tsN) {
        this.vertexSize = tsN.getVertexSize();
        this.tokenSize = vertexSize / 4;
        this.ts = tsN;
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
         * If the node is a transition and inactive, return black square
         * If the node is a transition and active, return black square with green border
         * If the node is a transition, is active and fired at last step, return green square with red border
         */
        if (n.getNodeType().equalsIgnoreCase(NetViewer.TRANSITION)) {
            BufferedImage image = new BufferedImage(this.vertexSize, this.vertexSize, BufferedImage.TYPE_INT_ARGB);
            for (int col = 0; col < this.vertexSize; col++) {
                for (int row = 0; row < this.vertexSize; row++) {
                    image.setRGB(col, row, 0x0);
                }
            }
            Graphics2D graphics = (Graphics2D) image.getGraphics();
            graphics.setColor(Color.BLACK);
            graphics.fillRect(0, 0, this.vertexSize - 1, this.vertexSize - 1);

            //if transition is active
            if (this.ts.getActiveTransitions().contains(ts.getPetriNet().findTransition(id))) {
                graphics.setColor(Color.GREEN);
                graphics.fillRect(0, 0, this.vertexSize - 1, this.vertexSize - 1);
                graphics.setColor(Color.BLACK);
                int offset = new Double(this.vertexSize * 0.2).intValue();
                graphics.fillRect(offset, offset, this.vertexSize - 1 - offset * 2, this.vertexSize - 1 - offset * 2);
            }

            //if the transition was fired in last step, return black square with red border
            int lastFired = this.ts.lastHistoryStep;
            if (lastFired >= 0) {
                for (Transition t : this.ts.historyArrayList.get(lastFired)) {
                    if (id == t.id()) {
                        graphics.setColor(Color.RED);
                        graphics.fillRect(0, 0, this.vertexSize - 1, this.vertexSize - 1);
                        graphics.setColor(Color.BLACK);
                        if (ts.getActiveTransitions().contains(this.ts.getPetriNet().findTransition(id))) {
                            graphics.setColor(Color.GREEN);
                        }
                        int offset = (int) Math.round(this.vertexSize * 0.2);
                        graphics.fillRect(offset, offset, this.vertexSize - 1 - offset * 2, this.vertexSize - 1 - offset * 2);

                        break;
                    }
                }
            }
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
                graphics.setColor(Color.WHITE);
            }
            graphics.fillOval(0, 0, vertexSize - 1, vertexSize - 1);  // draw outline of the icon
            graphics.setColor(Color.black);
            graphics.drawOval(0, 0, vertexSize - 1, vertexSize - 1);

            graphics.setColor(Color.RED);
            long tokenCount = ts.getTokenSim().getTokens(id);
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
