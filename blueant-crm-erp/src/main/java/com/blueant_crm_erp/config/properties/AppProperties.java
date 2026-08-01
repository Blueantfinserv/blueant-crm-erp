package com.blueant_crm_erp.config.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@ConfigurationProperties(prefix = "app")
@Data
public class AppProperties {

    private String name;
    private String version;
    private String supportEmail;
    private String frontendUrl;
    private List<String> corsAllowedOrigins;
}
