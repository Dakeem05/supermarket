public class Admin {
    private int adminId;
    private String username;
    private String password;
    private String role;

    // Constructor, Getters & Setters
    public Admin(int adminId, String username, String password, String role) {
        this.adminId = adminId;
        this.username = username;
        this.password = password;
        this.role = role;
    }

    // Getters & Setters
    public int getAdminId() { return adminId; }
    public String getUsername() { return username; }
    public String getPassword() { return password; }
    public String getRole() { return role; }
}