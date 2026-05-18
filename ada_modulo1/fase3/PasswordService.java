package auth.security;

import org.mindrot.jbcrypt.BCrypt;

public class PasswordService {

    public String hashPassword(String password) {

        return BCrypt.hashpw(
                password,
                BCrypt.gensalt(12)
        );
    }

    public boolean verifyPassword(String password,
                                  String hash) {

        return BCrypt.checkpw(password, hash);
    }
}