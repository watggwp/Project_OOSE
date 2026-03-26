package service;

import model.User;
import repository.UserRepository;

public class AuthService {
    private final UserRepository userRepository;

    public AuthService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    /**
     * สมัครสมาชิกใหม่ โดยตรวจสอบว่าชื่อผู้ใช้มีในระบบแล้วหรือไม่
     */
    public boolean register(String username, String password) {
        if (username == null || username.trim().isEmpty() || password == null || password.trim().isEmpty()) {
            return false;
        }
        if (userRepository.existsByUsername(username)) {
            return false;
        }
        return userRepository.save(new User(username, password));
    }

    /**
     * เข้าสู่ระบบ ตรวจสอบชื่อผู้ใช้และรหัสผ่าน หากถูกต้องจะคืนค่าเป็นอ็อบเจกต์ User
     */
    public User login(String username, String password) {
        User user = userRepository.findByUsername(username);
        if (user == null) {
            return null;
        }
        if (user.login(username, password)) {
            return user;
        }
        return null;
    }
}
