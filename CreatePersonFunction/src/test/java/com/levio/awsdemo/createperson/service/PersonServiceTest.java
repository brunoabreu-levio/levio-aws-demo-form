package com.levio.awsdemo.createperson.service;

import com.levio.awsdemo.createperson.dto.PersonDto;
import com.levio.awsdemo.createperson.dto.PersonalInfoDto;
import com.levio.awsdemo.createperson.repository.PersonRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class PersonServiceTest {
    private static final String ANONYMOUS = "Anonymous";

    @InjectMocks
    PersonService personService;

    @Mock
    PersonRepository personRepository;

    @Test
    public void givenValidPersonWithPersonalInfoConsentedWhenSaveThenOk() {
        // Given
        PersonDto person = mock(PersonDto.class);
        PersonalInfoDto personalInfo = mock(PersonalInfoDto.class);
        when(person.isPersonalInfoConsented()).thenReturn(true);

        // When
        personService.save(person);

        // Then
        verify(personRepository).save(person);
        verify(personalInfo, never()).setFirstName(ANONYMOUS);
        verify(personalInfo, never()).setLastName(ANONYMOUS);
    }

    @Test
    public void givenValidPersonWithNoPersonalInfoConsentedWhenSaveThenOk() {
        // Given
        PersonDto person = mock(PersonDto.class);
        PersonalInfoDto personalInfo = mock(PersonalInfoDto.class);
        when(person.isPersonalInfoConsented()).thenReturn(false);
        when(person.getPersonalInfo()).thenReturn(personalInfo);

        // When
        personService.save(person);

        // Then
        verify(personRepository).save(person);
        verify(personalInfo).setFirstName(ANONYMOUS);
        verify(personalInfo).setLastName(ANONYMOUS);
    }
}