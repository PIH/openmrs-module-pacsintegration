/*
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

import org.openmrs.Concept;
import org.openmrs.ConceptSource;
import org.openmrs.module.emr.radiology.RadiologyConstants;
import org.openmrs.module.emrapi.EmrApiProperties;
import org.openmrs.module.emrapi.utils.ModuleProperties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

@Component("pacsIntegrationProperties")
public class PacsIntegrationProperties extends ModuleProperties {

    @Autowired
    @Qualifier("emrApiProperties")
    private EmrApiProperties emrApiProperties;

    public void setEmrApiProperties(EmrApiProperties emrApiProperties) {
        this.emrApiProperties = emrApiProperties;
    }

    public ConceptSource getProcedureCodesConceptSource() {
        return getConceptSourceByGlobalProperty(PacsIntegrationConstants.GP_PROCEDURE_CODE_CONCEPT_SOURCE_UUID);
    }

    public Concept getReportTypePrelimConcept() {
        return  conceptService.getConceptByMapping(RadiologyConstants.CONCEPT_CODE_RADIOLOGY_REPORT_PRELIM,
                emrApiProperties.getEmrApiConceptSource().getName());
    }

    public Concept getReportTypeFinalConcept() {
        return  conceptService.getConceptByMapping(RadiologyConstants.CONCEPT_CODE_RADIOLOGY_REPORT_FINAL,
                emrApiProperties.getEmrApiConceptSource().getName());
    }

    public Concept getReportTypeCorrectionConcept() {
        return  conceptService.getConceptByMapping(RadiologyConstants.CONCEPT_CODE_RADIOLOGY_REPORT_CORRECTION,
                emrApiProperties.getEmrApiConceptSource().getName());
    }

    public Integer getHL7ListenerPort() {
        return getIntegerByGlobalProperty(PacsIntegrationConstants.GP_HL7_LISTENER_PORT);
    }
}
