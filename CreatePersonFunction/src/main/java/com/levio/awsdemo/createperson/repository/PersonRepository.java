package com.levio.awsdemo.createperson.repository;

import com.levio.awsdemo.createperson.dto.PersonDto;
import com.levio.awsdemo.createperson.dto.ProfessionalInfoDto;
import com.levio.awsdemo.createperson.dto.ContactDto;
import com.levio.awsdemo.createperson.dto.PersonalInfoDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;
import software.amazon.awssdk.services.dynamodb.model.DynamoDbException;
import software.amazon.awssdk.services.dynamodb.model.PutItemRequest;
import software.amazon.awssdk.services.dynamodb.model.PutItemResponse;
import software.amazon.awssdk.services.dynamodb.model.ResourceNotFoundException;

import java.util.HashMap;

@RequiredArgsConstructor
@Slf4j
public class PersonRepository {
    private static final String PERSON = "Person";

    private final DynamoDbClient ddb;

    public void save(PersonDto person) {
        PutItemRequest request = PutItemRequest.builder()
                .tableName(PERSON)
                .item(buildItemValues(person))
                .build();

        try {
            PutItemResponse response = ddb.putItem(request);
            log.info(PERSON + " table was successfully updated. The request id is " + response.responseMetadata().requestId());
        } catch (ResourceNotFoundException e) {
            log.error("Resource " + PERSON + " not found", e);
            throw e;
        } catch (DynamoDbException e) {
            log.error("Failed to save", e);
            throw e;
        }
    }

    private HashMap<String, AttributeValue> buildItemValues(PersonDto person) {
        HashMap<String, AttributeValue> itemValues = new HashMap<>();

        PersonalInfoDto personalInfo = person.getPersonalInfo();
        itemValues.put("FirstName", AttributeValue.builder().s(personalInfo.getFirstName()).build());
        itemValues.put("LastName", AttributeValue.builder().s(personalInfo.getLastName()).build());

        ProfessionalInfoDto professionalInfo = person.getProfessionalInfo();
        itemValues.put("CompanyName", AttributeValue.builder().s(professionalInfo.getCompanyName()).build());

        ContactDto contact = professionalInfo.getContact();
        itemValues.put("Email", AttributeValue.builder().s(contact.getEmail()).build());
        itemValues.put("Phone", AttributeValue.builder().s(contact.getPhone()).build());

        return itemValues;
    }

}
