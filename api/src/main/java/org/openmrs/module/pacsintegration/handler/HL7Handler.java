package org.openmrs.module.pacsintegration.handler;

import org.apache.commons.lang3.StringUtils;
import org.openmrs.Concept;
import org.openmrs.Location;
import org.openmrs.Patient;
import org.openmrs.api.AdministrationService;
import org.openmrs.api.ConceptService;
import org.openmrs.api.LocationService;
import org.openmrs.api.PatientService;
import org.openmrs.api.ProviderService;
import org.openmrs.module.radiologyapp.RadiologyOrder;
import org.openmrs.module.radiologyapp.RadiologyService;
import org.openmrs.module.emrapi.EmrApiProperties;
import org.openmrs.module.pacsintegration.PacsIntegrationConstants;
import org.openmrs.module.pacsintegration.PacsIntegrationException;
import org.openmrs.module.pacsintegration.PacsIntegrationProperties;

import java.util.Collections;
import java.util.List;

abstract public class HL7Handler {

    protected PatientService patientService;

    protected ConceptService conceptService;

    protected AdministrationService adminService;

    protected RadiologyService radiologyService;

    protected LocationService locationService;

    protected ProviderService providerService;

    protected EmrApiProperties emrApiProperties;

    protected PacsIntegrationProperties pacsIntegrationProperties;


    protected Patient getPatient(String patientIdentifier) {
        if (StringUtils.isBlank(patientIdentifier)) {
            throw new PacsIntegrationException("Cannot import message. No patient identifier specified.");
        }

        List<Patient> patientList = patientService.getPatients(null, patientIdentifier,
                Collections.singletonList(emrApiProperties.getPrimaryIdentifierType()), true);

        if (patientList == null || patientList.size() == 0) {
            throw new PacsIntegrationException("Cannot import message. No patient with identifier " + patientIdentifier);
        }

        if (patientList.size() > 1) {
            throw new PacsIntegrationException("Cannot import message. Multiple patients with identifier " + patientIdentifier);
        }

        return patientList.get(0);
    }

    protected RadiologyOrder getRadiologyOrder(String accessionNumber, Patient patient) {

        // if no accession number, no order to associate with
        if (StringUtils.isBlank(accessionNumber)) {
            return null;
        }

        // try to fetch the order by accession number
        RadiologyOrder radiologyOrder = radiologyService.getRadiologyOrderByAccessionNumber(accessionNumber);

        // if there is an existing order, make sure the patients match
        // (note that we are allowing incoming obs with accession numbers that don't match anything in the system)
        if (radiologyOrder != null && !radiologyOrder.getPatient().equals(patient)) {
            throw new PacsIntegrationException("Cannot import message. Patient referenced in message different from patient attached to existing order.");

        }

        return radiologyOrder;
    }

    protected Concept getProcedure(String procedureCode) {
        Concept procedure = conceptService.getConceptByMapping(procedureCode, pacsIntegrationProperties.getProcedureCodesConceptSource().getName());

        if (procedure == null) {
            throw new PacsIntegrationException("Cannot import message. Procedure code not recognized.");
        }

        return procedure;
    }

    protected String getSendingFacility() {
        return adminService.getGlobalProperty(PacsIntegrationConstants.GP_SENDING_FACILITY);
    }

    protected Location getLocationByName(String name) {
        return locationService.getLocation(name);
    }


    /**
     * Setters
     */

    public void setPatientService(PatientService patientService) {
        this.patientService = patientService;
    }

    public void setConceptService(ConceptService conceptService) {
        this.conceptService = conceptService;
    }

    public void setAdminService(AdministrationService adminService) {
        this.adminService = adminService;
    }

    public void setRadiologyService(RadiologyService radiologyService) {
        this.radiologyService = radiologyService;
    }

    public void setLocationService(LocationService locationService) {
        this.locationService = locationService;
    }

    public void setEmrApiProperties(EmrApiProperties emrApiProperties) {
        this.emrApiProperties = emrApiProperties;
    }

    public void setPacsIntegrationProperties(PacsIntegrationProperties pacsIntegrationProperties) {
        this.pacsIntegrationProperties = pacsIntegrationProperties;
    }

    public void setProviderService(ProviderService providerService) {
        this.providerService = providerService;
    }
}
