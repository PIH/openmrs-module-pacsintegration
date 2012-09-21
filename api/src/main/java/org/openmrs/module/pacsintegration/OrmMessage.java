/**
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

import java.io.Serializable;
import org.openmrs.BaseOpenmrsObject;
import org.openmrs.BaseOpenmrsMetadata;
import org.simpleframework.xml.Element;
import org.simpleframework.xml.Root;

/**
 * Models a HL7 OrmMessage
 */

public class OrmMessage extends Message {

    private String sendingFacility;

    private String patientId;

    private String familyName;

    private String givenName;

    private String dateOfBirth;            // YYYYMMDDHHMM

    private String patientSex;

    private String orderControl;

    private String accessionNumber;

    private String universalServiceID;

    private String universalServiceIDText;

    private String deviceLocation;

    private String modality;

    private String scheduledExamDatetime;    // YYYYMMDDHHMM


    /**
     * Getters and Setters
     */

    public String getSendingFacility() {
        return sendingFacility;
    }

    public void setSendingFacility(String sendingFacility) {
        this.sendingFacility = sendingFacility;
    }

    public String getPatientId() {
        return patientId;
    }

    public void setPatientId(String patientId) {
        this.patientId = patientId;
    }

    public String getFamilyName() {
        return familyName;
    }

    public void setFamilyName(String familyName) {
        this.familyName = familyName;
    }

    public String getGivenName() {
        return givenName;
    }

    public void setGivenName(String givenName) {
        this.givenName = givenName;
    }

    public String getDateOfBirth() {
        return dateOfBirth;
    }

    public void setDateOfBirth(String dateOfBirth) {
        this.dateOfBirth = dateOfBirth;
    }

    public String getPatientSex() {
        return patientSex;
    }

    public void setPatientSex(String patientSex) {
        this.patientSex = patientSex;
    }

    public String getOrderControl() {
        return orderControl;
    }

    public void setOrderControl(String orderControl) {
        this.orderControl = orderControl;
    }

    public String getAccessionNumber() {
        return accessionNumber;
    }

    public void setAccessionNumber(String accessionNumber) {
        this.accessionNumber = accessionNumber;
    }

    public String getUniversalServiceID() {
        return universalServiceID;
    }

    public void setUniversalServiceID(String universalServiceID) {
        this.universalServiceID = universalServiceID;
    }

    public String getUniversalServiceIDText() {
        return universalServiceIDText;
    }

    public void setUniversalServiceIDText(String universalServiceIDText) {
        this.universalServiceIDText = universalServiceIDText;
    }

    public String getDeviceLocation() {
        return deviceLocation;
    }

    public void setDeviceLocation(String deviceLocation) {
        this.deviceLocation = deviceLocation;
    }

    public String getModality() {
        return modality;
    }

    public void setModality(String modality) {
        this.modality = modality;
    }

    public String getScheduledExamDatetime() {
        return scheduledExamDatetime;
    }

    public void setScheduledExamDatetime(String scheduledExamDatetime) {
        this.scheduledExamDatetime = scheduledExamDatetime;
    }
}