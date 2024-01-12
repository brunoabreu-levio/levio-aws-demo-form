package com.levio.awsdemo.createperson.repository;

import com.levio.awsdemo.createperson.dto.ContactDto;
import com.levio.awsdemo.createperson.dto.PersonDto;
import com.levio.awsdemo.createperson.dto.PersonalInfoDto;
import com.levio.awsdemo.createperson.dto.ProfessionalInfoDto;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.function.Executable;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.DynamoDbException;
import software.amazon.awssdk.services.dynamodb.model.DynamoDbResponseMetadata;
import software.amazon.awssdk.services.dynamodb.model.PutItemRequest;
import software.amazon.awssdk.services.dynamodb.model.PutItemResponse;
import software.amazon.awssdk.services.dynamodb.model.ResourceNotFoundException;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PersonRepositoryTest {

    @InjectMocks
    PersonRepository personRepository;

    @Mock
    DynamoDbClient ddb;

    PersonDto person;

    @BeforeEach
    void setUp() {
        person = mock(PersonDto.class);
        PersonalInfoDto personalInfo = mock(PersonalInfoDto.class);
        when(person.getPersonalInfo()).thenReturn(personalInfo);
        ProfessionalInfoDto professionalInfo = mock(ProfessionalInfoDto.class);
        when(person.getProfessionalInfo()).thenReturn(professionalInfo);
        ContactDto contact = mock(ContactDto.class);
        when(professionalInfo.getContact()).thenReturn(contact);
    }

    @Test
    public void givenPersonWhenSaveThenOk() {
        // Setup
        PutItemResponse putItemResponse = mock(PutItemResponse.class);
        DynamoDbResponseMetadata responseMetadata = mock(DynamoDbResponseMetadata.class);
        when(putItemResponse.responseMetadata()).thenReturn(responseMetadata);

        when(ddb.putItem(any(PutItemRequest.class))).thenReturn(putItemResponse);

        // When
        personRepository.save(person);

        // Then
        verify(ddb).putItem(any(PutItemRequest.class));
    }

    @Test
    public void givenNonExistentPersonTableWhenSaveThenResourceNotFound() {
        when(ddb.putItem(any(PutItemRequest.class))).thenThrow(ResourceNotFoundException.class);

        // When
        Executable executable = () -> personRepository.save(person);

        // Then
        Assertions.assertThrows(ResourceNotFoundException.class, executable);
    }

    @Test
    public void givenDynamoDbExceptionWhenSaveThenException() {
        when(ddb.putItem(any(PutItemRequest.class))).thenThrow(DynamoDbException.class);

        // When
        Executable executable = () -> personRepository.save(person);

        // Then
        Assertions.assertThrows(DynamoDbException.class, executable);
    }

}