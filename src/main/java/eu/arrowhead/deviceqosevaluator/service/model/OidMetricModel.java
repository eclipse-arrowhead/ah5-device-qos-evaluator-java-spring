package eu.arrowhead.deviceqosevaluator.service.model;

import java.util.HashMap;
import java.util.Map;

import eu.arrowhead.deviceqosevaluator.DeviceQoSEvaluatorConstants;
import eu.arrowhead.deviceqosevaluator.enums.OidGroup;
import eu.arrowhead.deviceqosevaluator.enums.OidMetric;

public class OidMetricModel {

	//=================================================================================================
	// members
	
	private final OidGroup group;
	private final Map<OidMetric, Double> metricWeight = new HashMap<>();

	
	//=================================================================================================
	// methods
	
	//-------------------------------------------------------------------------------------------------
	public OidMetricModel(final OidGroup group) {
		this.group = group;
	}

	//-------------------------------------------------------------------------------------------------
	public OidGroup getGroup() {
		return group;
	}


	//-------------------------------------------------------------------------------------------------
	public Map<OidMetric, Double> getMetricWeight() {
		return metricWeight;
	}

	//-------------------------------------------------------------------------------------------------
	public Double getScaleTo() {
		if (group == OidGroup.RTT) {
			return Double.valueOf(DeviceQoSEvaluatorConstants.RTT_TIMEOUT);
		}
		
		return null;
	}
}
