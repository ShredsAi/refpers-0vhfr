package ai.shreds.shared.dtos;

import ai.shreds.shared.enums.SharedEnumHttpMethod;
import org.springframework.http.server.reactive.ServerHttpRequest;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SharedServerHttpRequestDTO {
    private String path;
    private SharedEnumHttpMethod method;
    private Map<String, List<String>> headers;
    private String remoteAddress;
    private Map<String, List<String>> queryParams;

    public SharedServerHttpRequestDTO() {
        this.headers = new HashMap<>();
        this.queryParams = new HashMap<>();
    }
    
    public SharedServerHttpRequestDTO(String path, SharedEnumHttpMethod method, 
                                 Map<String, List<String>> headers, String remoteAddress,
                                 Map<String, List<String>> queryParams) {
        this.path = path;
        this.method = method;
        this.headers = headers != null ? headers : new HashMap<>();
        this.remoteAddress = remoteAddress;
        this.queryParams = queryParams != null ? queryParams : new HashMap<>();
    }

    public String getPath() {
        return path;
    }
    
    public void setPath(String path) {
        this.path = path;
    }

    public SharedEnumHttpMethod getMethod() {
        return method;
    }
    
    public void setMethod(SharedEnumHttpMethod method) {
        this.method = method;
    }

    public Map<String, List<String>> getHeaders() {
        return Collections.unmodifiableMap(headers);
    }
    
    public void setHeaders(Map<String, List<String>> headers) {
        this.headers = headers != null ? new HashMap<>(headers) : new HashMap<>();
    }

    public String getRemoteAddress() {
        return remoteAddress;
    }
    
    public void setRemoteAddress(String remoteAddress) {
        this.remoteAddress = remoteAddress;
    }

    public Map<String, List<String>> getQueryParams() {
        return Collections.unmodifiableMap(queryParams);
    }
    
    public void setQueryParams(Map<String, List<String>> queryParams) {
        this.queryParams = queryParams != null ? new HashMap<>(queryParams) : new HashMap<>();
    }

    public static SharedServerHttpRequestDTO fromServerRequest(ServerHttpRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("ServerHttpRequest cannot be null");
        }
        
        SharedServerHttpRequestDTO dto = new SharedServerHttpRequestDTO();
        
        // Set path
        dto.path = request.getPath().pathWithinApplication().value();
        
        // Set HTTP method
        try {
            dto.method = SharedEnumHttpMethod.valueOf(request.getMethod().name());
        } catch (IllegalArgumentException e) {
            // Default to GET if method can't be parsed
            dto.method = SharedEnumHttpMethod.GET;
        }
        
        // Set headers
        dto.headers = request.getHeaders();
        
        // Set remote address
        if (request.getRemoteAddress() != null && request.getRemoteAddress().getAddress() != null) {
            dto.remoteAddress = request.getRemoteAddress().getAddress().getHostAddress();
        } else {
            dto.remoteAddress = "unknown";
        }
        
        // Set query parameters
        dto.queryParams = request.getQueryParams();
        
        return dto;
    }
    
    @Override
    public String toString() {
        return "SharedServerHttpRequestDTO{" +
                "path='" + path + '\'' +
                ", method=" + method +
                ", headers=" + headers.keySet() +
                ", remoteAddress='" + remoteAddress + '\'' +
                ", queryParams=" + queryParams.keySet() +
                '}';
    }
}