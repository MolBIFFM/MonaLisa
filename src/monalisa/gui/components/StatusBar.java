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

import javax.swing.*;

public class StatusBar extends JPanel {
    private static final long serialVersionUID = 5856619714640166163L;
    private final JLabel statusLabel;
    private final JProgressBar progressBar;
    
    public StatusBar() {
        setLayout(new BoxLayout(this, BoxLayout.LINE_AXIS));
        setBorder(BorderFactory.createEmptyBorder(3, 5, 3, 15));
        
        statusLabel = new JLabel(" ");
        
        progressBar = new JProgressBar(JProgressBar.HORIZONTAL);
        progressBar.setVisible(false);
        
        add(statusLabel);
        add(Box.createRigidArea(new Dimension(5, 0)));
        add(progressBar);
    }
    
    public String getStatusText() {
        return statusLabel.getText();
    }
    
    public void setStatusText(String statusText) {
        statusLabel.setText(statusText);
    }
    
    public boolean isProgressVisible() {
        return progressBar.isVisible();
    }
    
    public void setProgressVisible(boolean value) {
        progressBar.setVisible(value);
    }
    
    public int getProgressValue() {
        return progressBar.getValue();
    }
    
    public void setProgressValue(int value) {
        progressBar.setValue(value);
    }
    
    public boolean hasIndeterminateProgress() {
        return progressBar.isIndeterminate();
    }
    
    public void setIndeterminateProgress(boolean value) {
        progressBar.setIndeterminate(value);
    }
    
    @Override
    protected void paintComponent(Graphics g) {
        final Color shade = getBackground();
        Graphics2D g2d = (Graphics2D) g;
        final int w = getWidth();
        final int h = getHeight();

        g2d.setPaint(new GradientPaint(0, 0, shade, 0, h, shade.darker()));
        g2d.fill(new Rectangle(w, h));
    }
}
