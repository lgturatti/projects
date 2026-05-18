package auth;

import auth.domain.User;
import auth.security.PasswordService;
import auth.service.AuthService;

public class Main {

    public static void main(String[] args) {

        PasswordService passwordService =
                new PasswordService();

        String hash =
                passwordService.hashPassword(
                        "Admin@123"
                );

        User user = new User(
                1L,
                "admin@eventmaster.com",
                hash,
                "ADMIN"
        );

        AuthService authService =
                new AuthService();

        String token =
                authService.login(
                        user,
                        "Admin@123"
                );

        System.out.println(
                "JWT TOKEN:\n" + token
        );
    }
}