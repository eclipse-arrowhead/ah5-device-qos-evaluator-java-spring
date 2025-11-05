package eu.arrowhead.deviceqosevaluator.service.model;

import java.util.Map;

import eu.arrowhead.deviceqosevaluator.enums.OidGroup;
import eu.arrowhead.deviceqosevaluator.enums.OidMetric;

public class OidMetricModel {

	//=================================================================================================
	// members
	
	private final OidGroup group;
	
	private final Map<OidMetric, Double> metricWeight;

	
	//=================================================================================================
	// methods
	
	//-------------------------------------------------------------------------------------------------
	public OidMetricModel(final OidGroup group, final Map<OidMetric, Double> metricWeight) {
		this.group = group;
		this.metricWeight = metricWeight;
	}

	//-------------------------------------------------------------------------------------------------
	public OidGroup getGroup() {
		return group;
	}

	//-------------------------------------------------------------------------------------------------
	public Map<OidMetric, Double> getMetricWeight() {
		return metricWeight;
	}
	
	
}
