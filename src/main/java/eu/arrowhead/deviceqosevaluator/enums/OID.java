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
package eu.arrowhead.deviceqosevaluator.enums;

public enum OID {

	CPU_TOTAL_LOAD("1.4"), MEMORY_USED("2.1");
	
	private String value;

	private OID(String value) {
		this.value = value;
	}

	public String getValue() {
		return value;
	}
}
