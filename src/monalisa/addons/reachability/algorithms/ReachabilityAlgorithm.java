/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package monalisa.addons.reachability.algorithms;

import java.util.ArrayList;
import monalisa.addons.reachability.ReachabilityListener;
import monalisa.addons.reachability.ReachabilityNode;
import monalisa.data.pn.Transition;

/**
 *
 * @author Marcel Gehrmann
 */
public interface ReachabilityAlgorithm {

    public void run() throws InterruptedException;
    
    //public void runExplicit() throws InterruptedException;

    public void computePriority(ReachabilityNode node);

    public ArrayList<Transition> backtrack();

    public void addListener(ReachabilityListener listener);

    public void removeListener(ReachabilityListener listener);

}
