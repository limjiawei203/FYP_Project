package FYP;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;

@Entity
@Table(name = "pdf")
public class PDF {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    // Essential company details from ACRA document
    @NotBlank(message = "Company name is required")
    @Column(nullable = false)
    private String companyName;
    
    @NotBlank(message = "UEN is required")
    @Pattern(regexp = "\\d{9}[A-Z]", message = "Invalid UEN format")
    @Column(nullable = false, length = 10)
    private String uen;
    
    @NotBlank(message = "Trading name is required")
    @Column(length = 25)
    private String tradingName;
    
    @NotBlank(message = "Postal Code is required")
    @Pattern(regexp = "\\d{6}", message = "Postal code must be 6 digits")
    @Column(length = 6)
    private String postalCode;
    
    @NotBlank(message = "Name is required")
    @Column
    private String directorName;
    
    // Store only the file path for document retrieval
    @Column(nullable = false)
    private String filePath;
    
    @NotBlank(message = "Business entity type is required")
    @Column(nullable = false)
    private String businessEntityType;
    
    @NotBlank(message = "Country of incorporation is required")
    @Column(nullable = false)
    private String countryOfIncorporation;
    
    @NotBlank(message = "Country of operation is required")
    @Column(nullable = false)
    private String countryOfOperation;

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
    
    public String getFilePath() { 
        return filePath; 
    }
    
    public void setFilePath(String filePath) { 
        this.filePath = filePath; 
    }

    public String getBusinessEntityType() {
        return businessEntityType;
    }
    
    public void setBusinessEntityType(String businessEntityType) {
        this.businessEntityType = businessEntityType;
    }
    
    public String getCountryOfIncorporation() {
        return countryOfIncorporation;
    }
    
    public void setCountryOfIncorporation(String countryOfIncorporation) {
        this.countryOfIncorporation = countryOfIncorporation;
    }
    
    public String getCountryOfOperation() {
        return countryOfOperation;
    }
    
    public void setCountryOfOperation(String countryOfOperation) {
        this.countryOfOperation = countryOfOperation;
    }
}