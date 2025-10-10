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

package eu.arrowhead.deviceqosevaluator.quartz;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.quartz.DisallowConcurrentExecution;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import eu.arrowhead.deviceqosevaluator.engine.DeviceCollectorEngine;

@Component
@DisallowConcurrentExecution
public class DeviceCollectorJob implements Job {
	
	//=================================================================================================
	// members
	
	@Autowired
	private DeviceCollectorEngine deviceCollectorEngine;

	private final Logger logger = LogManager.getLogger(this.getClass());
	
	//=================================================================================================
	// methods
	
	//-------------------------------------------------------------------------------------------------
	@Override
	public void execute(final JobExecutionContext context) throws JobExecutionException {
		logger.debug("DeviceCollectorJob.execute started");
		
		try {
			deviceCollectorEngine.refresh();			
		} catch (final Exception ex) {
			logger.error("Device collecting job failure");
			logger.error(ex.getMessage());
			logger.debug(ex);
		}
	}
}
