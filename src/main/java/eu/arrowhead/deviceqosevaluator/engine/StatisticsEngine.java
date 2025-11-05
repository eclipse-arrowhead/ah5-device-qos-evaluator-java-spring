/*******************************************************************************
 *
 * Copyright (c) 2025 AITIA
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 *
 * http://www.eclipse.org/legal/epl-2.0.
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *  	AITIA - implementation
 *  	Arrowhead Consortia - conceptualization
 *
 *******************************************************************************/
package eu.arrowhead.deviceqosevaluator.engine;

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.UUID;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import eu.arrowhead.common.Utilities;
import eu.arrowhead.deviceqosevaluator.DeviceQoSEvaluatorSystemInfo;
import eu.arrowhead.deviceqosevaluator.enums.OidGroup;
import eu.arrowhead.deviceqosevaluator.enums.OidMetric;
import eu.arrowhead.deviceqosevaluator.jpa.entity.System;
import eu.arrowhead.deviceqosevaluator.jpa.entity.mapped.StatEntity;
import eu.arrowhead.deviceqosevaluator.jpa.service.StatDbService;
import eu.arrowhead.deviceqosevaluator.jpa.service.SystemDbService;
import eu.arrowhead.deviceqosevaluator.service.model.OidMetricModel;
import eu.arrowhead.deviceqosevaluator.service.model.SystemEvalModel;
import eu.arrowhead.deviceqosevaluator.util.Stat;

@Service
public class StatisticsEngine {

	//=================================================================================================
	// members

	@Autowired
	private DeviceQoSEvaluatorSystemInfo sysInfo;
	
	@Autowired
	private SystemDbService sysDbService;
	
	@Autowired
	private StatDbService statDbService;

	private final Logger logger = LogManager.getLogger(this.getClass());

	//=================================================================================================
	// methods

	//-------------------------------------------------------------------------------------------------
	public List<SystemEvalModel> evaluate(final Set<String> systemNames, final List<OidMetricModel> metrics) {
		logger.debug("evaluate started");

		final Map<UUID, List<SystemEvalModel>> deviceMap = new HashMap<>(systemNames.size());
		final List<SystemEvalModel> unknownList = new ArrayList<>();

		final List<System> systemRecords = sysDbService.findByNames(systemNames);
		for (final String sysName : systemNames) {
			boolean found = false;
			for (final System sysRecord : systemRecords) {
				if (sysName.equalsIgnoreCase(sysRecord.getName())) {
					deviceMap.putIfAbsent(sysRecord.getDevice().getId(), new ArrayList<>());
					deviceMap.get(sysRecord.getDevice().getId()).add(new SystemEvalModel(sysName));
					found = true;
					break;
				}

				if (!found) {
					final SystemEvalModel evalModel = new SystemEvalModel(sysName);
					for (final OidMetricModel metric : metrics) {
						evalModel.addNoStat(metric.getGroup());
					}
					unknownList.add(evalModel);
				}
			}
		}
		
		final ZonedDateTime now = Utilities.utcNow();
		final ZonedDateTime afterTimestamp = now.minusSeconds(sysInfo.getEvaluationTimeWindow());

		final List<SystemEvalModel> result = new ArrayList<>(systemNames.size());
		for (final Entry<UUID, List<SystemEvalModel>> entry : deviceMap.entrySet()) {
			final Set<OidGroup> noStat = new HashSet<>();
			final double score = calculateDeviceMetrics(entry.getKey(), metrics, now, afterTimestamp, noStat);
			entry.getValue().forEach(sys -> { 
				sys.setScore(score); 
				result.add(sys);
			});
		}
		result.addAll(unknownList);

		return result;
	}

	//=================================================================================================
	// assistant methods

	//-------------------------------------------------------------------------------------------------
	private double calculateDeviceMetrics(final UUID deviceId, final List<OidMetricModel> metrics, final ZonedDateTime now, final ZonedDateTime afterTimestamp, final Set<OidGroup> noStat) {
		logger.debug("calculateDeviceMetrics started");
		
		double score = 0;
		for (final OidMetricModel metricModel : metrics) {
			final List<StatEntity> data = statDbService.getByDeviceIdUntilTimestamp(metricModel.getGroup(), deviceId, afterTimestamp);
			if (Utilities.isEmpty(data)) {
				noStat.add(metricModel.getGroup());
			} else {
				data.sort(Comparator.comparing(StatEntity::getTimestamp)); // ascending				
			}
			
			int missingData = 0;			
			final long step = metricModel.getGroup() == OidGroup.RTT ? sysInfo.getRttMeasurementJobInterval() : sysInfo.getAugmentedMeasurementJobInterval();
			ZonedDateTime head = !Utilities.isEmpty(data) ? data.getLast().getTimestamp().plusSeconds(step) : afterTimestamp;
			while (!head.isAfter(now)) {
				missingData++;
				head = head.plusSeconds(step);
			}
			
			for (final Entry<OidMetric, Double> metricEntry : metricModel.getMetricWeight().entrySet()) {
				score = score + calculateMetricScore(metricEntry.getKey(), metricEntry.getValue(), data, missingData, metricModel.getGroup().getWorstStat());
			}

		}
		
		return score;
	}
	
	//-------------------------------------------------------------------------------------------------
	private double calculateMetricScore(final OidMetric metric, final Double weight, final List<StatEntity> data, final int missingData, final double worstStat) {
		logger.debug("calculateMetricScore started");
		
		if (metric == OidMetric.CURRENT) {
			if (missingData > 0) {
				return worstStat * weight;				
			}
			return data.getLast().getCurrent() * weight;
		}
		
		final double[] values = new double[data.size() + missingData];
		for (int i = 0; i < values.length; ++i) {
			values[i] = getValue(metric, data, i, worstStat);			
		}
		
		switch (metric) {
		case MIN:
			return Stat.min(values) * weight;
			
		case MAX:
			return Stat.max(values) * weight;
			
		case MEAN:
			return Stat.mean(values) * weight;
			
		case MEDIAN:
			return Stat.median(values) * weight;
			
		default:
			logger.error("Unhandled OID metric: " + metric);
			return 0;
		}
	}
	
	//-------------------------------------------------------------------------------------------------
	private double getValue(final OidMetric metric, final List<StatEntity> data, final int idx, final double worstStat) {
		logger.debug("getValue started");
		
		if (idx > data.size() - 1) {
			return worstStat;
		}
		
		switch (metric) {
		case MIN:
			return data.get(idx).getMinimum();
			
		case MAX:
			return data.get(idx).getMaximum();
			
		case MEAN:
			return data.get(idx).getMean();
			
		case MEDIAN:
			return data.get(idx).getMedian();
			
		default:
			return worstStat;
		}
	}
}
