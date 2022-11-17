package org.openmrs.module.pacsintegration.runner;

import org.openmrs.api.AdministrationService;
import org.openmrs.api.context.Context;
import org.openmrs.api.context.UsernamePasswordCredentials;

import static org.openmrs.module.pacsintegration.PacsIntegrationConstants.GP_LISTENER_PASSWORD;
import static org.openmrs.module.pacsintegration.PacsIntegrationConstants.GP_LISTENER_USERNAME;

public class PacsUserContextTaskRunner implements ContextTaskRunner {

    private AdministrationService adminService;

    @Override
    public void run(Runnable runnable) {
        try {
            Context.openSession();
            String username = adminService.getGlobalProperty(GP_LISTENER_USERNAME);
            String password = adminService.getGlobalProperty(GP_LISTENER_PASSWORD);
            Context.authenticate(new UsernamePasswordCredentials(username, password));
            runnable.run();
        }
        finally {
            Context.closeSession();
        }
    }

    public void setAdminService(AdministrationService adminService) {
        this.adminService = adminService;
    }
}
