package org.openmrs.module.pacsintegration.listener;

import org.openmrs.OpenmrsObject;
import org.openmrs.Patient;
import org.openmrs.api.PatientService;
import org.openmrs.api.context.Context;
import org.openmrs.event.Event;
import org.openmrs.event.SubscribableEventListener;
import org.openmrs.module.pacsintegration.api.PacsIntegrationService;
import org.openmrs.module.pacsintegration.api.PatientToPacsConverter;

import javax.jms.JMSException;
import javax.jms.MapMessage;
import javax.jms.Message;
import java.util.Arrays;
import java.util.List;

import static org.openmrs.event.Event.Action.CREATED;
import static org.openmrs.event.Event.Action.UPDATED;
import static org.openmrs.module.pacsintegration.PacsIntegrationGlobalProperties.LISTENER_PASSWORD;
import static org.openmrs.module.pacsintegration.PacsIntegrationGlobalProperties.LISTENER_USERNAME;


public class PatientEventListener implements SubscribableEventListener {

    private PatientService patientService;
    private PatientToPacsConverter pacsConverter;
    private PacsIntegrationService pacsIntegrationService;


    public PatientEventListener(PatientService patientService, PatientToPacsConverter pacsConverter, PacsIntegrationService pacsIntegrationService) {
        this.patientService = patientService;
        this.pacsConverter = pacsConverter;
        this.pacsIntegrationService = pacsIntegrationService;
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
    public void onMessage(Message message) {
        Context.openSession();
        try {
            Context.authenticate(LISTENER_USERNAME(), LISTENER_PASSWORD());
            MapMessage mapMessage = (MapMessage) message;
            String action = mapMessage.getString("action");
            String classname = mapMessage.getString("classname");

            boolean isPatient = Patient.class.getName().equals(classname);
            boolean isCreated = CREATED.toString().equals(action);
            boolean isUpdated = UPDATED.toString().equals(action);

            if((isCreated || isUpdated) && isPatient)
                sendToPacs(mapMessage);

        } catch (JMSException e) {
            e.printStackTrace();
        } finally {
            Context.closeSession();
        }
    }

    private void sendToPacs(MapMessage mapMessage) throws JMSException {
        String uuid = mapMessage.getString("uuid");
        String sendingFacility = "";  //TODO We need to determine how the sending facility will be retrieved

        Patient patient = patientService.getPatientByUuid(uuid);
        String pacsXML = pacsConverter.convertToPacsFormat(patient, sendingFacility);
        pacsIntegrationService.sendMessageToPacs(pacsXML);
    }
}
