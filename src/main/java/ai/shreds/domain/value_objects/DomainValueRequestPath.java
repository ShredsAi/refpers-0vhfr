package ai.shreds.domain.value_objects;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.regex.Pattern;

/**
 * Value object representing a request path with validation and normalization.
 */
public class DomainValueRequestPath {
    private final String value;
    private static final int MAX_PATH_LENGTH = 2048;
    private static final Pattern VALID_PATH_PATTERN = Pattern.compile("^/[a-zA-Z0-9/_\\-\\.~:@!$&'()*+,;=]*$");

    public DomainValueRequestPath(String value) {
        this.value = normalizePath(value);
        validate();
    }

    private void validate() {
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalArgumentException("Request path cannot be null or empty");
        }
        
        if (value.length() > MAX_PATH_LENGTH) {
            throw new IllegalArgumentException("Request path exceeds maximum length of " + MAX_PATH_LENGTH + " characters");
        }
        
        if (!value.startsWith("/")) {
            throw new IllegalArgumentException("Request path must start with '/'");
        }
        
        // Validate path format
        if (!VALID_PATH_PATTERN.matcher(value).matches()) {
            throw new IllegalArgumentException("Request path contains invalid characters");
        }
        
        // Check for path traversal attempts
        if (value.contains("../") || value.contains("/.") || value.contains("//")) {
            throw new IllegalArgumentException("Request path contains invalid sequences");
        }
        
        // Additional security checks
        if (value.contains("%2e") || value.contains("%2f") || value.contains("%5c")) {
            throw new IllegalArgumentException("Request path contains URL encoded reserved characters");
        }
    }

    /**
     * Normalizes the path by removing redundant slashes and resolving relative paths.
     *
     * @param path the raw path to normalize
     * @return normalized path
     */
    private String normalizePath(String path) {
        if (path == null || path.trim().isEmpty()) {
            return path;
        }
        
        try {
            // Use URI to normalize the path
            URI uri = new URI(null, null, path, null);
            String normalized = uri.normalize().getPath();
            
            // Ensure it starts with /
            if (normalized == null || normalized.isEmpty()) {
                return "/";
            }
            
            if (!normalized.startsWith("/")) {
                normalized = "/" + normalized;
            }
            
            // Remove trailing slash unless it's the root path
            if (normalized.length() > 1 && normalized.endsWith("/")) {
                normalized = normalized.substring(0, normalized.length() - 1);
            }
            
            return normalized;
            
        } catch (URISyntaxException e) {
            // If URI parsing fails, do basic normalization
            return basicNormalization(path);
        }
    }

    /**
     * Basic path normalization when URI parsing fails.
     *
     * @param path the path to normalize
     * @return normalized path
     */
    private String basicNormalization(String path) {
        // Remove multiple consecutive slashes
        String normalized = path.replaceAll("/+", "/");
        
        // Ensure it starts with /
        if (!normalized.startsWith("/")) {
            normalized = "/" + normalized;
        }
        
        // Remove trailing slash unless it's the root path
        if (normalized.length() > 1 && normalized.endsWith("/")) {
            normalized = normalized.substring(0, normalized.length() - 1);
        }
        
        return normalized;
    }

    public String getValue() {
        return value;
    }

    /**
     * Checks if this path starts with the given prefix.
     *
     * @param prefix the prefix to check
     * @return true if this path starts with the prefix
     */
    public boolean startsWith(String prefix) {
        if (prefix == null) {
            return false;
        }
        return value.startsWith(prefix);
    }

    /**
     * Checks if this path ends with the given suffix.
     *
     * @param suffix the suffix to check
     * @return true if this path ends with the suffix
     */
    public boolean endsWith(String suffix) {
        if (suffix == null) {
            return false;
        }
        return value.endsWith(suffix);
    }

    /**
     * Gets the path segments as an array.
     *
     * @return array of path segments (excluding empty segments)
     */
    public String[] getSegments() {
        if (value.equals("/")) {
            return new String[0];
        }
        
        String[] segments = value.substring(1).split("/");
        // Filter out empty segments
        return java.util.Arrays.stream(segments)
            .filter(s -> !s.isEmpty())
            .toArray(String[]::new);
    }

    /**
     * Gets the number of path segments.
     *
     * @return number of segments
     */
    public int getSegmentCount() {
        return getSegments().length;
    }

    /**
     * Gets the file extension if present.
     *
     * @return file extension (without dot) or null if no extension
     */
    public String getExtension() {
        int lastSlash = value.lastIndexOf('/');
        int lastDot = value.lastIndexOf('.');
        
        if (lastDot > lastSlash && lastDot < value.length() - 1) {
            return value.substring(lastDot + 1);
        }
        
        return null;
    }

    /**
     * Checks if this is the root path.
     *
     * @return true if this is the root path ("/")
     */
    public boolean isRoot() {
        return "/".equals(value);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DomainValueRequestPath that = (DomainValueRequestPath) o;
        return value.equals(that.value);
    }

    @Override
    public int hashCode() {
        return value.hashCode();
    }

    @Override
    public String toString() {
        return value;
    }
}