/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package monalisa.addons.reachability;

import monalisa.addons.reachability.algorithms.ReachabilityAlgorithm;

/**
 *
 * @author Marcel
 */
public class AlgorithmRunner extends Thread {

    private final ReachabilityAlgorithm algorithm;

    public AlgorithmRunner(ReachabilityAlgorithm algorithm) {
        this.algorithm = algorithm;
    }

    
   /** public void runExplicit(){
        try {
            algorithm.runExplicit();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    
    }*/
    
    @Override
    public void run() {
        try {
            algorithm.run();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
