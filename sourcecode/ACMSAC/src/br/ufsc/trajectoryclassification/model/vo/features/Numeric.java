package br.ufsc.trajectoryclassification.model.vo.features;

import br.ufsc.trajectoryclassification.model.vo.description.FeatureComparisonDesc;

public class Numeric implements IFeature<Numeric> {
	
	private double value;
	
	public Numeric(double value) {
		this.value = value;		
	}
	
	public double getValue() {
		return value;
	}
	
		
	@Override
	public String toString() {
		// TODO Auto-generated method stub
		return Double.toString(value);
	}
	
	public double normalizeDistance(double distance, double maxValue ){
		
		/* If maxValue was not defined
		 * */
		if (maxValue == -1)
			return distance;
				
		if (distance >= maxValue)
			return Double.MAX_VALUE;
				
		return distance / maxValue;
		
	}

	@Override
	public double getDistanceTo(Numeric other, FeatureComparisonDesc featureComparisonDesc) {
		
		if (featureComparisonDesc == null)
			return Math.abs(this.value - other.getValue());
		
		/* ------------------------------------------------------------------ */
			
		switch (featureComparisonDesc.getDistance().toLowerCase() ) {
		
		case "difference": return normalizeDistance( Math.abs(this.value - other.getValue()), 
													featureComparisonDesc.getMaxValue());			

		case "proportion":	
			if (this.value == 0 && other.getValue() == 0)
				return 1;
			else 
				return Math.abs(this.value - other.value) / Math.abs(this.value + other.getValue());
		
		default:
			break;
		}
		
		// TODO Auto-generated method stub
		return -1;
	}

}
