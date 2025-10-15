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
package eu.arrowhead.deviceqosevaluator.quartz.job;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.quartz.QuartzJobBean;
import org.springframework.util.Assert;

import eu.arrowhead.common.Utilities;
import eu.arrowhead.deviceqosevaluator.driver.AugmentedMeasurementDriver;
import eu.arrowhead.deviceqosevaluator.dto.AugmentedMeasurementsDTO;
import eu.arrowhead.deviceqosevaluator.enums.OID;
import eu.arrowhead.deviceqosevaluator.jpa.entity.Device;
import eu.arrowhead.deviceqosevaluator.jpa.service.DeviceDbService;
import eu.arrowhead.deviceqosevaluator.util.Stat;

public class AugmentedMeasurementJob extends QuartzJobBean {

	//=================================================================================================
	// members

	@Autowired
	private DeviceDbService deviceDbService;

	@Autowired
	private AugmentedMeasurementDriver measurementDriver;

	private UUID deviceId;

	private final Logger logger = LogManager.getLogger(this.getClass());

	//=================================================================================================
	// methods

	//-------------------------------------------------------------------------------------------------
	public void setDeviceId(final UUID deviceId) {
		this.deviceId = deviceId;
	}

	//-------------------------------------------------------------------------------------------------
	@Override
	protected void executeInternal(JobExecutionContext context) throws JobExecutionException {
		logger.debug("AugmentedMeasurementJob.executeInternal started");
		Assert.notNull(deviceId, "device id is null");
		System.out.println("Augmented measurement execute: " + deviceId);

		try {
			final Optional<Device> optional = deviceDbService.findById(deviceId);
			if (optional.isEmpty()) {
				logger.error("Device not exists: " + deviceId.toString());
				return;
			}
			final Device device = optional.get();

			if (device.isInactive()) {
				logger.error("Device is inactive: " + deviceId.toString());
				return;
			}
			if (!device.isAugmented()) {
				logger.error("Device is not supporting augmented measurements: " + deviceId.toString());
				return;
			}

			final AugmentedMeasurementsDTO response = measurementDriver.fetch(device.getAddress());
			final Map<OID, List<Double>> aggregated = aggregate(response);

			for (Entry<OID, List<Double>> entry : aggregated.entrySet()) {
				System.out.println(entry.getKey() + ": " + entry.getValue().stream().map(String::valueOf).collect(Collectors.joining(",")));
			}

		} catch (final Exception ex) {
			logger.error("Augmented measurement job failure. Device: " + deviceId.toString() + ", Error: " + ex.getMessage());
			logger.debug(ex);
		}
	}

	//=================================================================================================
	// assistant methods

	//-------------------------------------------------------------------------------------------------
	private Map<OID, List<Double>> aggregate(final AugmentedMeasurementsDTO values) {
		logger.debug("AugmentedMeasurementJob.aggregate started");

		final Map<OID, List<Double>> results = new HashMap<>();

		for (final OID oid : OID.values()) {
			if (values.containsKey(oid.getValue()) && !Utilities.isEmpty(values.get(oid.getValue()))) {
				final double[] array = values.get(oid.getValue()).stream().mapToDouble(Double::doubleValue).toArray();
				final List<Double> aggregated = List.of(Stat.min(array), Stat.max(array), Stat.mean(array), Stat.median(array));
				results.put(oid, aggregated);
			}
		}

		return results;
	}
}
