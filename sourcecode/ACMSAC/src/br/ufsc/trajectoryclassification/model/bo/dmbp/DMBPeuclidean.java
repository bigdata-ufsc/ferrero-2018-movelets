package br.ufsc.trajectoryclassification.model.bo.dmbp;

import java.util.List;

import br.ufsc.trajectoryclassification.model.vo.IPoint;
import br.ufsc.trajectoryclassification.model.vo.description.Description;
import br.ufsc.trajectoryclassification.model.vo.description.FeatureComparisonDesc;
import br.ufsc.trajectoryclassification.model.vo.description.PointComparisonDesc;

public class DMBPeuclidean implements IDistanceMeasureBetweenPoints {
	
	private PointComparisonDesc pointComparisonDesc;
		
	public DMBPeuclidean(Description description) {
		// TODO Auto-generated constructor stub
		this.pointComparisonDesc = description.getPointComparisonDesc();
	}
	
	public PointComparisonDesc getPointComparisonDesc() {
		return pointComparisonDesc;
	}
	
	@Override
	public double getDistance(IPoint p1, IPoint p2) {

		List<FeatureComparisonDesc> list = pointComparisonDesc.getFeatureComparisonDesc();
				
		double[] distances = new double[list.size()];
		
		for (int i = 0; i < distances.length; i++) {
			
			String strFeature = list.get(i).getText().toLowerCase();
			distances[i] = p1.getFeature(strFeature).getDistanceTo(p2.getFeature(strFeature), list.get(i));								
			
		}
		
		return normalizedEuclideanDistance(distances);
		
	}
	
	public double normalizedEuclideanDistance(double[] distances){
		
		double sumOfSquares = 0;
		double n = distances.length;
		
		for (int i = 0; i < n; i++) {
			sumOfSquares += distances[i] * distances[i];
		}
		
		return Math.sqrt(sumOfSquares) / n;
				
	}
	
	@Override
	public boolean haveMatch(IPoint p1, IPoint p2) {
		// TODO Auto-generated method stub
		return false;
	}

	
}
