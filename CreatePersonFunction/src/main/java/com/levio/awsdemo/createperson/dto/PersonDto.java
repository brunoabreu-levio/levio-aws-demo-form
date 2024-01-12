package com.levio.awsdemo.createperson.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PersonDto {
    private PersonalInfoDto personalInfo;
    private ProfessionalInfoDto professionalInfo;
    @JsonProperty("isPersonalInfoConsented")
    private boolean isPersonalInfoConsented;
}

