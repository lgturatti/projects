package auth.service;

import auth.domain.User;
import auth.security.JwtService;
import auth.security.PasswordService;

public class AuthService {

    private final PasswordService passwordService =
            new PasswordService();

    private final JwtService jwtService =
            new JwtService();

    public String login(User user,
                        String password) {

        boolean valid =
                passwordService.verifyPassword(
                        password,
                        user.getPasswordHash()
                );

        if (!valid) {
            throw new RuntimeException(
                    "Invalid credentials"
            );
        }

        return jwtService.generateToken(
                user.getEmail(),
                user.getRole()
        );
    }
}