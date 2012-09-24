/**
 * The contents of this file are subject to the OpenMRS Public License
 * Version 1.0 (the "License"); you may not use this file except in
 * compliance with the License. You may obtain a copy of the License at
 * http://license.openmrs.org
 *
 * Software distributed under the License is distributed on an "AS IS"
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. See the
 * License for the specific language governing rights and limitations
 * under the License.
 *
 * Copyright (C) OpenMRS, LLC.  All Rights Reserved.
 */
package org.openmrs.module.pacsintegration;

import org.apache.commons.lang.StringUtils;
import org.openmrs.Order;

import com.thoughtworks.xstream.XStream;

public class ConversionUtils {
	
	/**
	 * Given an order and an order control code, creates an OrmMessage object
	 */
	public static OrmMessage createORMMessage(Order order, String orderControl) {
		
		// TODO: better handle the missing values that would cause NPE
		
		if (order == null) {
			throw new PacsIntegrationException("Order cannot be null");
		}
		
		if (StringUtils.isEmpty(orderControl)) {
			// default to a new order
			orderControl = "NW";
		}
		
		OrmMessage ormMessage = new OrmMessage();
		
		ormMessage.setAccessionNumber(order.getAccessionNumber());
		ormMessage.setDateOfBirth(PacsIntegrationConstants.hl7DateFormat.format(order.getPatient().getBirthdate()));
		// TODO: ormMessage.setDeviceLocation();
		ormMessage.setFamilyName(order.getPatient().getFamilyName());
		ormMessage.setGivenName(order.getPatient().getGivenName());
		// TODO: ormMessage.setModality();
		ormMessage.setOrderControl(orderControl);
		// TODO: we should set this to specific identifier that is configured, not just the preferred one?
		ormMessage.setPatientId(order.getPatient().getPatientIdentifier().getIdentifier());
		ormMessage.setPatientSex(order.getPatient().getGender());
		// TODO: is the start date the order date?
		ormMessage.setScheduledExamDatetime(PacsIntegrationConstants.hl7DateFormat.format(order.getStartDate()));
		// TODO: ormMessage.setSendingFacility();
		// TODO: ormMessage.setUniversalServiceID();
		// TODO; ormMessage.setUniversalServiceIDText();
		
		return ormMessage;
	}
	
	/**
	 * Performs basic serialization using Xstream
	 */
	public static String serialize(Object obj) {
		
		if (obj == null) {
			throw new PacsIntegrationException("Item to serialize cannot be null");
		}
		
		// set up aliases for our classes so we don't get the whole package names
		XStream xstream = new XStream();
		xstream.alias("OrmMessage", OrmMessage.class);
		
		return xstream.toXML(obj);
	}
}
