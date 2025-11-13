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
package eu.arrowhead.deviceqosevaluator.dto;

import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;

import eu.arrowhead.deviceqosevaluator.jpa.service.model.StatQueryResultModel;
import eu.arrowhead.dto.QoSDeviceStatQueryResponseDTO;
import eu.arrowhead.dto.QoSDeviceStatRecordDTO;

@Service
public class DTOConverter {

	//=================================================================================================
	// methods

	//-------------------------------------------------------------------------------------------------
	public QoSDeviceStatQueryResponseDTO convertStatQueryResultModelPageToDTO(final Page<StatQueryResultModel> page) {
		return new QoSDeviceStatQueryResponseDTO(page.stream().map(m -> convertStatQueryResultModelToDTO(m)).toList(), page.getTotalElements());
	}

	//-------------------------------------------------------------------------------------------------
	public QoSDeviceStatRecordDTO convertStatQueryResultModelToDTO(final StatQueryResultModel model) {
		return new QoSDeviceStatRecordDTO(
				model.group().name(),
				model.stat().getId(),
				model.stat().getUuid().toString(),
				model.stat().getMinimum(), model.stat().getMaximum(),
				model.stat().getMean(),
				model.stat().getMedian(),
				model.stat().getCurrent(),
				model.system().stream().map((sys -> sys.getName())).toList());
	}
}
