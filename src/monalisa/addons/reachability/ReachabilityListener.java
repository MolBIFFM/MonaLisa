/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package monalisa.addons.reachability;

import java.util.EventListener;

/**
 *
 * @author Marcel Gehrmann
 */
public interface ReachabilityListener extends EventListener {

    public void update(ReachabilityEvent e);
}
