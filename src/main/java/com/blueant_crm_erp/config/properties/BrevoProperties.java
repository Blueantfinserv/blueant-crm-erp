package com.blueant_crm_erp.config.properties;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Email;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Getter
@Setter
@Validated
@ConfigurationProperties(prefix = "brevo")
public class BrevoProperties {

    @NotBlank(message = "Brevo API key must not be blank.")
    private String apiKey;

    @NotBlank(message = "Brevo from email must not be blank.")
    @Email(message = "Brevo from email must be a valid email address.")
    private String fromEmail;

    @NotBlank(message = "Brevo from name must not be blank.")
    private String fromName;

    @NotBlank(message = "Brevo API URL must not be blank.")
    private String apiUrl = "https://api.brevo.com/v3";
}
