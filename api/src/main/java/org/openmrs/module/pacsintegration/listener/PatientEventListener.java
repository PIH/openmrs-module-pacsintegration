package org.openmrs.module.pacsintegration.listener;

import ca.uhn.hl7v2.HL7Exception;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.OpenmrsObject;
import org.openmrs.Patient;
import org.openmrs.api.AdministrationService;
import org.openmrs.api.PatientService;
import org.openmrs.api.context.Context;
import org.openmrs.event.Event;
import org.openmrs.event.SubscribableEventListener;
import org.openmrs.module.pacsintegration.api.PacsIntegrationService;
import org.openmrs.module.pacsintegration.converter.PatientToPacsConverter;

import javax.jms.JMSException;
import javax.jms.MapMessage;
import javax.jms.Message;
import java.util.Arrays;
import java.util.List;

import static org.openmrs.event.Event.Action.CREATED;
import static org.openmrs.event.Event.Action.UPDATED;
import static org.openmrs.module.pacsintegration.PacsIntegrationGlobalProperties.LISTENER_PASSWORD;
import static org.openmrs.module.pacsintegration.PacsIntegrationGlobalProperties.LISTENER_USERNAME;


// not currently in use
public class PatientEventListener implements SubscribableEventListener {

    protected final Log log = LogFactory.getLog(this.getClass());

    // TODO: autowire these instead

    private PatientService patientService;
    private PatientToPacsConverter pacsConverter;
    private PacsIntegrationService pacsIntegrationService;
    private AdministrationService admin;


    public PatientEventListener(PatientService patientService, PatientToPacsConverter pacsConverter, PacsIntegrationService pacsIntegrationService, AdministrationService admin) {
        this.patientService = patientService;
        this.pacsConverter = pacsConverter;
        this.pacsIntegrationService = pacsIntegrationService;
        this.admin = admin;
    }


    @Override
    public List<Class<? extends OpenmrsObject>> subscribeToObjects() {
        Object classes = Arrays.asList(Patient.class);
        return (List<Class<? extends OpenmrsObject>>) classes;
    }

    @Override
    public List<String> subscribeToActions() {
        return Arrays.asList(Event.Action.CREATED.name(), Event.Action.UPDATED.name());
    }

    @Override
    public void onMessage(Message message)  {
        Context.openSession();
        try {
            Context.authenticate(admin.getGlobalProperty(LISTENER_USERNAME), admin.getGlobalProperty(LISTENER_PASSWORD));
            MapMessage mapMessage = (MapMessage) message;
            String action = mapMessage.getString("action");
            String classname = mapMessage.getString("classname");

            boolean isPatient = Patient.class.getName().equals(classname);
            if (isPatient) {
                String hl7Message = generateHL7Message(mapMessage, action);
                if(hl7Message != null)
                    pacsIntegrationService.sendMessageToPacs(hl7Message);
            }

        } catch (JMSException e) {
            log.error("Unable to send ADT message to PACS for patient " + message, e);
        } catch (HL7Exception e) {
            log.error("Unable to send ADT message to PACS for patient " + message, e);
        }
        finally {
            Context.closeSession();
        }
    }

    private String generateHL7Message(MapMessage mapMessage, String action) throws JMSException, HL7Exception {
        String pacsHL7 = null;
        String uuid = mapMessage.getString("uuid");
        Patient patient = patientService.getPatientByUuid(uuid);

        boolean isCreated = CREATED.toString().equals(action);
        boolean isUpdated = UPDATED.toString().equals(action);

        if(isCreated) {
             pacsHL7 = pacsConverter.convertToAdmitMessage(patient);
        } else if(isUpdated) {
            pacsHL7 = pacsConverter.convertToUpdateMessage(patient);
        }

        return pacsHL7;
    }
}
