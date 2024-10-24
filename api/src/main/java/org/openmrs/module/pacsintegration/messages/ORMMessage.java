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
package org.openmrs.module.pacsintegration.messages;

import org.openmrs.Order;
import org.openmrs.module.pacsintegration.util.HL7Utils;

/**
 * Models a HL7 ORMMessage
 */

public class ORMMessage extends Message {
	
	private String orderControl;
	private String orderNumber;
	private String patientId;
	private String familyName;
	private String givenName;
	private String dateOfBirth; // YYYYMMDDHHMM
	private String patientSex;
	private String scheduledExamDatetime; // YYYYMMDDHHMM
	private String universalServiceID;
	private String universalServiceIDText;
	private String deviceLocation;
	private String modality;
	private String sendingFacility;

    public ORMMessage(Order order, String orderControl) {
        this.orderControl = orderControl;
        this.orderNumber = order.getOrderNumber();
        this.patientId = order.getPatient().getPatientIdentifier().getIdentifier();
        this.familyName = order.getPatient().getFamilyName();
        this.givenName = order.getPatient().getGivenName();
        this.dateOfBirth = HL7Utils.getHl7DateFormat().format(order.getPatient().getBirthdate());
        this.patientSex = order.getPatient().getGender();
        this.scheduledExamDatetime = HL7Utils.getHl7DateFormat().format(order.getDateActivated());
        // TODO: ormMessage.setDeviceLocation();
        // TODO: ormMessage.setModality();
        // TODO: we should set this to specific identifier that is configured, not just the preferred one?
        // TODO: is the start date the order date?
        // TODO: ormMessage.setSendingFacility();
        // TODO: ormMessage.setUniversalServiceID();
        // TODO; ormMessage.setUniversalServiceIDText();
    }

}
