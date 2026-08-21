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
    private Cors cors = new Cors();
    private PasswordReset passwordReset = new PasswordReset();

    @Data
    public static class Cors {
        private List<String> allowedOrigins;
    }

    @Data
    public static class PasswordReset {
        private int tokenExpiryMinutes = 15;
        private String frontendResetUrl = "https://blueantcrm.com/reset-password";
        private int resendCooldownSeconds = 60;
        private int maxRequestsPerHour = 5;
        private int ipMaxRequestsPerHour = 20;
    }

    public List<String> getCorsAllowedOrigins() {
        return cors != null ? cors.getAllowedOrigins() : java.util.Collections.emptyList();
    }
}
