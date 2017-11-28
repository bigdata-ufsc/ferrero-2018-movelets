package br.ufsc.trajectoryclassification.model.bo.movelets.QualityMeasures;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import br.ufsc.trajectoryclassification.model.vo.ISubtrajectory;
import br.ufsc.trajectoryclassification.model.vo.ITrajectory;
import br.ufsc.trajectoryclassification.utils.ArrayIndexComparator;

public class LeftSidePure2 implements IQualityMeasure {

	private List<ITrajectory> trajectories;
	
	private List<String> labels;	
	
	private Map<String,Long> classes;
	
	public LeftSidePure2(List<ITrajectory> trajectories) {
		this.trajectories = trajectories;
		this.labels = new ArrayList<>();
		for (int j = 0; j < trajectories.size(); j++) {
			labels.add(trajectories.get(j).getLabel());
		}
		this.classes = this.labels.stream().collect(Collectors.groupingBy(e -> e, Collectors.counting()));
	}
	

	@Override
	public void assesQuality(ISubtrajectory candidate) {
		// TODO Auto-generated method stub
						
		ArrayIndexComparator comparator = new ArrayIndexComparator(candidate.getDistances());
		Integer[] indexes = comparator.createIndexArray();
		Arrays.sort(indexes, comparator);
					
		String label = this.labels.get(indexes[0]);
		int maxElementsForTheClass = classes.get(label).intValue();
		double[] distances = candidate.getDistances();
		double maxDistance = 0;
		double splitdistance = 0;
		double splitpoint = 0;
				
		int n = 0;
		while (n < indexes.length && distances[indexes[n]] != Double.MAX_VALUE)
			n++;
		
		maxDistance = distances[indexes[n-1]];
				
		int i;
		for (i = 1; i < n; i++) {
			if (!label.equalsIgnoreCase(this.labels.get(indexes[i])))
				break;
		}
		
		if (i == n){
			splitdistance = Double.MAX_VALUE;
			splitpoint = distances[indexes[i-1]];			
		} else {
			splitdistance = (distances[indexes[i]] - distances[indexes[i-1]]);
			splitpoint	  = (distances[indexes[i]] + distances[indexes[i-1]]) / 2.0;			
		}
		
		/* It considers the minimum number of trajectories covered by 
		 * a movelet as 3;
		 * */
		int count = (i > 3) ? i-1 : 0;
	
		/* It extracts several information about the quality
		 * */
		Map<String, Double> data = new HashMap<>();
    	data.put("quality", count / (double) (maxElementsForTheClass-1) );
    	data.put("count", 1.0 * count);
    	data.put("splitpoint", splitpoint);
    	data.put("splitdistance", splitdistance);
    	data.put("size", 1.0 * candidate.getSize() );
    	data.put("start", 1.0 * candidate.getStart() );
    	data.put("tid", 1.0 * candidate.getTrajectory().getTid() );
    	data.put("maxDistance", maxDistance);
    	    	
    	IQuality<LeftSidePureQuality> quality = new LeftSidePureQuality();
    	quality.setData(data);	    	
		candidate.setQuality(quality);
		
	}

	
}
