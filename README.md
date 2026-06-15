## Experimental Environment Setup

All experiments in this work are conducted within the MonaLisa SPN analysis framework.

### Running the Application

A pre-built executable version of MonaLisa is provided in the `dist/` directory.

The application can be started in one of the following ways:

- Double-click `MonaLisa.jar` 
- Command line execution (If jar file is not linked to Java)

```bash
java -jar path/to/MonaLisa.jar
```

**note**

Ensure that the `lib/` folder remains in the same directory as `MonaLisa.jar`, as it contains required dependencies.

### Project Directory Structure

The MonaLisa repository is structured as follows:
```
(parent directory)
MonaLisa/                (GitHub repository)
├──dist/
│   ├── MonaLisa.jar     (executable application)        
│   ├── lib/             (required dependencies)
│   ├── output/          (runtime-generated, NOT included in repository)
│
├──experiments_setup/    (models, parameters, and experiment scripts)
│   ├──analysis_scripts/ 
│   ├── cyclic_model.xml
│   ├── acyclic_model.xml
│   ├── parameters.xml
│
├── src/                 (modified MonaLisa source code)
├── ...
```

### Accessing the Reachability Analysis Interface

To access the reachability analysis environment, the following steps are required:

1. Run the executable JAR file `MonaLisa.jar` from the `dist/` directory.
2. Import the model file from the `experiments_setup/` folder.
3. Show Petri net and navigate to the "Analysis".
4. Select "Place Invariants" and click "Compute Invariants".
5. Click "Reachability" to open the reachability analysis interface.
6. In the "Reachability" interface, click "Stochastic PN Setting" to import the parameter file from `experiments_setup/`.

All subsequent experiments and algorithm executions are performed within the "Reachability" interface.

Within the "Reachability" interface:

- Initial marking and target marking can be configured directly.
<!-- - Stochastic reaction constants must be configured in the "Stochastic PN Setting" panel. -->

Within the "Stochastic PN Setting" interface:

- import the parameter file (parameters.xml)
- configure stochastic reaction constants
- configure place capacities
- configure target marking

This study uses two Petri net models and one parameter file:
- acyclic_model.xml
- cyclic_model.xml
- parameters.xml  
<!-- All files are provided in the "experiments_setup" folder of the repository. -->

## Figure 6: State Space Analysis

Figure 6 is generated using both the acyclic and cyclic Petri net models. All experiments use the parameter file `parameters.xml`.

After importing the parameter file, a set of stochastic analysis functions becomes available in the reachability interface, including:
- "Stochastic Reachability Graph" (direct execution button)
- "Stochastic Reachability Path" (direct execution button)
- "Stochastic Dijkstra" (select + "Compute selected")
- "Stochastic A*" (select + "Compute selected")

In Figure 6, only the first two functions are used.

### Experimental Variable

The experiments vary the number of initial tokens on place `SalMediumStart`, ranging from 1 to 10.<br>
After each modification, the following steps are executed:

- Click "Stochastic Reachability Graph"<br>
- Click "Stochastic Reachability Path"

All output files are stored in the default `output` directory.

### Acyclic Model Experiment

For the acyclic model, the procedure follows the steps described above.<br>
Each run directly produces the reachability graph and path results.

### Cyclic Model Experiment

For the cyclic model, an additional step is required:

1. First generate the reachability graph using "Stochastic Reachability Graph".
2. Extract the maximum depth of the generated graph.
3. Input this value into the "Max Depth" field to the right of "Stochastic Reachability Path".
4. Execute "Stochastic Reachability Path" to generate path results.

### Post-processing

Each result file contains a field `Stored`, indicating the number of stored markings.<br>
This value is used as input for the Python script `fig_6.py`, which reconstructs Figure 6.

The `Probability` column from reachability graph results is treated as the reference total probability for all subsequent analyses.

## Figure 7: Comparative Analysis of Stochastic Dijkstra and A*
Figure 7 is conducted using the acyclic model `acyclic_model.xml` and the parameter file `parameters.xml`.

### Experimental Configurations

Four experimental configurations are evaluated.<br>

| Experiment | Initial Marking | Target Marking |
|------------|----------------|----------------|
| 1 | (2, 0, 0, 1, 0) | (0, 0, 1, 1, 2) |
| 2 | (3, 0, 0, 1, 0) | (0, 0, 1, 1, 3) |
| 3 | (4, 0, 0, 1, 0) | (0, 0, 1, 1, 4) |
| 4 | (5, 0, 0, 1, 0) | (0, 0, 1, 1, 5) |

where the tuple follows the order:<br>
(SalMediumStart, SalSurface, NrRuffle, CapSurface, SalRuffle).

### Algorithm Execution

For each experiment, the following algorithms are executed:

- Stochastic Dijkstra
- Stochastic A*

The `Top Paths` parameter is fixed to:

- `10`

### Output Files

Each execution generates the following output files:
- `dijkstra.csv`
- `dijkstra_addedtoQ.csv`
- `dijkstra_expanded.csv`
- `astar.csv`
- `astar_addedtoQ.csv`
- `astar_expanded.csv`

**Note on Output Files**

To avoid overwriting results from different runs and to facilitate post-processing, 
it is recommended to rename output files by adding a prefix corresponding to the 
number of initial tokens

Example:

`dijkstra.csv` → `3_dijkstra.csv`

For further analysis, files can optionally be organized into folders such as 
`dijkstra/` and `astar/`.

### Post-processing

The files `dijkstra.csv` and `astar.csv` contain the fields `Probability` and `RealTime`.

The total probability reference is obtained from the reachability graph results of Figure 6 (acyclic model, initial tokens = 2/3/4/5).

These values are extracted and used in the Python script `fig_7.py`.

Running `fig_7.py` produces Figure 7.

## Figure 8: Accuracy Analysis of Stochastic A*

Figure 8 is generated using previously computed results from:

- Figure 6 (acyclic model, stochastic reachability tree results)
- Figure 7 (Stochastic A* results)

No additional experiments are required.

### Selected Configurations

- Initial tokens = 4
- Initial tokens = 5

### Data Sources

The following data are used:

- From Figure 6: reachability tree results (acyclic model)
- From Figure 7: `astar.csv` output files

### Post-processing

From each selected result file, the following fields are extracted:

- `Probability`
- `RealTime`

These values are extracted and used as input data for the Python script `fig_8.py`.

The total probability reference is reused for Figure 8.

Running `fig_8.py` produces Figure 8.

## Figure 9: Expansion and Coverage Analysis

Figure 9 is generated using the experimental results from Figure 7. No additional simulation is required.

### (a) Expansion Analysis

Subfigure (a) is generated from the `expanded` output files of both algorithms.

The input data should be organized as:
```
base_path/
├── dijkstra/
│   ├── {n}_dijkstra_expanded.csv
│   ├── ...
├── astar/
│   ├── {n}_astar_expanded.csv
│   ├── ...
```
where `n` denotes the number of initial tokens.

Set `base_path` in `fig_9_a.py` to the parent directory containing both the `dijkstra/` and `astar/` subfolders, then run the script to generate Figure 9(a).

### (b) Coverage Analysis

Subfigure (b) uses the `addedtoQ` output files generated by both algorithms in Figure 7.

The file paths for each experiment should be specified directly in `fig_9_b.py`.

Running the script produces Figure 9(b).

## Figure 10: Paths in Cyclic Model

Figure 10 is generated using the cyclic Petri net model `cyclic_model.xml` and the parameter file `parameters.xml`.

### Experimental Configurations

Two experimental configurations are evaluated.

| Experiment | Initial Marking | Target Marking |
| ---------- | --------------- | -------------- |
| 1 (Cyclic Target)     | (2, 0, 0, 0, 1, 0) | (0, 1, 0, 1, 1, 1) |
| 2 (Non-Cyclic Target) | (2, 0, 0, 0, 1, 0) | (0, 0, 0, 1, 1, 2) |

where the tuple follows the order:<br>
(SalMediumStart, SalMedium, SalSurface, NrRuffle, CapSurface, SalRuffle).

### Algorithm Execution

For each experiment, the following algorithms are executed:

- Stochastic Dijkstra
- Stochastic A*

The `Top Paths` parameter is fixed to:

- `10`

**note** 

For long-running cases with Stochastic Dijkstra, see the [Long-Running Computation Note](#implementation-note-on-long-running-computations) in the README.


### Post-processing

The files `dijkstra.csv` and `astar.csv` contain the fields `Probability` and `RealTime`.

The total probability reference is obtained from the reachability graph results of Figure 6 (cyclic model, initial tokens = 2).

These values are extracted and used in the Python script `fig_10.py`.

Running `fig_10.py` produces Figure 10.

## Figure 11: Expansion Analysis in Cyclic Model

Figure 11 is generated using the cyclic Petri net model `cyclic_model.xml` and the parameter file `parameters.xml`.

### Experimental Configurations

The initial and target markings are identical to those defined in Figure 10.

The experiment varies the stochastic reaction constant of transition `take_off`, which controls the cycle intensity:

- Strong: default setting (results reused from Figure 10)
- Mild: 0.067
- Weak: 0.0067

### Algorithm Execution

For Mild and Weak configurations, both target markings are evaluated, resulting in four experiments in total.

For each experiment, the following algorithms are executed:

- Stochastic Dijkstra
- Stochastic A*

The `Top Paths` parameter is fixed to:

- `10`

**note** 

For long-running cases under strong cycle intensity with Stochastic Dijkstra, see the [Long-Running Computation Note](#implementation-note-on-long-running-computations) in the README.

### Post-processing

The paths of `expanded` output files for each experiment should be specified directly in `fig_11.py`.

Running `fig_11.py` generates Figure 11.

## Figure 12: Coverage Analysis in Cyclic Model

Figure 12 is based on the same experimental runs as Figure 11, using the cyclic Petri net model `cyclic_model.xml` and parameter file `parameters.xml`.

No additional experiments are performed. The results are directly reused from the experiments in Figure 11.

**note** 

For long-running cases under strong cycle intensity with Stochastic Dijkstra, see the [Long-Running Computation Note](#implementation-note-on-long-running-computations) in the README.

### Post-processing

The paths of `addedtoQ` output files for each experiment should be specified directly in `fig_12.py`.

Running `fig_12.py` generates Figure 12.

## Implementation Note on Long-Running Computations

For the cyclic model under the strong cycle intensity configuration, the Stochastic Dijkstra search may fail to complete the search for the non-cyclic target within practical time limits. In such cases, the computation is manually terminated when the reachability interface counter exceeds 10^6 expansions by clicking "Stop Computation".

The displayed counter value is used as the expansion value for Figure 11. In the corresponding Python post-processing script (`fig_11.py`), this case is represented by setting the expansion count to 10^6.

The reachability interface should remain open for several hours after stopping, as the system continues writing `addedtoQ.csv`. The resulting file (typically containing over 80,000 entries in this study) reflects a truncated observation of the full expansion process, which may continue to grow if allowed to run longer.