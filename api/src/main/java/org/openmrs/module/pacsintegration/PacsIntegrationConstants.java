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

import java.text.DateFormat;
import java.text.SimpleDateFormat;


public class PacsIntegrationConstants {

    public static String XRAY_MODALITY_CODE = "CR";

    public static String SAME_AS_CONCEPT_MAP_TYPE_UUID = "35543629-7d8c-11e1-909d-c80aa9edcf4e";
	
	public static DateFormat HL7_DATE_FORMAT = new SimpleDateFormat("yyyyMMddHHmm");
	
}
