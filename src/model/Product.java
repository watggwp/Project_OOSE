package model;

public class Product {
    private final String code;
    private final String name;
    private final double price;
    private final ProductCategory category;

    /**
     * คอนสตรักเตอร์สำหรับสร้างข้อมูลสินค้าใหม่
     */
    public Product(String code, String name, double price, ProductCategory category) {
        this.code = code;
        this.name = name;
        this.price = price;
        this.category = category;
    }

    /**
     * ดึงรหัสสินค้า
     */
    public String getCode() {
        return code;
    }

    /**
     * ดึงชื่อสินค้า
     */
    public String getName() {
        return name;
    }

    /**
     * ดึงราคาสินค้า
     */
    public double getPrice() {
        return price;
    }

    /**
     * ดึงหมวดหมู่ของสินค้า
     */
    public ProductCategory getCategory() {
        return category;
    }
}
