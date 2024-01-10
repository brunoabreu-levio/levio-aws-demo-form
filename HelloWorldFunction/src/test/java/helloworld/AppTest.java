package helloworld;

import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import com.fasterxml.jackson.databind.ObjectMapper;
import helloworld.dto.PersonDto;
import helloworld.service.PersonService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import software.amazon.lambda.powertools.utilities.JsonConfig;

import java.io.File;
import java.io.IOException;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
public class AppTest {

    @InjectMocks
    App app;

    @Mock
    PersonService personService;

    @Test
    public void successfulResponse() throws IOException {
        // Setup
        APIGatewayProxyRequestEvent request = new APIGatewayProxyRequestEvent();
        ObjectMapper objectMapper = JsonConfig.get().getObjectMapper();
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

    private void validateHeaders(Map<String, String> headers) {
        assertTrue(headers.containsKey("Content-Type"));
        assertEquals("application/json", headers.get("Content-Type"));
        assertTrue(headers.containsKey("X-Custom-Header"));
        assertEquals("application/json", headers.get("X-Custom-Header"));
    }
}
