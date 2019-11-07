# MOVELETS

This folder contains the elements to reproduce the experiments
performed on the publication:
```
Ferrero, C.A.; Alvares, L.O.; Zalewski, W.; Bogorny, V. MOVELETS: Exploring Relevant Subtrajectories for Robust Trajectory Classification.
Proc. of the 33rd ACM/SIGAPP Symposium On Applied Computing. 1-8. 2018.
```

Folder Description:
- datasets: contains the datasets used on each experiment, and also the scripts to run each method on each datasets.
- programs: contains the jar files used to perform the feature extraction using different methods and the R scripts to build the models.
- results: contains the experimental results.
- run: contains 
	- Bash scripts to run the experiments 1 (Cross-validation evaluation), 2 (Holdout evaluation), and 3 (Transportation Mode Classification);
	- R scripts to perform the comparisons.
- sourcecode:
	- The sourcecode of the jar files contained in the programs folder.

Requirements:
- Java 1.8 or superior (for feature extraction);
- R 3.3.3 (for building models and performing comparisons);
- Memory Usage:
	- For Experiment 1 and 2 you need around 20 GB per thread, because of the dataset Vehicle that has very long trajectories)
	- For Experiment 3 you need around 50 GB per thread.
	- We use all this memory because of the cache of distances. If you don't have all this memory, please contact us to give information about how to run without cache of distances.

How to run the experiments?
1. Access the directory "run/"
2. Execute:
	bash E1
	Rscript E1comparison.R
3. Use the script of step 2 by replacing E1 by E2 or E3.

For any doubt, please contact us by e-mail: anfer86@gmail.com or andres.ferrero@ifsc.edu.br
