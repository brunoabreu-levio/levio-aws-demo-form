package com.levio.awsdemo.createperson.service;

import com.levio.awsdemo.createperson.dto.PersonDto;
import com.levio.awsdemo.createperson.repository.PersonRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RequiredArgsConstructor
@Slf4j
public class PersonService {

    private static final String ANONYMOUS = "Anonymous";

    private final PersonRepository personRepository;

    public void save(PersonDto person) {
        if (!person.isPersonalInfoConsented()) {
            log.info("Consent not granted.");
            person.getPersonalInfo().setFirstName(ANONYMOUS);
            person.getPersonalInfo().setLastName(ANONYMOUS);
        }
        personRepository.save(person);
    }
}
