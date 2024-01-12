package com.levio.awsdemo.createperson.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProfessionalInfoDto {
    private String companyName;
    private ContactDto contact;
}
