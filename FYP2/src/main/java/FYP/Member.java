package FYP;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

@Entity
public class Member {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private int id;
    
    @NotEmpty(message = "NRIC is required.")
    @Pattern(regexp = "^[STFGM][0-9]{7}[A-Z]$", message = "NRIC must start with S, T, F, G, or M, followed by 7 digits and an uppercase letter.")
    private String nric;

    @NotEmpty(message = "Name is required.")
    @Size(min = 3, message = "Name length must be at least 3 characters!")
    private String name;

    @NotEmpty(message = "Username is required.")
    @Size(min = 3, message = "Username length must be at least 3 characters!")
    private String username;

    @NotEmpty(message = "Password is required.")
    @Size(min = 8, message = "Password must be at least 8 characters.")
    private String password;

    @NotEmpty(message = "Email is required.")
    private String email;

    private String role;

    // Getters and Setters
    public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}
	
    public String getNric() {
        return nric;
    }

    public void setNric(String nric) {
        this.nric = nric;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }
}
