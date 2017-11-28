package br.ufsc.trajectoryclassification.model.vo.features;

import br.ufsc.trajectoryclassification.model.vo.description.FeatureComparisonDesc;

public class Nominal implements IFeature<Nominal> {
	
	private String value;
	
	public Nominal(String value) {
		this.value = value;		
	}
	
	public String getValue() {
		return value;
	}
	

	
	@Override
	public String toString() {
		// TODO Auto-generated method stub
		return value;
	}

	@Override
	public double getDistanceTo(Nominal other, FeatureComparisonDesc featureComparisonDesc) {
		
		switch (featureComparisonDesc.getDistance().toLowerCase() ) {
		
		case "equals": return this.value.equals(other.getValue()) ? 0 : 1 ;			
		
		// equalsIgnoreCase
		case "equalsignoreccase": return this.value.equalsIgnoreCase(other.getValue()) ? 0 : 1 ;			
		
		default:
			break;
		}
		
		// TODO Auto-generated method stub
		return -1;
	}

}
