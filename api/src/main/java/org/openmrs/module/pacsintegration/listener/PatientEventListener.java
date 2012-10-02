package org.openmrs.module.pacsintegration.listener;

import ca.uhn.hl7v2.HL7Exception;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.OpenmrsObject;
import org.openmrs.Patient;
import org.openmrs.api.PatientService;
import org.openmrs.api.context.Context;
import org.openmrs.event.Event;
import org.openmrs.event.SubscribableEventListener;
import org.openmrs.module.pacsintegration.api.PacsIntegrationService;
import org.openmrs.module.pacsintegration.api.converter.PatientToPacsConverter;

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

    protected final Log log = LogFactory.getLog(this.getClass());

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
    public void onMessage(Message message)  {
        Context.openSession();
        try {
            Context.authenticate(LISTENER_USERNAME(), LISTENER_PASSWORD());
            MapMessage mapMessage = (MapMessage) message;
            String action = mapMessage.getString("action");
            String classname = mapMessage.getString("classname");

            boolean isPatient = Patient.class.getName().equals(classname);
            boolean isCreated = CREATED.toString().equals(action);
            boolean isUpdated = UPDATED.toString().equals(action);

            if (isPatient) {
                if (isCreated) {
                    sendToPacs(mapMessage, "A01");
                }
                else if (isUpdated) {
                    sendToPacs(mapMessage, "A08");
                }
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

    private void sendToPacs(MapMessage mapMessage, String messageType) throws JMSException, HL7Exception {
        String uuid = mapMessage.getString("uuid");

        Patient patient = patientService.getPatientByUuid(uuid);
        String pacsHL7 = pacsConverter.convertToPacsFormat(patient, messageType);
        pacsIntegrationService.sendMessageToPacs(pacsHL7);
    }
}
