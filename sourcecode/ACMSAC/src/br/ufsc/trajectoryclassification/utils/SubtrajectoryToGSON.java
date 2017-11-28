package br.ufsc.trajectoryclassification.utils;

import java.util.Arrays;
import java.util.HashMap;

import br.ufsc.trajectoryclassification.model.bo.movelets.QualityMeasures.IQuality;
import br.ufsc.trajectoryclassification.model.vo.ISubtrajectory;
import br.ufsc.trajectoryclassification.model.vo.description.Description;
import br.ufsc.trajectoryclassification.model.vo.description.FeatureComparisonDesc;
import br.ufsc.trajectoryclassification.model.vo.features.IFeature;

public class SubtrajectoryToGSON {
	
	private int start;

	private int end;
 
	private int trajectory;
	
	private String label;

	private HashMap<String, IFeature> features;
		
	private HashMap<String, Double> maxValues;
	
	private double[] distances;
	
	private int[] positions;
	
	private IQuality quality;

	public SubtrajectoryToGSON(int start, int end, int trajectory, String label,
			HashMap<String, IFeature> features, double[] distances, ISubtrajectory[] bestAlignments, IQuality quality, Description description) {
		super();
		this.start = start;
		this.end = end;
		this.trajectory = trajectory;
		this.label = label;
		this.features = features;		
		this.distances = distances;
		this.positions = Arrays.asList(bestAlignments).stream().mapToInt(e -> (e!=null) ? e.getStart() : -1).toArray();
		this.quality = quality;
		this.maxValues = new HashMap<>();
		
		for (FeatureComparisonDesc featureComparisonDesc : description.getPointComparisonDesc().getFeatureComparisonDesc()) {
			maxValues.put(featureComparisonDesc.getText(), featureComparisonDesc.getMaxValue());				
		}

		for (FeatureComparisonDesc featureComparisonDesc : description.getSubtrajectoryComparisonDesc().getFeatureComparisonDesc()) {
			maxValues.put(featureComparisonDesc.getText(), featureComparisonDesc.getMaxValue());				
		}

	}

	public int getStart() {
		return start;
	}

	public void setStart(int start) {
		this.start = start;
	}

	public int getEnd() {
		return end;
	}

	public void setEnd(int end) {
		this.end = end;
	}

	public int getTrajectory() {
		return trajectory;
	}

	public void setTrajectory(int trajectory) {
		this.trajectory = trajectory;
	}

	public String getLabel() {
		return label;
	}

	public void setLabel(String label) {
		this.label = label;
	}


	public HashMap<String, IFeature> getFeatures() {
		return features;
	}

	public void setFeatures(HashMap<String, IFeature> features) {
		this.features = features;
	}

	public double[] getDistances() {
		return distances;
	}

	public void setDistances(double[] distances) {
		this.distances = distances;
	}

	public IQuality getQuality() {
		return quality;
	}

	public void setQuality(IQuality quality) {
		this.quality = quality;
	}
	
		
	public static SubtrajectoryToGSON fromSubtrajectory(ISubtrajectory s, Description description){
		
		return new SubtrajectoryToGSON(s.getStart(), s.getEnd(), s.getTrajectory().getTid(), 
				s.getTrajectory().getLabel(), s.getFeatures(), s.getDistances(), s.getBestAlignments(),
				s.getQuality(), description);
		
	}

}
