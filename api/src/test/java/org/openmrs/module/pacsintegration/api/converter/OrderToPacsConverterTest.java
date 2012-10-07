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

import org.junit.Before;
import org.junit.Test;
import org.openmrs.*;
import org.openmrs.api.AdministrationService;
import org.openmrs.api.PatientService;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class OrderToPacsConverterTest {

    private OrderToPacsConverter converter;

    private PatientIdentifierType patientIdentifierType = new PatientIdentifierType();


    @Before
    public void setup() {
        PatientService patientService = mock(PatientService.class);
        AdministrationService administrationService = mock(AdministrationService.class);
        when(patientService.getPatientIdentifierTypeByUuid(anyString())).thenReturn(patientIdentifierType);

        converter = new OrderToPacsConverter(patientService, administrationService);
    }

    @Test
    public void shouldGenerateMessageFromAnOrder() throws Exception {
        Order order = new Order();
        order.setAccessionNumber("54321");
        order.setStartDate(new SimpleDateFormat("MM-dd-yyyy").parse("08-08-2008"));
        order.setPatient(createPatient());

        String hl7Message = converter.convertToPacsFormat(order, "SC");

        assertThat(hl7Message, startsWith("MSH|^~\\&|||||||ORM^O01||P|2.3\r"));
        assertThat(hl7Message, containsString("PID|||6TS-4||Chebaskwony^Collet||197608250000|F\r"));
        assertThat(hl7Message, endsWith("ORC|SC|123|54321\r"));
    }


    private Patient createPatient() throws ParseException {
        PatientIdentifier patientIdentifier = new PatientIdentifier();
        patientIdentifier.setIdentifier("6TS-4");
        patientIdentifier.setIdentifierType(patientIdentifierType);

        PersonName patientName = new PersonName();
        patientName.setFamilyName("Chebaskwony");
        patientName.setGivenName("Collet");

        Patient patient = new Patient();
        patient.addIdentifier(patientIdentifier);
        patient.addName(patientName);
        Date birthDate = new SimpleDateFormat("MM-dd-yyyy").parse("08-25-1976");
        patient.setBirthdate(birthDate);
        patient.setGender("F");
        return patient;
    }

}
