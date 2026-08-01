package com.blueant_crm_erp.util.jwt;

import io.jsonwebtoken.Claims;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Objects;

/**
 * Utility class for extracting information from JWT Claims.
 *
 * This utility only reads claims.
 * Token generation and validation belong to JwtUtil.
 *
 * @author BlueAnt CRM ERP
 * @version 1.0
 */
public final class JwtClaimsUtil {

    private static final ZoneId DEFAULT_ZONE = ZoneId.of("Asia/Kolkata");

    /*
     * Standard JWT Claims
     */
    public static final String SUBJECT = Claims.SUBJECT;
    public static final String ISSUER = Claims.ISSUER;
    public static final String AUDIENCE = Claims.AUDIENCE;
    public static final String ID = Claims.ID;

    /*
     * Custom Claims
     */
    public static final String USER_ID = "userId";
    public static final String EMPLOYEE_CODE = "employeeCode";
    public static final String EMAIL = "email";
    public static final String ROLES = "roles";

    private JwtClaimsUtil() {
        throw new IllegalStateException("Utility class");
    }

    public static String getUsername(Claims claims) {
        requireClaims(claims);
        return claims.getSubject();
    }

    public static Long getUserId(Claims claims) {
        requireClaims(claims);
        return claims.get(USER_ID, Long.class);
    }

    public static String getEmployeeCode(Claims claims) {
        requireClaims(claims);
        return claims.get(EMPLOYEE_CODE, String.class);
    }

    public static String getEmail(Claims claims) {
        requireClaims(claims);
        return claims.get(EMAIL, String.class);
    }

    public static String getIssuer(Claims claims) {
        requireClaims(claims);
        return claims.getIssuer();
    }

    public static String getAudience(Claims claims) {

        requireClaims(claims);

        if (claims.getAudience() == null ||
                claims.getAudience().isEmpty()) {

            return null;
        }

        return claims.getAudience()
                .iterator()
                .next();
    }
    public static String getTokenId(Claims claims) {
        requireClaims(claims);
        return claims.getId();
    }

    @SuppressWarnings("unchecked")
    public static List<String> getRoles(Claims claims) {

        requireClaims(claims);

        Object roles = claims.get(ROLES);

        if (roles instanceof List<?>) {
            return (List<String>) roles;
        }

        return Collections.emptyList();
    }

    public static boolean hasRole(
            Claims claims,
            String role) {

        requireClaims(claims);

        return getRoles(claims)
                .stream()
                .anyMatch(role::equalsIgnoreCase);
    }

    public static LocalDateTime getIssuedAt(Claims claims) {

        requireClaims(claims);

        return toLocalDateTime(claims.getIssuedAt());
    }

    public static LocalDateTime getExpiration(Claims claims) {

        requireClaims(claims);

        return toLocalDateTime(claims.getExpiration());
    }

    public static LocalDateTime getNotBefore(Claims claims) {

        requireClaims(claims);

        return toLocalDateTime(claims.getNotBefore());
    }

    public static Instant getIssuedAtInstant(Claims claims) {

        requireClaims(claims);

        Date issuedAt = claims.getIssuedAt();

        return issuedAt == null
                ? null
                : issuedAt.toInstant();
    }

    public static boolean isExpired(Claims claims) {

        LocalDateTime expiration = getExpiration(claims);

        return expiration != null &&
                expiration.isBefore(LocalDateTime.now(DEFAULT_ZONE));
    }

    public static boolean hasClaim(
            Claims claims,
            String claimName) {

        requireClaims(claims);

        return claims.containsKey(claimName);
    }

    public static <T> T getClaim(
            Claims claims,
            String claimName,
            Class<T> clazz) {

        requireClaims(claims);

        return claims.get(claimName, clazz);
    }

    private static LocalDateTime toLocalDateTime(Date date) {

        if (date == null) {
            return null;
        }

        return LocalDateTime.ofInstant(
                date.toInstant(),
                DEFAULT_ZONE
        );
    }

    private static void requireClaims(Claims claims) {

        Objects.requireNonNull(
                claims,
                "JWT Claims cannot be null."
        );
    }

}