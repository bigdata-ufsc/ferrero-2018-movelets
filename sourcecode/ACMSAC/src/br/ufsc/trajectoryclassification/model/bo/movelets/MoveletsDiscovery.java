package br.ufsc.trajectoryclassification.model.bo.movelets;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Callable;

import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;
import org.apache.commons.math3.util.Pair;

import br.ufsc.trajectoryclassification.model.bo.dmbs.IDistanceMeasureForSubtrajectory;
import br.ufsc.trajectoryclassification.model.bo.movelets.QualityMeasures.IQualityMeasure;
import br.ufsc.trajectoryclassification.model.vo.ISubtrajectory;
import br.ufsc.trajectoryclassification.model.vo.ITrajectory;
import br.ufsc.trajectoryclassification.model.vo.Subtrajectory;


public class MoveletsDiscovery implements Callable<Integer> {
	
	private List<ISubtrajectory> candidates;

	private ITrajectory trajectory;
	
	private List<ITrajectory> trajectories;

	private IDistanceMeasureForSubtrajectory dmbt;
	
	private IQualityMeasure qualityMeasure;
	
	private int minSize;
	
	private int maxSize;
	
	private boolean cache;
		
	public MoveletsDiscovery(List<ISubtrajectory> candidates, ITrajectory trajectory, List<ITrajectory> trajectories,
			IDistanceMeasureForSubtrajectory dmbt, IQualityMeasure qualityMeasure, int minSize, int maxSize, boolean cache) {
		super();
		this.candidates = candidates;
		this.trajectory = trajectory;
		this.trajectories = trajectories;
		this.dmbt = dmbt;
		this.qualityMeasure = qualityMeasure;
		this.minSize = minSize;
		this.maxSize = maxSize;
		this.cache = cache;
	}

	@Override
	public Integer call() throws Exception {		
	
		measureShapeletCollection();
	
		return 0;

	}

	public void measureShapeletCollection() {
		
		List<ISubtrajectory> candidates;
		
		if (this.cache)
			candidates = fastMoveletsDiscoveryUsingCache(trajectory, trajectories, minSize, maxSize);
		else 
			candidates = moveletsDiscoveryWithoutCache(trajectory, trajectories, minSize, maxSize);

		for (ISubtrajectory candidate : candidates) {
			/*
			 * STEP 1: COMPUTE DISTANCES, IF NOT COMPUTED YET
			 */
			if (candidate.getDistances() == null)		
				ComputeDistances(candidate);
			
			/*
			 * STEP 2: ASSES QUALITY, IF REQUIRED
			 */
			if (qualityMeasure != null)
				AssesQuality(candidate);
		}
		
		/* STEP 3: SELECTING BEST CANDIDATES
		 * */		
		this.candidates.addAll(MoveletsFilterAndRanker.getShapelets(candidates));
	
	}

	
	private void AssesQuality(ISubtrajectory candidate) {
		qualityMeasure.assesQuality(candidate);
	}

	private void ComputeDistances(ISubtrajectory candidate) {
		
		/* This pairs will store the subtrajectory of the best alignment 
		 * of the candidate into each trajectory and the distance 
		 * */
		Pair<ISubtrajectory,Double> distance;
		
		double[] trajectoryDistancesToCandidate = new double[trajectories.size()];
		
		ISubtrajectory[] bestAlignments = new ISubtrajectory[trajectories.size()];
				
		/* It calculates the distance of trajectories to the candidate
		 */
		for (int i = 0; i < trajectories.size(); i++) {
			
			distance = dmbt.getBestAlignment(candidate, trajectories.get(i));
						
			bestAlignments[i] = distance.getFirst();
			trajectoryDistancesToCandidate[i] = distance.getSecond();			
		}
		
		candidate.setDistances(trajectoryDistancesToCandidate);
		candidate.setBestAlignments(bestAlignments);
	}

	private List<ISubtrajectory> getCandidatesUsingMDist(ITrajectory trajectory, List<ITrajectory> train, int size, double[][][] mdist) {
		
		int n = trajectory.getData().size();
				
		List<ISubtrajectory> candidates = new ArrayList<>();

		for (int start = 0; start <= (n - size); start++) {
			
			ISubtrajectory s = new Subtrajectory(start, start + size - 1, trajectory);
			
			double[][] distancesForAllT = mdist[start];
			
			double[] distances = new double[train.size()];
			
			for (int i = 0; i < distances.length; i++) {
				double[] e = distancesForAllT[i];
				int limit = train.get(i).getData().size() - size + 1;				
				distances[i] = (limit > 0) ? Arrays.stream(e,0,limit).min().getAsDouble() : Double.MAX_VALUE;
			}
			
			double[] distancesSqrt = new double[distances.length];
			for (int i = 0; i < distances.length; i++) {
				distancesSqrt[i] =  (distances[i] != Double.MAX_VALUE) ? Math.sqrt(distances[i] / size) : Double.MAX_VALUE;
			}				
						
			s.setBestAlignments(new ISubtrajectory[0]);
			s.setDistances(distancesSqrt);			
			
			candidates.add(s);

		}
		
		return candidates;
		
	}
			
	public int getArrayIndex(double[] arr,double value) {
	    for(int i=0;i<arr.length;i++)
	        if(arr[i]==value) return i;
	    return -1;
	}
	
	private double[][][] getBaseCase(ITrajectory trajectory, List<ITrajectory> train){
		int n = trajectory.getData().size();
		int size = 1;
		
		double[][][] base = new double[(n - size)+1][][];		
		
		for (int start = 0; start <= (n - size); start++) {
			
			base[start] = new double[train.size()][];				
			
			for (int i = 0; i < train.size(); i++) {
								
				base[start][i] = new double[(train.get(i).getData().size()-size)+1];
						
				for (int j = 0; j <= (train.get(i).getData().size()-size); j++) {
					
					double distance = dmbt.getDMBP().getDistance(
							trajectory.getData().get(start),
							train.get(i).getData().get(j));

					base[start][i][j] = (distance != Double.MAX_VALUE) ? (distance * distance) : Double.MAX_VALUE;
					
				} // for (int j = 0; j <= (train.size()-size); j++)
				
			} //for (int i = 0; i < train.size(); i++)
			
		} // for (int start = 0; start <= (n - size); start++)

		return base;
	}


	private double[][][] getNewSize(ITrajectory trajectory, List<ITrajectory> train, double[][][] base, double[][][] lastSize, int size) {
		
		int n = trajectory.getData().size();	
		
		for (int start = 0; start <= (n - size); start++) {
						
			for (int i = 0; i < train.size(); i++) {
				
				if (train.get(i).getData().size() >= size) {						
											
					for (int j = 0; j <= (train.get(i).getData().size()-size); j++) {
						
						if (lastSize[start][i][j] != Double.MAX_VALUE)
							
							lastSize[start][i][j] += base[start+size-1][i][j+size-1];
						
					} // for (int j = 0; j <= (train.size()-size); j++)
					
				} // if (train.get(i).getData().size() >= size) 
				
			} // for (int i = 0; i < train.size(); i++)
			
		} // for (int start = 0; start <= (n - size); start++)
		
		return lastSize;
	}


	private List<ISubtrajectory> moveletsDiscoveryWithoutCache(ITrajectory trajectory, List<ITrajectory> train, int minSize, int maxSize){
		
		List<ISubtrajectory> candidates = new ArrayList<>();
		
		int n = trajectory.getData().size();
		maxSize = (maxSize == -1) ? n : maxSize;
				
		MyCounter.numberOfCandidates += (maxSize * (maxSize-1) / 2);
				
		for (int size = minSize; size <= maxSize; size++) {				
			
			List<ISubtrajectory> candidatesOfSize = new ArrayList<>();
			
			for (int start = 0; start <= (n - size); start++) {
				
				ISubtrajectory candidate = new Subtrajectory(start, start + size - 1, trajectory);
			
				ComputeDistances(candidate);
				
				AssesQuality(candidate);
				
				candidatesOfSize.add(candidate);
			}
			
			candidatesOfSize = MoveletsFilterAndRanker.getShapelets(candidatesOfSize);
			
			candidates.addAll(candidatesOfSize);
		}
			
		return candidates;
		
	}
	
	private double[][][] clone3DArray(double [][][] source){
		double[][][] dest = new double[source.length][][];
		for (int i = 0; i < dest.length; i++) {
			dest[i] = new double[source[i].length][];
			for (int j = 0; j < dest[i].length; j++) {
				dest[i][j] = new double[source[i][j].length];
				for (int k = 0; k < dest[i][j].length; k++) {
					dest[i][j][k] = source[i][j][k];
				}
			}
		}
		return dest;		
	}
	
	private List<ISubtrajectory> fastMoveletsDiscoveryUsingCache(ITrajectory trajectory, List<ITrajectory> train, int minSize, int maxSize){
				
		List<ISubtrajectory> candidates = new ArrayList<>();
		
		int n = trajectory.getData().size();
		maxSize = (maxSize == -1) ? n : maxSize;
		
		MyCounter.numberOfCandidates += (maxSize * (maxSize-1) / 2);
		/* It starts with the base case
		 * */		
		int size = 1;
				
		double[][][] base = getBaseCase(trajectory, train);
		if( size >= minSize ) candidates.addAll(getCandidatesUsingMDist(trajectory, train, size, base));
		
		double[][][] lastSize = clone3DArray(base);
		
		/* Tratar o resto dos tamanhos 
		 * */
		for (size = 2; size <= maxSize; size++) {
	
			/* Precompute de distance matrix
			 * */
			double[][][] newSize = getNewSize(trajectory, train, base, lastSize, size);
			
			/* Create candidates and compute min distances
			 * */			
			List<ISubtrajectory> candidatesOfSize = getCandidatesUsingMDist(trajectory, train, size, newSize);
			
			if (size >= minSize){
				
				for (ISubtrajectory candidate : candidatesOfSize) {
					AssesQuality(candidate);
				}
				
				candidatesOfSize = MoveletsFilterAndRanker.getShapelets(candidatesOfSize);
				
				candidates.addAll(candidatesOfSize);
			}
		
			lastSize = newSize;
						
		} // for (int size = 2; size <= max; size++)	
	
		base =  null;
		lastSize = null;
		
		candidates = MoveletsFilterAndRanker.getShapelets(candidates);
		
		return candidates;
	}


}
