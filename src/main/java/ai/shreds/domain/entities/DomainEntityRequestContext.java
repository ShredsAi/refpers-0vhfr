package ai.shreds.domain.entities;

import java.time.Instant;
import java.util.Map;

import ai.shreds.shared.dtos.SharedRequestContextDTO;
import ai.shreds.shared.enums.SharedEnumHttpMethod;
import ai.shreds.shared.value_objects.SharedValueRequestId;
import ai.shreds.shared.value_objects.SharedValueIpAddress;
import ai.shreds.domain.value_objects.DomainValueUserAgent;
import ai.shreds.domain.value_objects.DomainValueRequestPath;
import ai.shreds.domain.value_objects.DomainValueRequestHeaders;

/**
 * Entity representing a complete request context combining HTTP request metadata
 * with authentication information.
 */
public class DomainEntityRequestContext {
    private final SharedValueRequestId requestId;
    private final SharedValueIpAddress clientIp;
    private final DomainValueUserAgent userAgent;
    private final DomainValueRequestPath requestPath;
    private final SharedEnumHttpMethod httpMethod;
    private final DomainValueRequestHeaders headers;
    private final Instant timestamp;
    private final DomainEntityAuthenticationContext authContext;

    public DomainEntityRequestContext(
            SharedValueRequestId requestId,
            SharedValueIpAddress clientIp,
            DomainValueUserAgent userAgent,
            DomainValueRequestPath requestPath,
            SharedEnumHttpMethod httpMethod,
            DomainValueRequestHeaders headers,
            Instant timestamp,
            DomainEntityAuthenticationContext authContext) {
        this.requestId = requestId;
        this.clientIp = clientIp;
        this.userAgent = userAgent;
        this.requestPath = requestPath;
        this.httpMethod = httpMethod;
        this.headers = headers;
        this.timestamp = timestamp;
        this.authContext = authContext;
        validate();
    }

    private void validate() {
        if (requestId == null) {
            throw new IllegalArgumentException("Request ID cannot be null");
        }
        if (clientIp == null) {
            throw new IllegalArgumentException("Client IP cannot be null");
        }
        if (userAgent == null) {
            throw new IllegalArgumentException("User Agent cannot be null");
        }
        if (requestPath == null) {
            throw new IllegalArgumentException("Request Path cannot be null");
        }
        if (httpMethod == null) {
            throw new IllegalArgumentException("HTTP Method cannot be null");
        }
        if (headers == null) {
            throw new IllegalArgumentException("Headers cannot be null");
        }
        if (timestamp == null) {
            throw new IllegalArgumentException("Timestamp cannot be null");
        }
        if (authContext == null) {
            throw new IllegalArgumentException("Authentication Context cannot be null");
        }
    }

    public SharedValueRequestId getRequestId() {
        return requestId;
    }

    public SharedValueIpAddress getClientIp() {
        return clientIp;
    }

    public DomainValueUserAgent getUserAgent() {
        return userAgent;
    }

    public DomainValueRequestPath getRequestPath() {
        return requestPath;
    }

    public SharedEnumHttpMethod getHttpMethod() {
        return httpMethod;
    }

    public DomainValueRequestHeaders getHeaders() {
        return headers;
    }

    public Instant getTimestamp() {
        return timestamp;
    }

    public DomainEntityAuthenticationContext getAuthContext() {
        return authContext;
    }

    /**
     * Converts this domain entity to a shared DTO.
     */
    public SharedRequestContextDTO toDTO() {
        SharedRequestContextDTO dto = new SharedRequestContextDTO();
        dto.setRequestId(requestId);
        dto.setClientIp(clientIp);
        dto.setUserAgent(userAgent.getValue());
        dto.setRequestPath(requestPath.getValue());
        dto.setHttpMethod(httpMethod);
        dto.setHeaders(headers.getHeaders());
        dto.setTimestamp(timestamp);
        dto.setAuthContext(authContext.toDTO());
        return dto;
    }

    @Override
    public String toString() {
        return String.format("RequestContext{requestId=%s, clientIp=%s, path=%s, method=%s, timestamp=%s}",
            requestId, clientIp, requestPath, httpMethod, timestamp);
    }
}