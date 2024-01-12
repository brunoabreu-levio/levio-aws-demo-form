package com.levio.awsdemo.createperson;

import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.levio.awsdemo.createperson.dto.PersonDto;
import com.levio.awsdemo.createperson.service.PersonService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import software.amazon.lambda.powertools.utilities.JsonConfig;

import java.io.File;
import java.io.IOException;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

@ExtendWith(MockitoExtension.class)
public class AppTest {
    App app;

    @Mock
    PersonService personService;

    String corsOrigin = "*";

    APIGatewayProxyRequestEvent request = new APIGatewayProxyRequestEvent();

    ObjectMapper objectMapper = JsonConfig.get().getObjectMapper();

    @BeforeEach
    void setUp() {
        app = new App(personService, corsOrigin);
    }

    @Test
    public void givenValidPersonWhenHandleRequestThenOk() throws IOException {
        // Setup
        PersonDto person = objectMapper.readValue(new File("src/test/resources/person.json"), PersonDto.class);

        // Given
        request.setBody(objectMapper.writeValueAsString(person));

        // When
        APIGatewayProxyResponseEvent responseEvent = app.handleRequest(request, null);

        // Then
        assertEquals(200, responseEvent.getStatusCode().intValue());
        validateHeaders(responseEvent.getHeaders());
        verify(personService).save(person);
    }

    @Test
    public void givenInvalidPersonWhenHandleRequestThenBadRequest() throws IOException {
        // Given
        request.setBody(objectMapper.writeValueAsString(""));

        // When
        APIGatewayProxyResponseEvent responseEvent = app.handleRequest(request, null);

        // Then
        assertEquals(400, responseEvent.getStatusCode().intValue());
        validateHeaders(responseEvent.getHeaders());
        verifyNoInteractions(personService);
    }

    @Test
    public void givenExceptionWhenHandleRequestThenInternalServerError() throws IOException {
        // Setup
        PersonDto person = objectMapper.readValue(new File("src/test/resources/person.json"), PersonDto.class);
        request.setBody(objectMapper.writeValueAsString(person));

        // Given
        doThrow(new RuntimeException("Internal Server Error")).when(personService).save(person);

        // When
        APIGatewayProxyResponseEvent responseEvent = app.handleRequest(request, null);

        // Then
        assertEquals(500, responseEvent.getStatusCode().intValue());
        validateHeaders(responseEvent.getHeaders());
        verify(personService).save(person);
    }

    private void validateHeaders(Map<String, String> headers) {
        assertTrue(headers.containsKey("Content-Type"));
        assertEquals("application/json", headers.get("Content-Type"));
        assertTrue(headers.containsKey("X-Custom-Header"));
        assertEquals("application/json", headers.get("X-Custom-Header"));
        assertTrue(headers.containsKey("Access-Control-Allow-Origin"));
        assertEquals(corsOrigin, headers.get("Access-Control-Allow-Origin"));
    }
}
