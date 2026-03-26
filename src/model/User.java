package model;

public class User {
    private final String username;
    private final String password;

    /**
     * คอนสตรักเตอร์สำหรับสร้างผู้ใช้ใหม่
     */
    public User(String username, String password) {
        this.username = username;
        this.password = password;
    }

    /**
     * ตรวจสอบชื่อผู้ใช้และรหัสผ่านสำหรับการเข้าสู่ระบบ
     */
    public boolean login(String username, String password) {
        return this.username.equals(username) && this.password.equals(password);
    }

    /**
     * ทำการออกจากระบบ
     */
    public void logout() {
        // Nothing to do since there's no state variable
    }



    /**
     * ดึงชื่อผู้ใช้
     */
    public String getUsername() {
        return username;
    }

    /**
     * ดึงรหัสผ่านผู้ใช้
     */
    public String getPassword() {
        return password;
    }
}
