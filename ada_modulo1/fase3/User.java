package auth.domain;

public class User {

    private Long id;
    private String email;
    private String passwordHash;
    private String role;

    public User(Long id,
                String email,
                String passwordHash,
                String role) {

        this.id = id;
        this.email = email;
        this.passwordHash = passwordHash;
        this.role = role;
    }

    public String getEmail() {
        return email;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public String getRole() {
        return role;
    }
}