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

public class PacsIntegrationConstants {

    public static final String GP_PATIENT_IDENTIFIER_TYPE_UUID = "pacsintegration.patientIdentifierTypeUuid";
    public static final String GP_SENDING_FACILITY = "pacsintegration.sendingFacility";
    public static final String GP_PROCEDURE_CODE_CONCEPT_SOURCE_UUID = "pacsintegration.procedureCodeConceptSourceUuid";
    public static final String GP_DEFAULT_LOCALE = "pacsintegration.defaultLocale";
    public static final String GP_LOCATION_CODE_ATTRIBUTE_TYPE_UUID = "pacsintegration.locationCodeAttributeTypeUuid";
    public static final String GP_HL7_LISTENER_PORT = "pacsintegration.hl7ListenerPort";

    public static final String SAME_AS_CONCEPT_MAP_TYPE_UUID = "35543629-7d8c-11e1-909d-c80aa9edcf4e";

    // in McKesson PACS, name fields have a max size of 31 characters (does this include end bit? trim to 30 to be safe)
    public static final Integer MAX_LENGTH_FAMILY_NAME = 30;
    public static final Integer MAX_LENGTH_GIVEN_NAME = 30;

    // in McKesson PACS, order description field has a max size of 65 characters (does this include end bit? trim to 64 to be safe)
    public static final Integer MAX_LENGTH_PROCEDURE_TYPE_DESCRIPTION = 64;

}
