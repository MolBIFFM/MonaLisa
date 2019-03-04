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
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;

import javax.swing.Icon;
import javax.swing.JPanel;

import monalisa.resources.ResourceManager;

public class SplashScreen extends JPanel {
    private static final long serialVersionUID = 1L;
    private Icon splash;
    
    public SplashScreen() {
        initComponents();
    }
    
    private void initComponents() {
        splash = ResourceManager.instance().getIcon("splash.png");
    }
    
    @Override
    protected void paintComponent(Graphics g) {
        final Color shade = getBackground();
        Graphics2D g2d = (Graphics2D) g;
        final int w = getWidth();
        final int h = getHeight();

        g2d.setPaint(new GradientPaint(0, 0, shade, 0, h, shade.darker()));
        g2d.fill(new Rectangle(w, h));
        
        int x = getWidth() / 2 - splash.getIconWidth() / 2;
        int y = getHeight() / 2 - splash.getIconHeight() / 2;
        splash.paintIcon(this, g, x, y);
    }
}
