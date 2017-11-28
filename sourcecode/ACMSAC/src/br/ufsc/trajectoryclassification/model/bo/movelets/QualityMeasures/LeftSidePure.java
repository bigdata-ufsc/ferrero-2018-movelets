package br.ufsc.trajectoryclassification.model.bo.movelets.QualityMeasures;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import br.ufsc.trajectoryclassification.model.vo.ISubtrajectory;
import br.ufsc.trajectoryclassification.model.vo.ITrajectory;
import br.ufsc.trajectoryclassification.utils.ArrayIndexComparator;
import weka.attributeSelection.InfoGainAttributeEval;
import weka.classifiers.trees.j48.BinC45Split;
import weka.core.Attribute;
import weka.core.DenseInstance;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.Utils;

public class LeftSidePure implements IQualityMeasure {

	private List<ITrajectory> trajectories;
	
	private List<String> labels;	
	
	private Map<String,Long> classes;
	
	public LeftSidePure(List<ITrajectory> trajectories) {
		this.trajectories = trajectories;
		this.labels = new ArrayList<>();
		for (int j = 0; j < trajectories.size(); j++) {
			labels.add(trajectories.get(j).getLabel());
		}
		this.classes = this.labels.stream().collect(Collectors.groupingBy(e -> e, Collectors.counting()));
	}
	

	public Instances fromDistancesToInstances(ISubtrajectory candidate){

		ArrayList<Attribute> atts = new ArrayList<Attribute>(2);
        
		ArrayList<String> strClasses = new ArrayList<>(classes.keySet());
		
        atts.add(new Attribute("distance"));
        atts.add(new Attribute("class",strClasses));

        Instances dataRaw = new Instances("Dataset",atts,0);
        
        for (int i = 0; i < candidate.getDistances().length; i++) {
        	double[] instanceValue1 = new double[2];
            instanceValue1[0] = candidate.getDistances()[i];
            instanceValue1[1] = classes.get(labels.get(i)).doubleValue();            
            dataRaw.add( new DenseInstance(1.0, instanceValue1) );
		}
        
    	dataRaw.setClassIndex(1);
    	
    	return dataRaw;
		
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
    	//data.put("quality", count / (double) (maxElementsForTheClass-1) );
		data.put("quality", getIGLSP(classes, label, count) );   	
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
	
	public double getIGLSP(Map<String,Long> classes, String label, int covered){
		
		if (covered == 0) return 0;
		
		Long n = classes.values().stream().mapToLong(e -> e).sum();
		Long nc = classes.get(label).longValue(); // elementForTheClass
		Long nr = n - covered;
		Long nrc = nc - covered;
		
		double ganho = (1.0/n) * (
				n  * Utils.log2(n) 	- nr * Utils.log2(nr) - 
				nc * Utils.log2(nc) + nrc * Utils.log2(nrc) );
		
		return ganho;
		
	}
	
}
