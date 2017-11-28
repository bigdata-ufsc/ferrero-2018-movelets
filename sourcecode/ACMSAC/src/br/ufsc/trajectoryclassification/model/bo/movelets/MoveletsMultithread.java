package br.ufsc.trajectoryclassification.model.bo.movelets;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadPoolExecutor;

import org.apache.commons.collections4.ListUtils;

import br.ufsc.trajectoryclassification.model.bo.dmbs.IDistanceMeasureForSubtrajectory;
import br.ufsc.trajectoryclassification.model.bo.movelets.QualityMeasures.IQualityMeasure;
import br.ufsc.trajectoryclassification.model.vo.ISubtrajectory;
import br.ufsc.trajectoryclassification.model.vo.ITrajectory;
import br.ufsc.trajectoryclassification.model.vo.Subtrajectory;
import br.ufsc.trajectoryclassification.utils.ProgressBar;
import br.ufsc.trajectoryclassification.utils.Utils;

/**
 * @author andres
 *
 */
public class MoveletsMultithread {

	private List<ITrajectory> train;
	private List<ITrajectory> test;
	private IDistanceMeasureForSubtrajectory dmbt;
	private int minSize;
	private int maxSize = -1;
	private int nthreads;
	private IQualityMeasure qualityMeasure;
	private String resultDirPath;
	private boolean cache = false;
	private boolean showProgressBar = true;
	
	public MoveletsMultithread(List<ITrajectory> train, List<ITrajectory> test,
			IDistanceMeasureForSubtrajectory dmbt, int minSize, int nthreads,
			IQualityMeasure qualityMeasure, boolean cache, 
			String resultDirPath) {
		super();
		this.train = train;
		this.test = test;
		this.dmbt = dmbt;
		this.minSize = minSize;
		this.nthreads = nthreads;
		this.qualityMeasure = qualityMeasure;
		this.cache = cache;
		this.resultDirPath = resultDirPath;
	}

	public List<ISubtrajectory> moveletsDiscoveryMultithread(List<ITrajectory> trajectories, boolean filterAndRank) {

		List<ISubtrajectory> candidates = new ArrayList<>();
		
		if (this.nthreads > 1){
			
		ThreadPoolExecutor executor = (ThreadPoolExecutor) Executors.newFixedThreadPool(this.nthreads);

		executor.setRejectedExecutionHandler(new ThreadPoolExecutor.DiscardPolicy());

		List<Future<Integer>> resultList = new ArrayList<>();
				
		for (ITrajectory trajectory : trajectories) {
			
			MoveletsDiscovery shapeletsExtractor = new MoveletsDiscovery(candidates, trajectory, trajectories, dmbt, (filterAndRank) ? qualityMeasure : null, minSize, maxSize, cache);
			
			Future<Integer> result = executor.submit(shapeletsExtractor);

			resultList.add(result);
			
		}
		
		ProgressBar progressBar = new ProgressBar();

		int progress = 0;
		progressBar.update(progress, train.size());
		
		List<Integer> results = new ArrayList<>();

		for (Future<Integer> future : resultList) {

			try {

				results.add(future.get());
				
				progressBar.update(progress++, train.size());

				Executors.newCachedThreadPool();

			} catch (InterruptedException | ExecutionException e) {
				e.printStackTrace();
			}
		}

		executor.shutdown();
		}
		
		else {
			
			ProgressBar progressBar = new ProgressBar();
			int progress = 0;
			progressBar.update(progress, trajectories.size());
						
			for (ITrajectory trajectory : trajectories) {
				
				//System.gc();
				
				MoveletsDiscovery shapeletsExtractor = new MoveletsDiscovery(candidates, trajectory, trajectories, dmbt, (filterAndRank) ? qualityMeasure : null, minSize, maxSize, cache);
				
				shapeletsExtractor.measureShapeletCollection();

				progressBar.update(progress++, trajectories.size());
				
			}			
			
		}
		
		if (filterAndRank)
			return MoveletsFilterAndRanker.rankCandidates(candidates);
		else
			return candidates;
		
	}
	
	public List<ISubtrajectory> moveletsFinding(List<ISubtrajectory> candidates,
			List<ITrajectory> trajectories, boolean filterAndRank) {

		ThreadPoolExecutor executor = (ThreadPoolExecutor) Executors.newFixedThreadPool(this.nthreads);

		executor.setRejectedExecutionHandler(new ThreadPoolExecutor.DiscardPolicy());

		List<Future<Integer>> resultList = new ArrayList<>();

		/*
		 * Create groups of subtrajectory candidates to send to threads
		 */
		int groupSize = (candidates.size() > this.nthreads) ? Math.floorDiv(candidates.size(), this.nthreads) + 1 : 1;

		if (filterAndRank) Collections.shuffle(candidates);

		List<List<ISubtrajectory>> groups = ListUtils.partition(candidates, groupSize);

		/*
		 * Create the processes to send to thread
		 */
		for (List<ISubtrajectory> group : groups) {

			if (group.isEmpty())
				break;

			MoveletsFinding shapeletsExtractor = new MoveletsFinding(group, trajectories, dmbt);
			
			Future<Integer> result = executor.submit(shapeletsExtractor);

			resultList.add(result);
		}

		/*
		 * Execute threads
		 */
		List<Integer> results = new ArrayList<>();

		for (Future<Integer> future : resultList) {

			try {

				results.add(future.get());
				
				Executors.newCachedThreadPool();

			} catch (InterruptedException | ExecutionException e) {
				e.printStackTrace();
			}
		}

		executor.shutdown();

		/*
		 * Return the best candidates
		 */
		if (filterAndRank)
			return MoveletsFilterAndRanker.getShapelets(candidates);
		else
			return candidates;
	}

	public void run() {

		long startTime = System.currentTimeMillis();
		
		/* STEP 1: It starts at discovering movelets 
		 * */		
		boolean removeSelfsimilarAndRank = true;
		
		List<ISubtrajectory> movelets = moveletsDiscoveryMultithread(train, removeSelfsimilarAndRank);

		long estimatedTime = System.currentTimeMillis() - startTime;
		
		MyCounter.data.put("MoveletsDiscoveryTime", estimatedTime);
		MyCounter.data.put("MoveletsFound", (long) movelets.size());
		
		MyCounter.numberOfShapelets = movelets.size();

		/* STEP 2: It runs the pruning process  
		 * */		
		movelets = MoveletsFilterAndRanker.noveltyFilter(movelets);
		MyCounter.data.put("MoveletsAfterPruning", (long) movelets.size());
		
		/* STEP 3: It transforms the training and test sets of trajectories
		 * using the movelets
		 * */
				
		/* It initializes the set of distances of all movelets to null
		 * */
		for (ISubtrajectory movelet : movelets) {
			movelet.setDistances(null);
		}		
		
		/* Transforming the training set using movelets
		 * In this step the set of distances is filled by this method
		 * */
		new MoveletsFinding(movelets,train,dmbt).run();
		
		/* It puts distances as trajectory attributes
		 * */
		for (ISubtrajectory movelet : movelets) {
			Utils.putAttributeIntoTrajectories(train, movelet);
		}		
		
		/* It writes a JSON and a CSV in a attribute-value format
		 * */		
		Utils.writeShapeletsToGSON(train, movelets, dmbt.getDescription(), resultDirPath + "moveletsOnTrain.json");		
		Utils.writeAttributesCSV(train, resultDirPath + "train.csv");
		
		/* If a test trajectory set was provided, it does the same.
		 * and return otherwise 
		 */		
		if (test.isEmpty()) return;
		
		for (ISubtrajectory movelet : movelets) {
			movelet.setDistances(null);
		}
		
		new MoveletsFinding(movelets,test,dmbt).run();				

		for (ISubtrajectory movelet : movelets) {
			Utils.putAttributeIntoTrajectories(test, movelet);
		}
				
		Utils.writeShapeletsToGSON(test, movelets, dmbt.getDescription(), resultDirPath + "moveletsOnTest.json");
		Utils.writeAttributesCSV(test, resultDirPath + "test.csv");

	}
	
		
	public void setShowProgressBar(boolean showProgressBar) {
		this.showProgressBar = showProgressBar;
	}

	public void setMaxSize(int maxSize) {
		this.maxSize = maxSize;
	}

}
