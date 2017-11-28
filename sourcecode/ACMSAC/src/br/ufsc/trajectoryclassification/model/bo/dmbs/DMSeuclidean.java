package br.ufsc.trajectoryclassification.model.bo.dmbs;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.math3.util.Pair;

import br.ufsc.trajectoryclassification.model.bo.dmbp.DMBP;
import br.ufsc.trajectoryclassification.model.bo.dmbp.IDistanceMeasureBetweenPoints;
import br.ufsc.trajectoryclassification.model.vo.IPoint;
import br.ufsc.trajectoryclassification.model.vo.ISubtrajectory;
import br.ufsc.trajectoryclassification.model.vo.ITrajectory;
import br.ufsc.trajectoryclassification.model.vo.Subtrajectory;
import br.ufsc.trajectoryclassification.model.vo.description.Description;
import br.ufsc.trajectoryclassification.model.vo.description.FeatureComparisonDesc;
import br.ufsc.trajectoryclassification.model.vo.description.SubtrajectoryComparisonDesc;

public class DMSeuclidean implements IDistanceMeasureForSubtrajectory {

	private IDistanceMeasureBetweenPoints dmbp;

	private SubtrajectoryComparisonDesc subtrajectoryComparisonDesc;

	private FeatureComparisonDesc featureComparisonDescToPoints;

	private List<FeatureComparisonDesc> featureComparisonDescToSubtrajectories;

	private Description description;

	public DMSeuclidean(Description description) {
		super();
		this.description = description;
		this.subtrajectoryComparisonDesc = description.getSubtrajectoryComparisonDesc();
		this.dmbp = DMBP.getDMBPfromDescription(description);

		List<FeatureComparisonDesc> list = subtrajectoryComparisonDesc.getFeatureComparisonDesc();

		featureComparisonDescToSubtrajectories = new ArrayList<>();

		for (FeatureComparisonDesc featureComparisonDesc : list) {
			if (featureComparisonDesc.getText().equalsIgnoreCase("points"))
				featureComparisonDescToPoints = featureComparisonDesc;
			else
				featureComparisonDescToSubtrajectories.add(featureComparisonDesc);
		}

	}
		
	@Override
	public Pair<ISubtrajectory, Double> getBestAlignment(ISubtrajectory s, ITrajectory t) {
		
		/*
		 * If subtrajectory is largest than trajectory return maximum distance
		 */
		if (s.getSize() > t.getData().size())
			return new Pair<>(null,Double.MAX_VALUE);

		/*
		 * Compute min distance
		 */
		if (featureComparisonDescToPoints != null) {
			if (featureComparisonDescToSubtrajectories.size() > 0)
				return getBestAlignmentByPointAndSubtrajectoryFeatures(s, t);
			else
				return getBestAlignmentByPointFeatures(s, t);
		} else {
			if (featureComparisonDescToSubtrajectories.size() > 0)
				return getBestAlignmentSubtrajectoryFeatures(s, t);
			else
				return new Pair<>(null,Double.MAX_VALUE);
		}
		

	}

	private Pair<ISubtrajectory, Double> getBestAlignmentByPointFeatures(ISubtrajectory s, ITrajectory t) {

		if (s.getData().size() > t.getData().size())
			return new Pair<>(null,Double.MAX_VALUE);

		List<IPoint> menor = s.getData();
		List<IPoint> maior = t.getData();

		int diffLength = maior.size() - menor.size();		
		double minSumFound = Double.MAX_VALUE;
		int bestPosition = -1;
		double currentSum = 0;
		double value = 0;
		
		for (int i = 0; i <= diffLength; i++) {

			currentSum = 0;

			/* Here we use the early abandon speed up technique.
			 * Please, read papers about that before reimplement  
			 * */
			for (int j = 0; j < menor.size() && currentSum < minSumFound; j++) {

				value = dmbp.getDistance(menor.get(j), maior.get(i + j));

				if (value == Double.MAX_VALUE) {
					/* Com esta atribuicao se abandona o loop
					 * na proxima iteracao
					 * */
					currentSum = value;
				} else {
					currentSum += value * value;
				}

			}

			/* If a better best alignment happens then
			 * store the best alignment distance and 
			 * the position in the aligned trajectory.			 * 
			 * */
			if (currentSum < minSumFound){
				minSumFound = currentSum;
				bestPosition = i;
			}
		}
		
		/* If the minimum sum found is equal to MAX_VALUE there are
		 * any alignment. In general this occurs when the subtrajectory
		 * is longer than the trajectory.
		 * */
		if (minSumFound == Double.MAX_VALUE || bestPosition == -1)
			return new Pair<>(null,Double.MAX_VALUE);
				
		/* It normalizes the sum of square distance 
		 * */
		double distance = Math.sqrt(minSumFound/menor.size());
		
		/* It creates and returns the part of the trajectory that
		 * best matches with the subtrajectory.
		 * */
		int start = bestPosition;
		int end = bestPosition + menor.size() - 1;				
		return new Pair<>(new Subtrajectory(start, end , t), distance);
	}
	
	private Pair<ISubtrajectory, Double> getBestAlignmentSubtrajectoryFeatures(ISubtrajectory s, ITrajectory t) {
		
		/* NOT IMPLEMENTED YET
		 * */
		
		return null;
	}

	private Pair<ISubtrajectory, Double> getBestAlignmentByPointAndSubtrajectoryFeatures(ISubtrajectory s, ITrajectory t) {
		
		/* NOT IMPLEMENTED YET
		 * */
		
		return null;
	}
	

	@Override
	public IDistanceMeasureBetweenPoints getDMBP() {
		// TODO Auto-generated method stub
		return dmbp;
	}

	public Description getDescription() {
		return description;
	}
}
