package com.mongodb.healthcare.fhir.utils;

import ca.uhn.fhir.context.FhirContext;
import org.hl7.fhir.r4.model.*;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;

public class ClaimGenerator {

    private FhirContext fhirContext;

    public ClaimGenerator() {
        fhirContext = FhirContext.forR4();
    }

    public void generateClaim() {
        Claim claim = new Claim();
        Reference patientReference = new Reference();
        patientReference.setId("48d817fe-6786-432f-a0e7-0af585d01348");
        claim.setPatient(patientReference);
        claim.setStatus(Claim.ClaimStatus.ACTIVE);
        claim.setUse(Claim.Use.CLAIM);

        // Priority
        CodeableConcept priority = new CodeableConcept();
        Coding priorityCoding = new Coding();
        priorityCoding.setSystem("http://terminology.hl7.org/CodeSystem/processpriority");
        priorityCoding.setCode("normal");
        List<Coding> priorityList = new ArrayList<>();
        priorityList.add(priorityCoding);
        priority.setCoding(priorityList);
        claim.setPriority(priority);

        // Insurance
        Claim.InsuranceComponent insuranceComponent = new Claim.InsuranceComponent();
        insuranceComponent.setSequence(0);
        List<Claim.InsuranceComponent> insuranceComponents = new ArrayList<Claim.InsuranceComponent>();
        insuranceComponents.add(insuranceComponent);
        claim.setInsurance(insuranceComponents);

        // Billable Period
        LocalDateTime startLocalDateTime = LocalDateTime.now();
        LocalDateTime endLocalDateTime = startLocalDateTime.plusMinutes(30);
        java.util.Date startDate = java.util.Date.from( startLocalDateTime.atZone( ZoneId.systemDefault()).toInstant());
        java.util.Date endDate = java.util.Date.from( endLocalDateTime.atZone( ZoneId.systemDefault()).toInstant());
        claim.setBillablePeriod(new Period().setStart(startDate).setEnd(endDate));

        // Created Date
        claim.setCreated(endDate);

        // Payee
        Claim.PayeeComponent payeeComponent = new Claim.PayeeComponent();
        CodeableConcept payeeCodeableComponent = new CodeableConcept();
        List<Coding> payeeCoding = new ArrayList<>();
        Coding payeeCode = new Coding();
        payeeCode.setCode("provider");
        payeeCoding.add(payeeCode);
        payeeCodeableComponent.setCoding(payeeCoding);
        payeeComponent.setType(payeeCodeableComponent);
        claim.setPayee(payeeComponent);

        // We can now use a parser to encode this resource into a string.
        String encoded = fhirContext.newJsonParser().encodeResourceToString(claim);
        System.out.println(encoded);
    }

    public static void main(String[] args) {
        System.out.println("Generate new claim.");
        new ClaimGenerator().generateClaim();
        System.out.println("New claim generated.");
    }
}
