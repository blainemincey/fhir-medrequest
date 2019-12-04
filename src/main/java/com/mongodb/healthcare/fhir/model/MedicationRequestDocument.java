package com.mongodb.healthcare.fhir.model;

import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Date;

@Document(collection = "medicationRequests")
public class MedicationRequestDocument {
    // ObjectId
    private String id;

    // MedicationRequest Parsed Timestamp
    private Date dateParsed;

    // Patient Id
    private String patientId;

    // Status
    private String status;

    // Intent
    private String intent;

    // Medication
    private String medication;

    // Date authored
    private Date authoredOn;

    // Dr. name
    private String practitionerName;

    // Reason for med
    private String reason;

    // Dosage Instructions
    private String dosageInstructions;

    // Dispense Validity
    private Date dispenseValidityStart;
    private Date dispenseValidityEnd;

    public MedicationRequestDocument() {

    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Date getDateParsed() {
        return dateParsed;
    }

    public void setDateParsed(Date dateParsed) {
        this.dateParsed = dateParsed;
    }

    public String getPatientId() {
        return patientId;
    }

    public void setPatientId(String patientId) {
        this.patientId = patientId;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getIntent() {
        return intent;
    }

    public void setIntent(String intent) {
        this.intent = intent;
    }

    public String getMedication() {
        return medication;
    }

    public void setMedication(String medication) {
        this.medication = medication;
    }

    public Date getAuthoredOn() {
        return authoredOn;
    }

    public void setAuthoredOn(Date authoredOn) {
        this.authoredOn = authoredOn;
    }

    public String getPractitionerName() {
        return practitionerName;
    }

    public void setPractitionerName(String practitionerName) {
        this.practitionerName = practitionerName;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public String getDosageInstructions() {
        return dosageInstructions;
    }

    public void setDosageInstructions(String dosageInstructions) {
        this.dosageInstructions = dosageInstructions;
    }

    public Date getDispenseValidityStart() {
        return dispenseValidityStart;
    }

    public void setDispenseValidityStart(Date dispenseValidityStart) {
        this.dispenseValidityStart = dispenseValidityStart;
    }

    public Date getDispenseValidityEnd() {
        return dispenseValidityEnd;
    }

    public void setDispenseValidityEnd(Date dispenseValidityEnd) {
        this.dispenseValidityEnd = dispenseValidityEnd;
    }

    @Override
    public String toString() {
        return "MedicationRequestDocument{" +
                "id='" + id + '\'' +
                ", dateParsed=" + dateParsed +
                ", patientId='" + patientId + '\'' +
                ", status='" + status + '\'' +
                ", intent='" + intent + '\'' +
                ", medication='" + medication + '\'' +
                ", authoredOn=" + authoredOn +
                ", practitionerName='" + practitionerName + '\'' +
                ", reason='" + reason + '\'' +
                ", dosageInstructions='" + dosageInstructions + '\'' +
                ", dispenseValidityStart=" + dispenseValidityStart +
                ", dispenseValidityEnd=" + dispenseValidityEnd +
                '}';
    }
}
