package com.nexstar.portal.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.List;

@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "app")
public class AppProperties {

    private Jwt jwt = new Jwt();
    private Cors cors = new Cors();
    private Aws aws = new Aws();
    private Mail mail = new Mail();
    private PasswordReset passwordReset = new PasswordReset();
    private EmailVerification emailVerification = new EmailVerification();
    private Pagination pagination = new Pagination();

    @Getter
    @Setter
    public static class Jwt {
        private String secret;
        private long accessTokenExpirationMs;
        private long refreshTokenExpirationMs;
    }

    @Getter
    @Setter
    public static class Cors {
        private List<String> allowedOrigins;
        private String allowedMethods;
        private String allowedHeaders;
        private boolean allowCredentials;
        private long maxAge;
    }

    @Getter
    @Setter
    public static class Aws {
        private String region;
        private String accessKey;
        private String secretKey;
        private String s3Bucket;
    }

    @Getter
    @Setter
    public static class Mail {
        private String from;
        private String fromName;
    }

    @Getter
    @Setter
    public static class PasswordReset {
        private int tokenExpiryMinutes;
    }

    @Getter
    @Setter
    public static class EmailVerification {
        private int tokenExpiryHours;
    }

    @Getter
    @Setter
    public static class Pagination {
        private int defaultPageSize;
        private int maxPageSize;
    }
}
