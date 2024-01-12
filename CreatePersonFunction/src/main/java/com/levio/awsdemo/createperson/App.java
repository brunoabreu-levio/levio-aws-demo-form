package com.levio.awsdemo.createperson;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.levio.awsdemo.createperson.dto.PersonDto;
import com.levio.awsdemo.createperson.dto.ResponseDto;
import com.levio.awsdemo.createperson.service.PersonService;
import software.amazon.lambda.powertools.utilities.EventDeserializationException;
import software.amazon.lambda.powertools.utilities.EventDeserializer;
import software.amazon.lambda.powertools.utilities.JsonConfig;

import java.util.HashMap;
import java.util.Map;

import static java.net.HttpURLConnection.HTTP_BAD_REQUEST;
import static java.net.HttpURLConnection.HTTP_INTERNAL_ERROR;
import static java.net.HttpURLConnection.HTTP_OK;

public class App implements RequestHandler<APIGatewayProxyRequestEvent, APIGatewayProxyResponseEvent> {

    private final ObjectMapper objectMapper = JsonConfig.get().getObjectMapper();
    private final PersonService personService;

    public App() {
        this.personService = new PersonService();
    }

    public App(PersonService personService) {
        this.personService = personService;
    }

    public APIGatewayProxyResponseEvent handleRequest(final APIGatewayProxyRequestEvent input, final Context context) {
        try {
            PersonDto person = EventDeserializer.extractDataFrom(input).as(PersonDto.class);
            personService.save(person);
            return createApiGatewayProxyResponseEvent(person, HTTP_OK);
        } catch (EventDeserializationException e) {
            return createApiGatewayProxyResponseEvent(new ResponseDto(e.getMessage()), HTTP_BAD_REQUEST);
        } catch (Exception e) {
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
        headers.put("Access-Control-Allow-Origin", System.getenv("CORS_ORIGIN"));

        return headers;
    }

}
