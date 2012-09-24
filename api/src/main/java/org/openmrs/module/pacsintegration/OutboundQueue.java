/*
 * The contents of this file are subject to the OpenMRS Public License
 * Version 1.0 (the "License"); you may not use this file except in
 * compliance with the License. You may obtain a copy of the License at
 * http://license.openmrs.org
 *
 * Software distributed under the License is distributed on an "AS IS"
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. See the
 * License for the specific language governing rights and limitations
 * under the License.
 *
 * Copyright (C) OpenMRS, LLC.  All Rights Reserved.
 */

package org.openmrs.module.pacsintegration;

import org.openmrs.BaseOpenmrsObject;

import java.util.Date;

public class OutboundQueue extends BaseOpenmrsObject {

    private Integer outboundQueueId;

    private String message;

    private Date dateCreated;

    private Boolean processed = false;

    /**
     * Constructor
     */
    public OutboundQueue() {

    }

    public OutboundQueue(String message) {
        this.message = message;
        this.dateCreated = new Date();
        processed = false;
    }

    /**
     * Getters and Setters
     */

    @Override
    public Integer getId() {
        return getOutboundQueueId();
    }

    @Override
    public void setId(Integer id) {
        setOutboundQueueId(id);
    }

    public Integer getOutboundQueueId() {
        return outboundQueueId;
    }

    public void setOutboundQueueId(Integer outboundQueueId) {
        this.outboundQueueId = outboundQueueId;
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
