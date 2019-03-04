/*
 *
 *  This file ist part of the software MonaLisa.
 *  MonaLisa is free software, dependend on non-free software. For more information read LICENCE and README.
 *
 *  (c) Department of Molecular Bioinformatics, Institue of Computer Science, Johann Wolfgang
 *  Goethe-University Frankfurt am Main, Germany
 *
 */

package monalisa.addons.centrality;

/**
 * This class is used to compute the shortest-path-lengths-matrix for a given adjacency matrix using the Floyd-Warshall algorithm
 * @author Lilya Mirzoyan
 */
public class FloydWarshall {
    
    private final AdjacencyMatrix adjMatrix;
    private final int[][] splMatrix;
       
    /**
     * Goes for all nodes and looks, if there exist a shorter path. If there is no path between two nodes, the value is set to zero
     * @param adjMatrix 
     */
    public FloydWarshall(AdjacencyMatrix adjMatrix){
        this.adjMatrix = adjMatrix;
        
        splMatrix = adjMatrix.getMatrix();
        int n = adjMatrix.getLength();
        
        for (int i = 0; i < n; i++){
            for (int j = 0; j < n; j++){
                if (splMatrix[j][i] > 0){
                for (int k = 0; k < n; k++){
                   if (splMatrix[i][k] > 0){
                   if ((splMatrix[j][k] > (splMatrix[j][i] + splMatrix[i][k])) || ((splMatrix[j][k] == 0) && (j!=k))){
                        splMatrix[j][k] = splMatrix[j][i] + splMatrix[i][k];
                   }
                  }
                }
              }        
           }
        }
    }  
    
    /**
     * Returns all shortes-path-lengths in a matrix
     * @return distance matrix
     */
    public int[][] getDistMatrix(){

        return this.splMatrix;
    }
  
}

