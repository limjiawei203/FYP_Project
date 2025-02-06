package FYP;

import java.time.LocalDateTime;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;

@Entity
@Table(name = "Exceldatabase")
public class Exceldatabase {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    private String cust_name_tx;
    private String trading_name;
    private String ctc_name;
    private String mobile_no;
    private String email_tx;
    private LocalDateTime last_Modified;

    // Getter and setter for lastModified
    public LocalDateTime getLastModified() {
        return last_Modified;
    }

    public void setLastModified(LocalDateTime lastModified) {
        this.last_Modified = lastModified;
    }

    // Only update timestamp on entity updates, not on creations
    @PreUpdate
    public void onUpdate() {
        this.last_Modified = LocalDateTime.now();
    }

    // Set initial timestamp on creation
    @PrePersist
    public void onCreate() {
        this.last_Modified = LocalDateTime.now();
    }

    // Getters and Setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getCust_name_tx() {
        return cust_name_tx;
    }

    public void setCust_name_tx(String cust_name_tx) {
        this.cust_name_tx = cust_name_tx;
    }

    public String getTrading_name() {
        return trading_name;
    }

    public void setTrading_name(String trading_name) {
        this.trading_name = trading_name;
    }

    public String getCtc_name() {
        return ctc_name;
    }

    public void setCtc_name(String ctc_name) {
        this.ctc_name = ctc_name;
    }

    public String getMobile_no() {
        return mobile_no;
    }

    public void setMobile_no(String mobile_no) {
        this.mobile_no = mobile_no;
    }

    public String getEmail_tx() {
        return email_tx;
    }

    public void setEmail_tx(String email_tx) {
        this.email_tx = email_tx;
    }
}