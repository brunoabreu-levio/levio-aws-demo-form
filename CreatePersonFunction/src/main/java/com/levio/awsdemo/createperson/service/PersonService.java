package com.levio.awsdemo.createperson.service;

import com.levio.awsdemo.createperson.dto.PersonDto;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;
import software.amazon.awssdk.services.dynamodb.model.DynamoDbException;
import software.amazon.awssdk.services.dynamodb.model.PutItemRequest;
import software.amazon.awssdk.services.dynamodb.model.PutItemResponse;
import software.amazon.awssdk.services.dynamodb.model.ResourceNotFoundException;

import java.util.HashMap;

public class PersonService {

    public void save(PersonDto person) {
        try (DynamoDbClient ddb = DynamoDbClient.builder()
                .region(Region.US_WEST_2)
                .build()) {

            HashMap<String, AttributeValue> itemValues = new HashMap<>();
            itemValues.put("FirstName", AttributeValue.builder().s(person.getFirstName()).build());
            itemValues.put("LastName", AttributeValue.builder().s(person.getLastName()).build());
            itemValues.put("CompanyName", AttributeValue.builder().s(person.getCompanyName()).build());
            itemValues.put("Email", AttributeValue.builder().s(person.getEmail()).build());

            PutItemRequest request = PutItemRequest.builder()
                    .tableName("Person")
                    .item(itemValues)
                    .build();

            try {
                PutItemResponse response = ddb.putItem(request);
                System.out.println("Person table was successfully updated. The request id is " + response.responseMetadata().requestId());
            } catch (ResourceNotFoundException e) {
                System.err.println("Be sure that it exists and that you've typed its name correctly!");
                System.exit(1);
            } catch (DynamoDbException e) {
                System.err.println(e.getMessage());
                System.exit(1);
            }
        }
    }
}
