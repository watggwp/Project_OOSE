package model;

public enum ProductCategory {
    COFFEE("กาแฟ"),
    TEA("ชา"),
    BAKERY("เบเกอรี่");

    private final String displayName;

    /**
     * คอนสตรักเตอร์สำหรับกำหนดชื่อแสดงผลของหมวดหมู่
     */
    ProductCategory(String displayName) {
        this.displayName = displayName;
    }

    /**
     * ดึงชื่อแสดงผลของหมวดหมู่ (ภาษาไทย)
     */
    public String getDisplayName() {
        return displayName;
    }

    @Override
    public String toString() {
        return displayName;
    }
}
