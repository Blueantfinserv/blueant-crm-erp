package com.blueant_crm_erp.util.network;

import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Objects;

/**
 * URL Utility.
 *
 * Centralized utility for URL validation and manipulation.
 *
 * Responsibilities:
 * - URL validation
 * - HTTP / HTTPS detection
 * - URL encoding
 * - URL decoding
 * - Domain extraction
 * - Path extraction
 * - Protocol normalization
 *
 * This utility DOES NOT:
 * - Make HTTP requests
 * - Download files
 * - Call external APIs
 *
 * Used By:
 * - Authentication Module
 * - Notification Module
 * - File Module
 * - Integration Module
 * - Email Module
 *
 * @author BlueAnt CRM ERP
 * @version 1.0
 */
public final class UrlUtil {

    private UrlUtil() {
        throw new IllegalStateException("Utility class");
    }

    /**
     * Returns true if URL is valid.
     */
    public static boolean isValidUrl(String url) {

        if (url == null || url.isBlank()) {
            return false;
        }

        try {

            URI uri = new URI(url);

            return uri.getScheme() != null
                    && uri.getHost() != null;

        } catch (URISyntaxException ex) {
            return false;
        }
    }

    /**
     * Returns true if HTTP URL.
     */
    public static boolean isHttp(String url) {

        return isValidUrl(url)
                && url.toLowerCase().startsWith("http://");
    }

    /**
     * Returns true if HTTPS URL.
     */
    public static boolean isHttps(String url) {

        return isValidUrl(url)
                && url.toLowerCase().startsWith("https://");
    }

    /**
     * Returns normalized HTTPS URL.
     */
    public static String normalize(String url) {

        Objects.requireNonNull(url, "URL cannot be null.");

        String value = url.trim();

        if (!value.startsWith("http://")
                && !value.startsWith("https://")) {

            value = "https://" + value;
        }

        return value;
    }

    /**
     * Returns host/domain.
     */
    public static String getHost(String url) {

        Objects.requireNonNull(url);

        try {

            return new URI(normalize(url))
                    .getHost();

        } catch (URISyntaxException ex) {

            throw new IllegalArgumentException(
                    "Invalid URL.",
                    ex
            );
        }
    }

    /**
     * Returns URL path.
     */
    public static String getPath(String url) {

        Objects.requireNonNull(url);

        try {

            return new URI(normalize(url))
                    .getPath();

        } catch (URISyntaxException ex) {

            throw new IllegalArgumentException(
                    "Invalid URL.",
                    ex
            );
        }
    }

    /**
     * Returns query string.
     */
    public static String getQuery(String url) {

        Objects.requireNonNull(url);

        try {

            return new URI(normalize(url))
                    .getQuery();

        } catch (URISyntaxException ex) {

            throw new IllegalArgumentException(
                    "Invalid URL.",
                    ex
            );
        }
    }

    /**
     * URL Encode.
     */
    public static String encode(String value) {

        Objects.requireNonNull(value);

        return URLEncoder.encode(
                value,
                StandardCharsets.UTF_8
        );
    }

    /**
     * URL Decode.
     */
    public static String decode(String value) {

        Objects.requireNonNull(value);

        return URLDecoder.decode(
                value,
                StandardCharsets.UTF_8
        );
    }

    /**
     * Removes trailing slash.
     */
    public static String removeTrailingSlash(String url) {

        Objects.requireNonNull(url);

        return url.endsWith("/")
                ? url.substring(0, url.length() - 1)
                : url;
    }

    /**
     * Returns true if two URLs belong to same domain.
     */
    public static boolean sameHost(
            String firstUrl,
            String secondUrl) {

        return Objects.equals(
                getHost(firstUrl),
                getHost(secondUrl)
        );
    }

}