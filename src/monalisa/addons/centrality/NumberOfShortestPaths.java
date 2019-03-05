/*
 *
 *  This file is part of the software MonaLisa.
 *  MonaLisa is free software, dependent on non-free software. For more information read LICENCE and README.
 *
 *  (c) Department of Molecular Bioinformatics, Institute of Computer Science, Johann Wolfgang
 *  Goethe-University Frankfurt am Main, Germany
 *
 */

package monalisa.addons.centrality;

import java.util.HashMap;
import java.util.Map;

/**
 * This class computes for every node the number of shortest paths which pass through it
 * and stores the number of shortest paths for each node combination in a matrix
 * @author Lilya Mirzoyan
 */
public class NumberOfShortestPaths {
    
    private final AdjacencyMatrix adjMatrix;
    
    private final int[][] splMatrix;
    private int[][] splNumberMatrix;
    private final Map<Integer, Integer> numberOfSplMap;
    
    /**
     * Computes the matrix of the shortest-paths-number. Based on this matrix,
     * the number of shortest paths is computed, which pass through a certain node. 
     * This is done for every node
     * @param adjMatrix
     * @param floydWarshall
     */
    public NumberOfShortestPaths(AdjacencyMatrix adjMatrix, FloydWarshall floydWarshall){
        this.adjMatrix = adjMatrix;
                       
        int n = adjMatrix.getLength();
        
        initSplNumberMatrix(n);
        splMatrix = floydWarshall.getDistMatrix();
        numberOfSplMap = new HashMap<>();
               
        for (int v = 0; v < n; v++){
            for (int s = 0; s < n; s++){
                if (splMatrix[s][v] > 0 && s!=v){
                    for (int t = 0; t < n; t++){
                        if (splMatrix[v][t] > 0 && t!=v && s!=t){
                            // if a shortest path passes through node v, then every
                            // combination of s,v and v,t is a shortest path
                            // between s and t.
                            if (splMatrix[s][t] == (splMatrix[s][v] + splMatrix[v][t])){
                                splNumberMatrix[s][t] =  splNumberMatrix[s][t] + (splNumberMatrix[s][v] *  splNumberMatrix[v][t]);
                            }
                                                       
                        }
                    }
                }
            }
       }
        
        for (int v = 0; v < n; v++){
            int counter = 0;
            for (int s = 0; s < n; s++){
                for (int t = 0; t < n; t++){
                    if (s!=t && s!=v && t!=v){
                        // if the shortest path length between node s and node t 
                        // equals to the shortest path length, passing through v, 
                        // then node v lies on a shortest path between s and t.
                        if (splMatrix[s][t] == splMatrix[s][v] + splMatrix[v][t]){
                            counter += splNumberMatrix[s][v] * splNumberMatrix[v][t];
                        }
                    }
                }
            }
           // the number of shortest paths, passing through v is stored in a map
           numberOfSplMap.put(v, counter);
        }
     }
    
    
    /**
     * Initializes the shortest-paths-number matrix using the values of the
     * adjacency matrix
     * @param size 
     */
    public void initSplNumberMatrix(int size){
        splNumberMatrix = new int[size][size];
        for (int i = 0; i < size; i++) {
            for (int j = 0; j < size; j++){
                if(adjMatrix.getMatrix()[i][j] == 1){
                splNumberMatrix[i][j] = 1;
                }
                else if(adjMatrix.getMatrix()[i][j] == 0){
                splNumberMatrix[i][j] = 0;
                }
            }
            
        }
    }
    
    /**
     * Yields the number of shortest paths for each combination of nodes in a matrix
     * @return shortest-path-length-number matrix
     */
    public int[][] getSplNumberMatrix(){

        return this.splNumberMatrix;
    }
    
   
    /**
     * Returns the number of shortest paths for a given node
     * @param node
     * @return number of shortest paths for a node
     */
    public int getCounter(int node){
         int n = this.numberOfSplMap.size();
         return numberOfSplMap.get(node);
    }
    
    /**
     * Counts up the number of shortest paths for all nodes
     * @return sum of all shortest paths
     */
    public int getAllShortestPaths(){
      int n = this.numberOfSplMap.size(); 
      int allShortestPaths = 0;
      for (int v = 0; v < n; v++){
          allShortestPaths += numberOfSplMap.get(v);
      }
      return allShortestPaths;
    }
}
