package FYP;

import java.time.LocalDateTime;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;

@Entity
public class Pdfdatabase {
	
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    private String companyName;
    
    private String uen;

    private String tradingName;

    private String postalCode;
    
    private String directorName;
    
    private String businessEntityType;
    
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
   

    // Basic getters and setters
    public Long getId() { 
        return id; 
    }
    
    public void setId(Long id) { 
        this.id = id; 
    }
    
    public String getCompanyName() { 
        return companyName; 
    }
    
    public void setCompanyName(String companyName) { 
        this.companyName = companyName; 
    }
    
    public String getUen() { 
        return uen; 
    }
    
    public void setUen(String uen) { 
        this.uen = uen; 
    }
    
    public String getTradingName() { 
        return tradingName; 
    }
    
    public void setTradingName(String tradingName) { 
        this.tradingName = tradingName; 
    }
    
    public String getPostalCode() { 
        return postalCode; 
    }
    
    public void setPostalCode(String postalCode) { 
        this.postalCode = postalCode; 
    }
    
    public String getDirectorName() { 
        return directorName; 
    }
    
    public void setDirectorName(String directorName) { 
        this.directorName = directorName; 
    }

    public String getBusinessEntityType() {
        return businessEntityType;
    }
    
    public void setBusinessEntityType(String businessEntityType) {
        this.businessEntityType = businessEntityType;
    }

    
}
