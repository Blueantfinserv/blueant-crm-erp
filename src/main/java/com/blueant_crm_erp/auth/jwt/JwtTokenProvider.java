package com.blueant_crm_erp.auth.jwt;

import com.blueant_crm_erp.auth.security.CustomUserDetails;
import com.blueant_crm_erp.config.properties.JwtProperties;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * =============================================================================
 * JWT Token Provider
 * =============================================================================
 *
 * Single production-ready implementation for JWT Operations.
 *
 * Responsibilities
 * -----------------------------------------------------------------------------
 * • generateAccessToken()
 * • generateRefreshToken()
 * • validateToken()
 * • extractClaims()
 * • extractUsername()
 * • extractExpiration()
 * • resolveToken()
 * • parseToken()
 *
 * Project : BlueAnt CRM ERP Platform
 * Module  : Authentication
 *
 * @author BlueAnt CRM ERP Team
 * @since 1.0.0
 * =============================================================================
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class JwtTokenProvider {

    private final JwtProperties jwtProperties;
    private SecretKey secretKey;

    @PostConstruct
    public void init() {
        byte[] keyBytes = Decoders.BASE64.decode(jwtProperties.getSecret());
        this.secretKey = Keys.hmacShaKeyFor(keyBytes);
    }

    /**
     * Generate Access Token with all necessary claims.
     */
    public String generateAccessToken(CustomUserDetails userDetails, String sessionId) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", userDetails.getUserId());
        claims.put("employeeCode", userDetails.getEmployeeCode());
        claims.put("roleCode", userDetails.getRoleCode());
        claims.put("department", userDetails.getDepartment());
        claims.put("designation", userDetails.getDesignation());
        claims.put("team", userDetails.getTeam());
        claims.put("sessionId", sessionId);

        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + jwtProperties.getExpirationMs());

        return Jwts.builder()
                .claims(claims)
                .subject(userDetails.getUsername())
                .issuer(jwtProperties.getIssuer())
                .issuedAt(now)
                .expiration(expiryDate)
                .signWith(secretKey)
                .compact();
    }

    /**
     * Generate Refresh Token.
     */
    public String generateRefreshToken(CustomUserDetails userDetails) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + jwtProperties.getRefreshExpirationMs());

        return Jwts.builder()
                .subject(userDetails.getUsername())
                .issuer(jwtProperties.getIssuer())
                .issuedAt(now)
                .expiration(expiryDate)
                .signWith(secretKey)
                .compact();
    }

    /**
     * Validate JWT Token.
     */
    public boolean validateToken(String token) {
        try {
            parseToken(token);
            return true;
        } catch (JwtException ex) {
            log.error("Invalid JWT Token", ex);
            return false;
        } catch (Exception ex) {
            log.error("Unexpected error validating JWT token", ex);
            return false;
        }
    }

    /**
     * Extract all claims.
     */
    public Claims extractClaims(String token) {
        return parseToken(token).getPayload();
    }

    /**
     * Extract username.
     */
    public String extractUsername(String token) {
        return extractClaims(token).getSubject();
    }

    /**
     * Extract expiration.
     */
    public Date extractExpiration(String token) {
        return extractClaims(token).getExpiration();
    }

    /**
     * Resolve JWT token from HTTP request header.
     */
    public String resolveToken(HttpServletRequest request) {
        String bearerToken = request.getHeader(jwtProperties.getHeader());
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith(jwtProperties.getPrefix() + " ")) {
            String token = bearerToken.substring(jwtProperties.getPrefix().length() + 1).trim();
            if (token.startsWith("\"") && token.endsWith("\"")) {
                token = token.substring(1, token.length() - 1);
            }
            if (token.startsWith("'") && token.endsWith("'")) {
                token = token.substring(1, token.length() - 1);
            }
            return token;
        }
        return null;
    }

    /**
     * Parse the JWT token.
     */
    private io.jsonwebtoken.Jws<Claims> parseToken(String token) {
        return Jwts.parser()
                .verifyWith(secretKey)
                .build()
                .parseSignedClaims(token);
    }
}
