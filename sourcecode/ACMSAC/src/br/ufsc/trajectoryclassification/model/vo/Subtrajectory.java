package br.ufsc.trajectoryclassification.model.vo;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import br.ufsc.trajectoryclassification.model.bo.movelets.QualityMeasures.IQuality;
import br.ufsc.trajectoryclassification.model.vo.features.IFeature;

public class Subtrajectory implements ISubtrajectory {

	private int start;

	private int end;
 
	private ITrajectory trajectory;

	private List<IPoint> data;

	private HashMap<String, IFeature> features;
	
	private double[] distances;
	
	private Map<Integer, double[]> mdist; 
		
	private ISubtrajectory[] bestAlignments;
	
	private boolean[] goodTrajectories;
	
	private IQuality quality;

	public Subtrajectory(int start, int end, ITrajectory t) {
		super();
		this.start = start;
		this.end = end;
		this.trajectory = t;
		this.data = t.getData().subList(start, end+1);
		this.features = new HashMap<>();
	}

	public Subtrajectory(int start, int end, ITrajectory t, boolean[] goodTrajectories) {
		super();
		this.start = start;
		this.end = end;
		this.trajectory = t;
		this.data = t.getData().subList(start, end+1);
		this.features = new HashMap<>();
		this.goodTrajectories = goodTrajectories;
	}
	
	public boolean[] getGoodTrajectories() {
		return goodTrajectories;
	}
	
	public HashMap<String, IFeature> getFeatures() {
		return features;
	}
	
	@Override
	public IFeature getFeature(String featureName){
		return 	features.get(featureName);
	}

	@Override
	public String toString() {

		String string = new String();

		string += "Size: " + getSize() + "\n";
		string += "Origin: t" + getTrajectory().getTid() + " from " + start + " to " + end + "\n";
		string += "Label: " + getTrajectory().getLabel() + "\n";
		string += "Data: \n";

		return string;
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

	public ITrajectory getTrajectory() {
		return trajectory;
	}

	public void setTrajectory(ITrajectory trajectory) {
		this.trajectory = trajectory;
	}

	public List<IPoint> getData() {
		return data;
	}

	public void setData(List<IPoint> data) {
		this.data = data;
	}

	public void setFeatures(HashMap<String, IFeature> features) {
		this.features = features;
	}

	@Override
	public int getSize() {
		// TODO Auto-generated method stub
		return end - start + 1;
	}
	
	public double[] getDistances() {
		return distances;
	}
	
	 public ISubtrajectory[] getBestAlignments() {
		return bestAlignments;
	}
	 
	public Map<Integer, double[]> getMdist() {
		return mdist;
	}
	 
	@Override
	public void setDistances(double[] distances) {
		// TODO Auto-generated method stub
		this.distances = distances;
	}
	
	@Override
	public void setBestAlignments(ISubtrajectory[] bestAlignments) {
		this.bestAlignments = bestAlignments;
	}	
	
	@Override
	public void setQuality(IQuality quality) {
		this.quality = quality;
	}
	
	@Override
	public IQuality getQuality() {
		// TODO Auto-generated method stub
		return this.quality;
	}

	@Override
	public double[] getPoint() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void createMDist() {
		// TODO Auto-generated method stub
		mdist = new HashMap<>();
	}
	
}
