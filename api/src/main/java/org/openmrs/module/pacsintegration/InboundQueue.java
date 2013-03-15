package org.openmrs.module.pacsintegration;

import java.util.Date;

public class InboundQueue {

    private Integer inboundQueueId;

    private String message;

    private Date dateCreated;

    private Boolean processed = false;

    /**
     * Getters and Setters
     */

    public Integer getInboundQueueId() {
        return inboundQueueId;
    }

    public void setInboundQueueId(Integer inboundQueueId) {
        this.inboundQueueId = inboundQueueId;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Date getDateCreated() {
        return dateCreated;
    }

    public void setDateCreated(Date dateCreated) {
        this.dateCreated = dateCreated;
    }

    public Boolean getProcessed() {
        return processed;
    }

    public void setProcessed(Boolean processed) {
        this.processed = processed;
    }
}
