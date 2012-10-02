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
import org.openmrs.Order;
import org.openmrs.Patient;
import org.openmrs.PatientIdentifier;
import org.openmrs.PersonName;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;

public class OrderToPacsConverterTest {

    private OrderToPacsConverter converter;

    @Before
    public void setup() {
        converter = new OrderToPacsConverter();
    }

	@Test
    public void shouldGeneratePacsMessageFromOrder() throws Exception {
        Order order = new Order();
        order.setAccessionNumber("54321");
        order.setStartDate(new SimpleDateFormat("MM-dd-yyyy").parse("08-08-2008"));
        order.setPatient(createPatient());


        String message = converter.convertToPacsFormat(order, "SC");

        assertThat(message, startsWith("<OrmMessage>"));
        assertThat(message, containsString("<accessionNumber>54321</accessionNumber>"));
		assertThat(message, containsString("<accessionNumber>54321</accessionNumber>"));
		assertThat(message, containsString("<dateOfBirth>197608250000</dateOfBirth>"));
		assertThat(message, containsString("<familyName>Chebaskwony</familyName>"));
		assertThat(message, containsString("<givenName>Collet</givenName>"));
		assertThat(message, containsString("<orderControl>SC</orderControl>"));
		assertThat(message, containsString("<patientId>6TS-4</patientId>"));
		assertThat(message, containsString("<patientSex>F</patientSex>"));
		assertThat(message, containsString("<scheduledExamDatetime>200808080000</scheduledExamDatetime>"));

		/* assertThat(message, containsString("<deviceLocation>E</deviceLocation>"));
		assertThat(message, containsString("<modality>D</modality>"));
		assertThat(message, containsString("<sendingFacility>A</sendingFacility>"));
		assertThat(message, containsString("<universalServiceID>B</universalServiceID>"));
		assertThat(message, containsString("<universalServiceIDText>C</universalServiceIDText>")); */

        assertThat(message, endsWith("</OrmMessage>"));

	}

    private Patient createPatient() throws ParseException {
        PatientIdentifier patientIdentifier = new PatientIdentifier();
        patientIdentifier.setIdentifier("6TS-4");

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
