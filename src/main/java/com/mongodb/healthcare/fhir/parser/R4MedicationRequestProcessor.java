package com.mongodb.healthcare.fhir.parser;

import com.mongodb.healthcare.fhir.model.MedicationRequestDocument;
import org.hl7.fhir.exceptions.FHIRException;
import org.hl7.fhir.r4.model.MedicationRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class R4MedicationRequestProcessor {

    private static final Logger logger = LoggerFactory.getLogger(R4MedicationRequestProcessor.class);

    private MedicationRequest medicationRequest;
    private MedicationRequestDocument medicationRequestDocument;

    public R4MedicationRequestProcessor(MedicationRequest medicationRequest) {
        logger.info("Processing Medication Request.");

        this.medicationRequest = medicationRequest;

        this.processMedicationRequest();
    }

    private void processMedicationRequest() {
        medicationRequestDocument = new MedicationRequestDocument();

        // Parse values
        medicationRequestDocument.setDateParsed(new java.util.Date());
        medicationRequestDocument.setPatientId(medicationRequest.getSubject().getId());
        medicationRequestDocument.setStatus(medicationRequest.getStatus().getDisplay());
        medicationRequestDocument.setIntent(medicationRequest.getIntent().getDisplay());
        try {
            medicationRequestDocument.setMedication(medicationRequest.getMedicationReference().getDisplay());
        }
        catch(FHIRException fe) {
            logger.error("Exception parsing Medication Request: " + fe);
            medicationRequestDocument.setMedication("ERROR - Invalid Medication");
        }
        medicationRequestDocument.setAuthoredOn(medicationRequest.getAuthoredOn());
        medicationRequestDocument.setPractitionerName(medicationRequest.getRequester().getDisplay());
        medicationRequestDocument.setReason(medicationRequest.getReasonCodeFirstRep().getCodingFirstRep().getDisplay());
        medicationRequestDocument.setDosageInstructions(medicationRequest.getDosageInstructionFirstRep().getText());

        // Dispense validity
        medicationRequestDocument.setDispenseValidityStart(medicationRequest.getDispenseRequest().getValidityPeriod().getStart());
        medicationRequestDocument.setDispenseValidityEnd(medicationRequest.getDispenseRequest().getValidityPeriod().getEnd());
    }

    public MedicationRequestDocument getMedicationRequestDocument() {
        return medicationRequestDocument;
    }
}
