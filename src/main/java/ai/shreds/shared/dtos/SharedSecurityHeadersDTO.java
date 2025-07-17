package ai.shreds.shared.dtos;

public class SharedSecurityHeadersDTO {
    private String userId;
    private String userRoles;

    public SharedSecurityHeadersDTO() {
    }

    public SharedSecurityHeadersDTO(String userId, String userRoles) {
        this.userId = userId;
        this.userRoles = userRoles;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getUserRoles() {
        return userRoles;
    }

    public void setUserRoles(String userRoles) {
        this.userRoles = userRoles;
    }

    @Override
    public String toString() {
        return "SharedSecurityHeadersDTO{" +
                "userId='" + userId + '\'' +
                ", userRoles='" + userRoles + '\'' +
                '}';
    }
}
