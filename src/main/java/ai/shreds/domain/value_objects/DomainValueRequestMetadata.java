package ai.shreds.domain.value_objects;

import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.Collections;

import ai.shreds.shared.enums.SharedEnumHttpMethod;

/**
 * Value object representing HTTP request metadata.
 */
public class DomainValueRequestMetadata {
    private final String path;
    private final SharedEnumHttpMethod method;
    private final Map<String, List<String>> headers;
    private final String remoteAddress;
    private final Map<String, List<String>> queryParams;
    
    public DomainValueRequestMetadata(
            String path,
            SharedEnumHttpMethod method,
            Map<String, List<String>> headers,
            String remoteAddress,
            Map<String, List<String>> queryParams) {
        this.path = path;
        this.method = method;
        this.headers = headers != null ? new HashMap<>(headers) : new HashMap<>();
        this.remoteAddress = remoteAddress;
        this.queryParams = queryParams != null ? new HashMap<>(queryParams) : new HashMap<>();
        validate();
    }
    
    private void validate() {
        if (path == null || path.trim().isEmpty()) {
            throw new IllegalArgumentException("Path cannot be null or empty");
        }
        if (method == null) {
            throw new IllegalArgumentException("HTTP method cannot be null");
        }
        if (remoteAddress == null || remoteAddress.trim().isEmpty()) {
            throw new IllegalArgumentException("Remote address cannot be null or empty");
        }
        if (!path.startsWith("/")) {
            throw new IllegalArgumentException("Path must start with '/'");
        }
        if (path.length() > 2048) {
            throw new IllegalArgumentException("Path exceeds maximum length of 2048 characters");
        }
        if (remoteAddress.length() > 45) { // IPv6 max length
            throw new IllegalArgumentException("Remote address exceeds maximum length");
        }
    }
    
    public String getPath() {
        return path;
    }
    
    public SharedEnumHttpMethod getMethod() {
        return method;
    }
    
    public Map<String, List<String>> getHeaders() {
        return Collections.unmodifiableMap(headers);
    }
    
    public String getRemoteAddress() {
        return remoteAddress;
    }
    
    public Map<String, List<String>> getQueryParams() {
        return Collections.unmodifiableMap(queryParams);
    }
    
    /**
     * Gets the first value of a header or null if not present.
     */
    public String getHeader(String name) {
        List<String> values = headers.get(name);
        return values != null && !values.isEmpty() ? values.get(0) : null;
    }
    
    /**
     * Gets the first value of a query parameter or null if not present.
     */
    public String getQueryParam(String name) {
        List<String> values = queryParams.get(name);
        return values != null && !values.isEmpty() ? values.get(0) : null;
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DomainValueRequestMetadata that = (DomainValueRequestMetadata) o;
        return path.equals(that.path) &&
               method == that.method &&
               headers.equals(that.headers) &&
               remoteAddress.equals(that.remoteAddress) &&
               queryParams.equals(that.queryParams);
    }
    
    @Override
    public int hashCode() {
        int result = path.hashCode();
        result = 31 * result + method.hashCode();
        result = 31 * result + headers.hashCode();
        result = 31 * result + remoteAddress.hashCode();
        result = 31 * result + queryParams.hashCode();
        return result;
    }
    
    @Override
    public String toString() {
        return String.format("RequestMetadata{path='%s', method=%s, remoteAddress='%s', headers=%d, queryParams=%d}",
            path, method, remoteAddress, headers.size(), queryParams.size());
    }
}