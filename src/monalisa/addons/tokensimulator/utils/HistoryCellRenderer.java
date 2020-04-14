/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package monalisa.addons.tokensimulator.utils;

import java.awt.Color;
import java.awt.Component;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.ListCellRenderer;
import monalisa.addons.tokensimulator.TokenSimulator;

/**
 * Handles the coloring of entries in historyList. The last performed step
 * has red background.
 */
public class HistoryCellRenderer implements ListCellRenderer {

    private final TokenSimulator ts;

    public HistoryCellRenderer(TokenSimulator ts) {
        this.ts = ts;
    }

    @Override
    public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
        DefaultListCellRenderer defaultRenderer = new DefaultListCellRenderer();
        JLabel renderer = (JLabel) defaultRenderer.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
        if (index == ts.lastHistoryStep) {
            renderer.setBackground(Color.red);
        } else {
            renderer.setBackground(Color.white);
        }
        return renderer;
    }
}