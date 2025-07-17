package ai.shreds.shared.enums;

public enum SharedEnumUserRole {
    USER,
    ADMIN,
    MODERATOR,
    GUEST;
    
    public static SharedEnumUserRole fromString(String roleName) {
        for (SharedEnumUserRole role : SharedEnumUserRole.values()) {
            if (role.name().equalsIgnoreCase(roleName)) {
                return role;
            }
        }
        throw new IllegalArgumentException("Unknown role: " + roleName);
    }
}
