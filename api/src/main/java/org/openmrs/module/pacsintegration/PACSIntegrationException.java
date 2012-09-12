package org.openmrs.module.pacsintegration;

/**
 * Generic exception thrown by PACS Integration module
 */
public class PACSIntegrationException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public PACSIntegrationException() {
        super();
    }

    public PACSIntegrationException (String message) {
        super(message);
    }

    public PACSIntegrationException (String message, Throwable throwable) {
        super(message, throwable);
    }

}
