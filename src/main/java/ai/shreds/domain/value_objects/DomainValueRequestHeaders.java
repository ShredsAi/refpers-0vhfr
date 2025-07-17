package ai.shreds.domain.value_objects;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Value object representing HTTP request headers with validation.
 */
public class DomainValueRequestHeaders {
    private final Map<String, String> headers;
    private static final int MAX_HEADER_NAME_LENGTH = 256;
    private static final int MAX_HEADER_VALUE_LENGTH = 8192;
    private static final int MAX_HEADERS_COUNT = 100;

    public DomainValueRequestHeaders(Map<String, String> headers) {
        this.headers = new HashMap<>(headers != null ? headers : Collections.emptyMap());
        validate();
    }

    private void validate() {
        if (headers == null) {
            throw new IllegalArgumentException("Headers cannot be null");
        }
        
        if (headers.size() > MAX_HEADERS_COUNT) {
            throw new IllegalArgumentException("Too many headers. Maximum allowed: " + MAX_HEADERS_COUNT);
        }
        
        for (Map.Entry<String, String> entry : headers.entrySet()) {
            String name = entry.getKey();
            String value = entry.getValue();
            
            if (name == null || name.trim().isEmpty()) {
                throw new IllegalArgumentException("Header name cannot be null or empty");
            }
            
            if (name.length() > MAX_HEADER_NAME_LENGTH) {
                throw new IllegalArgumentException("Header name exceeds maximum length of " + MAX_HEADER_NAME_LENGTH + " characters");
            }
            
            if (value == null) {
                throw new IllegalArgumentException("Header value cannot be null for header: " + name);
            }
            
            if (value.length() > MAX_HEADER_VALUE_LENGTH) {
                throw new IllegalArgumentException("Header value exceeds maximum length of " + MAX_HEADER_VALUE_LENGTH + " characters for header: " + name);
            }
            
            // Validate header name format (RFC 7230)
            if (!name.matches("^[a-zA-Z0-9!#$%&'*+\\-.^_`|~]+$")) {
                throw new IllegalArgumentException("Invalid header name format: " + name);
            }
            
            // Validate header value format (printable ASCII characters)
            if (!value.matches("^[\\x09\\x20-\\x7E]*$")) {
                throw new IllegalArgumentException("Invalid header value format for header: " + name);
            }
        }
    }

    public Map<String, String> getHeaders() {
        return Collections.unmodifiableMap(headers);
    }

    /**
     * Gets a specific header value.
     *
     * @param name the header name (case-insensitive)
     * @return the header value or null if not found
     */
    public String getHeader(String name) {
        if (name == null) {
            return null;
        }
        
        // Case-insensitive lookup
        for (Map.Entry<String, String> entry : headers.entrySet()) {
            if (entry.getKey().equalsIgnoreCase(name)) {
                return entry.getValue();
            }
        }
        
        return null;
    }

    /**
     * Checks if a header exists.
     *
     * @param name the header name (case-insensitive)
     * @return true if the header exists
     */
    public boolean hasHeader(String name) {
        return getHeader(name) != null;
    }

    /**
     * Gets the number of headers.
     *
     * @return the number of headers
     */
    public int size() {
        return headers.size();
    }

    /**
     * Checks if headers are empty.
     *
     * @return true if no headers are present
     */
    public boolean isEmpty() {
        return headers.isEmpty();
    }

    /**
     * Creates a new instance with an additional header.
     *
     * @param name the header name
     * @param value the header value
     * @return a new DomainValueRequestHeaders instance with the additional header
     */
    public DomainValueRequestHeaders withHeader(String name, String value) {
        Map<String, String> newHeaders = new HashMap<>(headers);
        newHeaders.put(name, value);
        return new DomainValueRequestHeaders(newHeaders);
    }

    /**
     * Creates a new instance without a specific header.
     *
     * @param name the header name to remove
     * @return a new DomainValueRequestHeaders instance without the specified header
     */
    public DomainValueRequestHeaders withoutHeader(String name) {
        Map<String, String> newHeaders = new HashMap<>(headers);
        newHeaders.entrySet().removeIf(entry -> entry.getKey().equalsIgnoreCase(name));
        return new DomainValueRequestHeaders(newHeaders);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DomainValueRequestHeaders that = (DomainValueRequestHeaders) o;
        return Objects.equals(headers, that.headers);
    }

    @Override
    public int hashCode() {
        return Objects.hash(headers);
    }

    @Override
    public String toString() {
        return String.format("RequestHeaders{count=%d, headers=%s}", headers.size(), headers);
    }
}