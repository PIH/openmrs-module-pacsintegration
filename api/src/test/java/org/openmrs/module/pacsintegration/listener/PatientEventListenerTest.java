package org.openmrs.module.pacsintegration.listener;

import org.junit.Before;
import org.junit.Test;
import org.openmrs.Patient;
import org.openmrs.api.PatientService;
import org.openmrs.event.Event;
import org.openmrs.module.pacsintegration.api.PacsIntegrationService;
import org.openmrs.module.pacsintegration.api.PatientToPacsConverter;

import javax.jms.JMSException;
import javax.jms.Message;

import static org.mockito.Mockito.*;
import static org.openmrs.event.Event.Action.CREATED;


public class PatientEventListenerTest {

    public static final String UID = "123456";
    private PatientEventListener patientEventListener;
    private PacsIntegrationService pis;
    private PatientToPacsConverter converter;
    private PatientService patientService;
    private static final String SENDING_FACILITY = "";


    @Before
    public void setUp() throws Exception {
        patientService = mock(PatientService.class);
        converter = mock(PatientToPacsConverter.class);
        pis = mock(PacsIntegrationService.class);

        patientEventListener = new PatientEventListener(patientService, converter, pis);
    }

    @Test
    public void shouldSendCreatedPatientToPacs() throws JMSException {
        Message message = new PatientMessage(){{
            setStringProperty("uuid", UID);
            setStringProperty("classname", Patient.class.getName());
            setStringProperty("action", CREATED.toString());
        }};

        Patient patient = new Patient();
        String xml = "";

        when(patientService.getPatientByUuid(UID)).thenReturn(patient);
        when(converter.convertToPacsFormat(patient, SENDING_FACILITY)).thenReturn(xml);

        patientEventListener.onMessage(message);

        verify(pis).sendMessageToPacs(xml);
    }

    @Test
    public void shouldSendUpdatedPatientToPacs() throws JMSException {
        Message message = new PatientMessage(){{
            setStringProperty("uuid", UID);
            setStringProperty("classname", Patient.class.getName());
            setStringProperty("action", Event.Action.UPDATED.toString());
        }};

        Patient patient = new Patient();
        String xml = "";

        when(patientService.getPatientByUuid(UID)).thenReturn(patient);
        when(converter.convertToPacsFormat(patient, SENDING_FACILITY)).thenReturn(xml);

        patientEventListener.onMessage(message);

        verify(pis).sendMessageToPacs(xml);
    }
}
