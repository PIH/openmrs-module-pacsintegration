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
package org.openmrs.module.pacsintegration.api.converter;

import ca.uhn.hl7v2.HL7Exception;
import ca.uhn.hl7v2.model.v23.message.ORM_O01;
import ca.uhn.hl7v2.model.v23.segment.MSH;
import ca.uhn.hl7v2.model.v23.segment.ORC;
import ca.uhn.hl7v2.model.v23.segment.PID;
import ca.uhn.hl7v2.parser.Parser;
import ca.uhn.hl7v2.parser.PipeParser;
import org.openmrs.PatientIdentifierType;
import org.openmrs.TestOrder;
import org.openmrs.api.AdministrationService;
import org.openmrs.api.PatientService;
import org.openmrs.module.pacsintegration.PacsIntegrationGlobalProperties;

import java.text.SimpleDateFormat;

public class OrderToPacsConverter {

    private final SimpleDateFormat pacsDateFormat = new SimpleDateFormat("yyyyMMddHHmm");
    private Parser parser = new PipeParser();

    private PatientService patientService;
    private AdministrationService adminService;

    public OrderToPacsConverter(PatientService patientService, AdministrationService adminService) {
        this.patientService = patientService;
        this.adminService = adminService;
    }

    public String convertToPacsFormat(TestOrder order, String orderControl) throws HL7Exception {
        ORM_O01 message = new ORM_O01();

        MSH msh = message.getMSH();
        msh.getFieldSeparator().setValue("|");
        msh.getEncodingCharacters().setValue("^~\\&");
        msh.getMessageType().getMessageType().setValue("ORM");
        msh.getMessageType().getTriggerEvent().setValue("O01");
        msh.getProcessingID().getProcessingID().setValue("P");  // stands for production (?)
        msh.getVersionID().setValue("2.3");
        // TODO: add sending facility

        PID pid = message.getPATIENT().getPID();
        pid.getPatientIDInternalID(0).getID().setValue(order.getPatient().getPatientIdentifier(getPatientIdentifierType()).getIdentifier());
        pid.getPatientName().getFamilyName().setValue(order.getPatient().getFamilyName());
        pid.getPatientName().getGivenName().setValue(order.getPatient().getGivenName());
        pid.getDateOfBirth().getTimeOfAnEvent().setValue(pacsDateFormat.format(order.getPatient().getBirthdate()));
        pid.getSex().setValue(order.getPatient().getGender());

        ORC orc = message.getORDER().getORC();
        orc.getOrderControl().setValue(orderControl);
        orc.getPlacerOrderNumber(0).getEntityIdentifier().setValue("123");
        orc.getFillerOrderNumber().getEntityIdentifier().setValue(order.getAccessionNumber());

        return parser.encode(message);
    }

    private PatientIdentifierType getPatientIdentifierType() {
        PatientIdentifierType patientIdentifierType = patientService.getPatientIdentifierTypeByUuid(adminService.getGlobalProperty(PacsIntegrationGlobalProperties.PATIENT_IDENTIFIER_TYPE_UUID));
        if (patientIdentifierType == null) {
            throw new RuntimeException("No patient identifier type specified. Is pacsintegration.patientIdentifierTypeUuid properly set?");
        }
        else {
            return patientIdentifierType;
        }
    }
}
