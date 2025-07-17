package ai.shreds.shared.value_objects;

import org.springframework.util.AntPathMatcher;

public class SharedValuePathPattern {
    private final String pattern;
    private static final AntPathMatcher pathMatcher = new AntPathMatcher();

    public SharedValuePathPattern(String pattern) {
        if (pattern == null || pattern.isEmpty()) {
            throw new IllegalArgumentException("Path pattern cannot be null or empty");
        }
        this.pattern = pattern;
        validate();
    }

    public String getPattern() {
        return pattern;
    }

    public void validate() {
        if (!pattern.startsWith("/")) {
            throw new IllegalArgumentException("Path pattern must start with '/': " + pattern);
        }
        // Check for invalid characters or patterns
        if (pattern.contains("//") || pattern.contains(".") || pattern.contains("\\")) {
            throw new IllegalArgumentException("Invalid path pattern format: " + pattern);
        }
    }

    public boolean matches(String path) {
        return pathMatcher.match(pattern, path);
    }

    @Override
    public String toString() {
        return pattern;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SharedValuePathPattern that = (SharedValuePathPattern) o;
        return pattern.equals(that.pattern);
    }

    @Override
    public int hashCode() {
        return pattern.hashCode();
    }
}
