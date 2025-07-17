package ai.shreds.shared.dtos;

import ai.shreds.shared.value_objects.SharedValueRequestId;
import ai.shreds.shared.value_objects.SharedValueIpAddress;
import ai.shreds.shared.enums.SharedEnumHttpMethod;
import ai.shreds.domain.entities.DomainEntityRequestContext;
import ai.shreds.domain.entities.DomainEntityAuthenticationContext;
import ai.shreds.domain.value_objects.DomainValueUserAgent;
import ai.shreds.domain.value_objects.DomainValueRequestPath;
import ai.shreds.domain.value_objects.DomainValueRequestHeaders;

import java.time.Instant;
import java.util.Map;

public class SharedRequestContextDTO {
    private SharedValueRequestId requestId;
    private SharedValueIpAddress clientIp;
    private String userAgent;
    private String requestPath;
    private SharedEnumHttpMethod httpMethod;
    private Map<String, String> headers;
    private Instant timestamp;
    private SharedAuthenticationContextDTO authContext;

    public SharedRequestContextDTO() {
    }
    
    public SharedRequestContextDTO(SharedValueRequestId requestId, SharedValueIpAddress clientIp, 
                                   String userAgent, String requestPath, SharedEnumHttpMethod httpMethod, 
                                   Map<String, String> headers, Instant timestamp, 
                                   SharedAuthenticationContextDTO authContext) {
        this.requestId = requestId;
        this.clientIp = clientIp;
        this.userAgent = userAgent;
        this.requestPath = requestPath;
        this.httpMethod = httpMethod;
        this.headers = headers;
        this.timestamp = timestamp;
        this.authContext = authContext;
    }

    /**
     * Converts this DTO to a domain entity.
     * 
     * @return DomainEntityRequestContext
     */
    public DomainEntityRequestContext toEntity() {
        if (requestId == null) {
            throw new IllegalStateException("RequestId cannot be null when converting to entity");
        }
        if (clientIp == null) {
            throw new IllegalStateException("ClientIp cannot be null when converting to entity");
        }
        if (userAgent == null) {
            throw new IllegalStateException("UserAgent cannot be null when converting to entity");
        }
        if (requestPath == null) {
            throw new IllegalStateException("RequestPath cannot be null when converting to entity");
        }
        if (httpMethod == null) {
            throw new IllegalStateException("HttpMethod cannot be null when converting to entity");
        }
        if (headers == null) {
            throw new IllegalStateException("Headers cannot be null when converting to entity");
        }
        if (timestamp == null) {
            throw new IllegalStateException("Timestamp cannot be null when converting to entity");
        }
        if (authContext == null) {
            throw new IllegalStateException("AuthContext cannot be null when converting to entity");
        }

        return new DomainEntityRequestContext(
            requestId,
            clientIp,
            new DomainValueUserAgent(userAgent),
            new DomainValueRequestPath(requestPath),
            httpMethod,
            new DomainValueRequestHeaders(headers),
            timestamp,
            authContext.toEntity()
        );
    }

    /**
     * Creates a DTO from a domain entity.
     * 
     * @param entity the domain entity
     * @return SharedRequestContextDTO
     */
    public static SharedRequestContextDTO fromEntity(DomainEntityRequestContext entity) {
        if (entity == null) {
            throw new IllegalArgumentException("Entity cannot be null");
        }
        
        return entity.toDTO();
    }

    public SharedValueRequestId getRequestId() {
        return requestId;
    }

    public void setRequestId(SharedValueRequestId requestId) {
        this.requestId = requestId;
    }

    public SharedValueIpAddress getClientIp() {
        return clientIp;
    }

    public void setClientIp(SharedValueIpAddress clientIp) {
        this.clientIp = clientIp;
    }

    public String getUserAgent() {
        return userAgent;
    }

    public void setUserAgent(String userAgent) {
        this.userAgent = userAgent;
    }

    public String getRequestPath() {
        return requestPath;
    }

    public void setRequestPath(String requestPath) {
        this.requestPath = requestPath;
    }

    public SharedEnumHttpMethod getHttpMethod() {
        return httpMethod;
    }

    public void setHttpMethod(SharedEnumHttpMethod httpMethod) {
        this.httpMethod = httpMethod;
    }

    public Map<String, String> getHeaders() {
        return headers;
    }

    public void setHeaders(Map<String, String> headers) {
        this.headers = headers;
    }

    public Instant getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Instant timestamp) {
        this.timestamp = timestamp;
    }

    public SharedAuthenticationContextDTO getAuthContext() {
        return authContext;
    }

    public void setAuthContext(SharedAuthenticationContextDTO authContext) {
        this.authContext = authContext;
    }

    @Override
    public String toString() {
        return "SharedRequestContextDTO{" +
                "requestId=" + requestId +
                ", clientIp=" + clientIp +
                ", userAgent='" + userAgent + '\'' +
                ", requestPath='" + requestPath + '\'' +
                ", httpMethod=" + httpMethod +
                ", timestamp=" + timestamp +
                ", authContext=" + authContext +
                '}';
    }
}