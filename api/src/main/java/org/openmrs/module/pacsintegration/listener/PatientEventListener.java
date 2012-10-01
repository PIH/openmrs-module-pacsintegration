package org.openmrs.module.pacsintegration.listener;

import org.openmrs.OpenmrsObject;
import org.openmrs.Patient;
import org.openmrs.api.PatientService;
import org.openmrs.event.Event;
import org.openmrs.event.SubscribableEventListener;
import org.openmrs.module.pacsintegration.api.PacsIntegrationService;
import org.openmrs.module.pacsintegration.api.PatientToPacsConverter;

import javax.jms.JMSException;
import javax.jms.MapMessage;
import javax.jms.Message;
import java.util.List;

import static org.openmrs.event.Event.Action.CREATED;


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
        return null;
    }

    @Override
    public List<String> subscribeToActions() {
        return null;
    }

    @Override
    public void onMessage(Message message) {
        try {
            MapMessage mapMessage = (MapMessage) message;
            String action = mapMessage.getStringProperty("action");
            String classname = mapMessage.getStringProperty("classname");

            boolean isPatient = Patient.class.getName().equals(classname);
            boolean isCreated = CREATED.toString().equals(action);
            boolean isUpdated = Event.Action.UPDATED.toString().equals(action);

            if((isCreated || isUpdated) && isPatient)
                sendToPacs(mapMessage);

        } catch (JMSException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
    }

    private void sendToPacs(MapMessage mapMessage) throws JMSException {
        String uuid = mapMessage.getStringProperty("uuid");
        String sendingFacility = "";  //TODO We need to determine how the sending facility will be retrieved

        Patient patient = patientService.getPatientByUuid(uuid);
        String pacsXML = pacsConverter.convertToPacsFormat(patient, sendingFacility);
        pacsIntegrationService.sendMessageToPacs(pacsXML);
    }
}
