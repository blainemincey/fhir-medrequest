package com.mongodb.healthcare.fhir.route;


import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.parser.IParser;
import ca.uhn.fhir.parser.LenientErrorHandler;
import com.codahale.metrics.MetricRegistry;
import com.mongodb.healthcare.fhir.db.MyMongoOperations;
import com.mongodb.healthcare.fhir.model.MedicationRequestDocument;
import com.mongodb.healthcare.fhir.parser.R4MedicationRequestProcessor;
import org.apache.camel.CamelContext;
import org.apache.camel.Exchange;
import org.apache.camel.LoggingLevel;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.metrics.routepolicy.MetricsRegistryService;
import org.apache.camel.component.metrics.routepolicy.MetricsRoutePolicyFactory;
import org.apache.camel.spring.boot.CamelContextConfiguration;
import org.hl7.fhir.r4.model.MedicationRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;


@Component
public class FhirMedRequestRoute extends RouteBuilder {

    // Logger component
    private static final Logger logger = LoggerFactory.getLogger(FhirMedRequestRoute.class);

    // FHIR Version
    private static final String FHIR_VERSION_R4 = "R4";

    // Fhir Context for parser
    private static FhirContext fhirContext = FhirContext.forR4();

    // TODO Implement Camel metrics
    private int success = 0;
    private int error = 0;

    @Autowired
    private MyMongoOperations myMongoOperations;

    MetricRegistry metricRegistry;

    // Constructor
    public FhirMedRequestRoute(){
        logger.info("======== Starting FhirMedRequestRoute =========");
    }

    @Override
    public void configure() {

        // Exceptions
        onException(Exception.class)
                .log(LoggingLevel.ERROR, "!! Exception !! - ${exception.message} " +
                        "- unable to process Medication Request")
                .process(exchange -> {
                    error++;
                    this.printStatus();
                })
                .handled(true)
                .useOriginalMessage()
                .to("metrics:counter:medication.request.error.counter?increment=1")
                .setHeader(Exchange.HTTP_RESPONSE_CODE, constant(HttpStatus.BAD_REQUEST))
                .end();

        // Rest service configuration
        // Kept the below in as a reference.  It is already configured in FhirRoute.  Simply re-using.
        restConfiguration()
                .component("restlet")
                .host("{{camel.rest.server}}")
                .port("{{camel.rest.port}}");

        // Rest service path
        // Configured specific to the Med request
        rest("{{camel.rest.path.medrequest}}")
                .post().consumes("application/json")
                .to("direct:postFhirMedRequestMessage");


        // Process FHIR bundle route
        from("direct:processMedicationRequest")
                .routeId("RouteId - Medication Request Processor")
                .log(LoggingLevel.INFO, logger, "Process Medication Request")
                .process(exchange ->  {
                    MedicationRequest medicationRequest = exchange.getIn().getBody(MedicationRequest.class);

                    R4MedicationRequestProcessor medicationRequestProcessor
                            = new R4MedicationRequestProcessor(medicationRequest);
                    exchange.getIn().setBody(medicationRequestProcessor.getMedicationRequestDocument());
                })
                .log("Completed parsing medication request.  Persist to MongoDB.")
                .process(exchange -> {
                    MongoOperations mongoOps =
                            myMongoOperations.getMongoOperations();

                    MedicationRequestDocument insertedMedicationRequestDocument =
                            mongoOps.insert(exchange.getIn().getBody(MedicationRequestDocument.class));

                    String message = "Inserted Medication Request with _id: " + insertedMedicationRequestDocument.getId();

                    logger.info(message);
                    success++;
                    this.printStatus();

                    exchange.getIn().setBody(message);
                })
                .to("metrics:counter:medication.request.success.counter?increment=1")
                .log(LoggingLevel.INFO, logger, "Completed Writing Medication Request to MongoDB.")
                .end();

        // Rest route
        from("direct:postFhirMedRequestMessage")
                .routeId("RouteId - Medication Request REST processor")
                .log("==> Received HTTP Message.")
                .process(exchange -> {
                    String myString = exchange.getIn().getBody(String.class);
                    IParser parser = this.fhirContext.newJsonParser();
                    parser.setParserErrorHandler(new LenientErrorHandler());

                    MedicationRequest medicationRequest
                            = parser.parseResource(MedicationRequest.class, myString);

                    exchange.getIn().setBody(medicationRequest);
                })
                .to("direct:processMedicationRequest");
    }

    @Bean(name = "MedicationRequestProcessor-CamelContext")
    CamelContextConfiguration contextConfiguration() {
        return new CamelContextConfiguration() {
            @Override
            public void beforeApplicationStart(CamelContext camelContext) {
                logger.info("Configuring Camel metrics on all routes.");
                MetricsRoutePolicyFactory metricsRoutePolicyFactory = new MetricsRoutePolicyFactory();
                metricsRoutePolicyFactory.setMetricsRegistry(metricRegistry);
                camelContext.addRoutePolicyFactory(metricsRoutePolicyFactory);
            }

            @Override
            public void afterApplicationStart(CamelContext camelContext) {
                //no-op
            }
        };
    }

    private void printStatus() {
        logger.info("=================");
        logger.info("Success count: " + success);
        logger.info("  Error count: " + error);
        MetricsRegistryService registryService = getContext().hasService(MetricsRegistryService.class);
        if(registryService != null) {
            logger.info(registryService.dumpStatisticsAsJson());
        }
        logger.info("=================");
    }
}