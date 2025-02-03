package FYP;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ExceldatabaseService {

    @Autowired
    private DatabaseSampleRepository databaseSampleRepository;
    
    @Autowired
    private PdfdatabaseRepository pdfdatabaseRepository;
    
    public List<Exceldatabase> getAllRecords() {
        return databaseSampleRepository.findAll();
    }

    // Method to find existing record using all fields
    public Exceldatabase findExistingRecord(String custNameTx, 
                                            String tradingName, 
                                            String ctcName, 
                                            String mobileNo, 
                                            String emailTx) {
        // Use the repository's findAll method and stream to find a match
        return databaseSampleRepository.findAll().stream()
            .filter(record -> 
                nullSafeEquals(record.getCust_name_tx(), custNameTx) &&
                nullSafeEquals(record.getTrading_name(), tradingName) &&
                nullSafeEquals(record.getCtc_name(), ctcName) &&
                nullSafeEquals(record.getMobile_no(), mobileNo) &&
                nullSafeEquals(record.getEmail_tx(), emailTx))
            .findFirst()
            .orElse(null);
    }

    // Utility method to safely compare strings, handling null values
    private boolean nullSafeEquals(String str1, String str2) {
        if (str1 == null && str2 == null) return true;
        if (str1 == null || str2 == null) return false;
        return str1.trim().equalsIgnoreCase(str2.trim());
    }

    // Method to save a new record
    public Exceldatabase saveRecord(Exceldatabase record) {
        return databaseSampleRepository.save(record);
    }
    
 // Method to save a new record
    public Pdfdatabase saveRecord(Pdfdatabase record) {
        return pdfdatabaseRepository.save(record);
    }
}