package shyrshovfinal.service;

import org.springframework.security.crypto.password.PasswordEncoder;

public class PasswordEncoderUtil {

    private final PasswordEncoder passwordEncoder;

    public PasswordEncoderUtil(PasswordEncoder passwordEncoder) {
        this.passwordEncoder = passwordEncoder;
    }

    public boolean checkPassword(String password, String hashedPassword) {
        return passwordEncoder.matches(password, hashedPassword);
    }
}
