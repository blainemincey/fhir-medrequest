package com.mongodb.healthcare.fhir;

import ca.uhn.fhir.context.FhirContext;
import com.mongodb.healthcare.fhir.db.MyMongoOperations;
import com.mongodb.healthcare.fhir.utils.RandomMedGenerator;
import com.mongodb.healthcare.fhir.model.MyPatientModel;
import org.hl7.fhir.r4.model.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.data.mongodb.core.query.Query;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@SpringBootApplication
public class MedicationRequestApplication implements CommandLineRunner {

    // Logger component
    private static final Logger logger = LoggerFactory.getLogger(MedicationRequestApplication.class);

    @Autowired
    private MyMongoOperations myMongoOperations;

    @Value("${file.endpoint.medicationrequests}")
    private String outputDir;

    private FhirContext fhirContext;

    private RandomMedGenerator randomMedGenerator = new RandomMedGenerator();

    /**
     *
     */
    public MedicationRequestApplication() {
        fhirContext = FhirContext.forR4();
    }

    public void generateMedicationRequest(String patientId, String practitionerName) {
        // Resource Type
        MedicationRequest medicationRequest = new MedicationRequest();
        RandomMedGenerator.Medicine medication = this.randomMedGenerator.getMedicine();

        // Generate unique id
        UUID uuid = UUID.randomUUID();
        medicationRequest.setId(uuid.toString());

        // Medication
        Reference medicationReference = new Reference();
        medicationReference.setDisplay(medication.getName());
        medicationRequest.setMedication(medicationReference);

        // Dosage
        Dosage dosage = new Dosage();
        dosage.setText(medication.getDosage());
        List<Dosage> dosageList = new ArrayList<>();
        dosageList.add(dosage);
        medicationRequest.setDosageInstruction(dosageList);

        // Status and Intent
        medicationRequest.setStatus(MedicationRequest.MedicationRequestStatus.ACTIVE);
        medicationRequest.setIntent(MedicationRequest.MedicationRequestIntent.ORDER);

        // Patient ref
        Reference patientReference = new Reference();
        patientReference.setId(patientId);
        medicationRequest.setSubject(patientReference);

        // Authored Date
        medicationRequest.setAuthoredOn(new java.util.Date());

        // Requestor
        Reference providerReference = new Reference();
        providerReference.setReference("Practitioner");
        providerReference.setDisplay(practitionerName);
        medicationRequest.setRequester(providerReference);

        // Reason
        CodeableConcept reason = new CodeableConcept();
        Coding reasonCoding = new Coding();
        reasonCoding.setSystem("http://snomed.info/sct");
        reasonCoding.setCode("123456789");
        reasonCoding.setDisplay(medication.getCondition());
        List<Coding> reasonList = new ArrayList<>();
        reasonList.add(reasonCoding);
        reason.setCoding(reasonList);
        List<CodeableConcept> ccList = new ArrayList<>();
        ccList.add(reason);
        medicationRequest.setReasonCode(ccList);

        // Dispense Validity Period
        MedicationRequest.MedicationRequestDispenseRequestComponent dispenseRequestComponent =
                new MedicationRequest.MedicationRequestDispenseRequestComponent();
        LocalDateTime startLocalDateTime = LocalDateTime.now();
        LocalDateTime endLocalDateTime = startLocalDateTime.plusYears(1);
        java.util.Date startDate = java.util.Date.from( startLocalDateTime.atZone( ZoneId.systemDefault()).toInstant());
        java.util.Date endDate = java.util.Date.from( endLocalDateTime.atZone( ZoneId.systemDefault()).toInstant());
        dispenseRequestComponent.setValidityPeriod(new Period().setStart(startDate).setEnd(endDate));
        medicationRequest.setDispenseRequest(dispenseRequestComponent);

        // We can now use a parser to encode this resource into a string.
        String encoded = fhirContext.newJsonParser().encodeResourceToString(medicationRequest);

        this.writeMedRequestFile(encoded, uuid.toString());
    }

    /**
     *
     */
    private void getRandomPatients() {

        // Grab a few patients
        MongoOperations mongoOps =
                myMongoOperations.getMongoOperations();
        Query query = new Query();
        query.limit(25);

        // TODO - Need to update the spelling of practitioners
        query.fields().include("patientId").include("practioners");

        List<MyPatientModel> patients = mongoOps.find(query,MyPatientModel.class);

        // Generate FHIR Medication Requests
        for (MyPatientModel patient : patients ) {
            String patientId = patient.getPatientId();
            String prefix = patient.getPractioners().get(0).getPrefix();
            String firstName = patient.getPractioners().get(0).getFirstName();
            String lastName = patient.getPractioners().get(0).getLastName();

            String practitionerName = prefix + " " + firstName + " " + lastName;

            this.generateMedicationRequest(patientId, practitionerName);
        }
    }

    /**
     *
     * @param medRequest
     */
    private void writeMedRequestFile(String medRequest, String uuid) {
        logger.info("Writing Med Request Output file.");

        try {

            Path filePath = FileSystems.getDefault().getPath(this.outputDir, uuid + ".json");

            Files.writeString(filePath.toAbsolutePath(), medRequest, StandardCharsets.UTF_8);

        } catch (IOException ioe) {
            logger.error("Error writing Medication Request JSON file: " + ioe);
        }

    }

    @Override
    public void run(String... args) throws Exception {
        this.getRandomPatients();
    }

    public static void main(String[] args) {
        SpringApplication.run(MedicationRequestApplication.class, args);
    }
}
