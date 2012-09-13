package org.openmrs.module.pacsintegration;

import org.openmrs.api.context.Context;

public class PACSIntegrationGlobalProperties {

    public static final String GLOBAL_PROPERTY_MIRTH_IP_ADDRESS() {
        return Context.getAdministrationService().getGlobalProperty("pacsintegration.mirthIpAddress");
    }

    public static final Integer GLOBAL_PROPERTY_MIRTH_INPUT_PORT() {
        return Integer.parseInt(Context.getAdministrationService().getGlobalProperty("pacsintegration.mirthInputPort"));
    }

}
