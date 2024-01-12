package com.levio.awsdemo.createperson;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.levio.awsdemo.createperson.dto.PersonDto;
import com.levio.awsdemo.createperson.dto.ResponseDto;
import com.levio.awsdemo.createperson.repository.PersonRepository;
import com.levio.awsdemo.createperson.service.PersonService;
import lombok.extern.slf4j.Slf4j;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.lambda.powertools.utilities.EventDeserializationException;
import software.amazon.lambda.powertools.utilities.EventDeserializer;
import software.amazon.lambda.powertools.utilities.JsonConfig;

import java.util.HashMap;
import java.util.Map;

import static java.net.HttpURLConnection.HTTP_BAD_REQUEST;
import static java.net.HttpURLConnection.HTTP_INTERNAL_ERROR;
import static java.net.HttpURLConnection.HTTP_OK;

@Slf4j
public class App implements RequestHandler<APIGatewayProxyRequestEvent, APIGatewayProxyResponseEvent> {

    private final ObjectMapper objectMapper = JsonConfig.get().getObjectMapper();
    private final PersonService personService;

    private final String corsOrigin;

    public App() {
        try (DynamoDbClient ddb = DynamoDbClient.builder().region(Region.US_WEST_2).build()) {
            PersonRepository personRepository = new PersonRepository(ddb);
            this.personService = new PersonService(personRepository);
            this.corsOrigin = System.getenv("CORS_ORIGIN");
        }
    }

    public App(PersonService personService, String corsOrigin) {
        this.personService = personService;
        this.corsOrigin = corsOrigin;
    }

    public APIGatewayProxyResponseEvent handleRequest(final APIGatewayProxyRequestEvent input, final Context context) {
        try {
            log.info("New person.");
            PersonDto person = EventDeserializer.extractDataFrom(input).as(PersonDto.class);
            personService.save(person);
            log.info("Save successfully.");
            return createApiGatewayProxyResponseEvent(null, HTTP_OK);
        } catch (EventDeserializationException e) {
            log.error("Deserialization failed.", e);
            return createApiGatewayProxyResponseEvent(new ResponseDto(e.getMessage()), HTTP_BAD_REQUEST);
        } catch (Exception e) {
            log.error("Internal error.", e);
            return createApiGatewayProxyResponseEvent(new ResponseDto(e.getMessage()), HTTP_INTERNAL_ERROR);
        }
    }

    private APIGatewayProxyResponseEvent createApiGatewayProxyResponseEvent(Object body, int httpStatusCode) {
        APIGatewayProxyResponseEvent response = new APIGatewayProxyResponseEvent()
                .withHeaders(getHeaders())
                .withStatusCode(httpStatusCode);

        if (body == null) {
            return response;
        }
        try {
            return response
                    .withBody(objectMapper.writeValueAsString(body));
        } catch (JsonProcessingException e) {
            return response
                    .withStatusCode(HTTP_INTERNAL_ERROR)
                    .withBody("{\"message\":\"" + e.getMessage() + "\"}");
        }
    }

    private Map<String, String> getHeaders() {
        Map<String, String> headers = new HashMap<>();
        headers.put("Content-Type", "application/json");
        headers.put("X-Custom-Header", "application/json");
        headers.put("Access-Control-Allow-Origin", corsOrigin);

        return headers;
    }

}
