package FYP;
	
	import org.apache.poi.ss.usermodel.Row;
	import org.apache.poi.ss.usermodel.Sheet;
	import org.apache.poi.ss.usermodel.Workbook;
	import org.apache.poi.ss.usermodel.WorkbookFactory;
	import org.apache.pdfbox.pdmodel.PDDocument;
	import org.apache.pdfbox.pdmodel.PDDocumentCatalog;
	import org.apache.pdfbox.pdmodel.interactive.form.PDAcroForm;
	import org.apache.pdfbox.pdmodel.interactive.form.PDField;
import org.apache.pdfbox.text.PDFTextStripper;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
	import org.springframework.http.MediaType;
	import org.springframework.http.ResponseEntity;
	import org.springframework.stereotype.Controller;
	import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
	import org.springframework.web.bind.annotation.RequestParam;
	import org.springframework.web.multipart.MultipartFile;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import java.io.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
	
	@Controller
	public class FormController {
	
	    private List<File> filledForms = new ArrayList<>();
	    
	    //@PersistenceContext
	    //private EntityManager entityManager;
	 
	    @Autowired
	    private DatabaseSampleRepository databaseSampleRepository;
	    
	    @Autowired
	    private ExceldatabaseService exceldatabaseService;
	    
	    @Autowired
	    private PdfdatabaseRepository pdfDatabaseRepository;
	    
	    @PostMapping("/uploadExcel")
	    @Transactional
	    public String uploadExcel(@RequestParam("file") MultipartFile file, Model model) {
	        try (InputStream excelStream = file.getInputStream()) {
	            Workbook workbook = WorkbookFactory.create(excelStream);
	            Sheet sheet = workbook.getSheetAt(0);

	            // Initialize validation results
	            ValidationResult validationResult = new ValidationResult();
	            
	            // Validate data and collect empty cell information
	            validateExcelData(sheet, validationResult);

	            // Process the data regardless of empty cells
	            UploadResult result = processValidExcelData(sheet);
	            
	            // Prepare the message for the user
	            StringBuilder messageBuilder = new StringBuilder();
	            messageBuilder.append(String.format("Excel file processed. Total records uploaded: %d, New records added: %d, Duplicate records: %d", 
	                result.getTotalRecords(), result.getNewRecords(), result.getDuplicateRecords()));
	            
	            // Add empty cell warnings if any
	            if (!validationResult.getEmptyCells().isEmpty()) {
	                messageBuilder.append("\n\nEmpty cells found in the following locations:");
	                for (String emptyCell : validationResult.getEmptyCells()) {
	                    messageBuilder.append("\n").append(emptyCell);
	                }
	            }

	            // Add phone format warnings if any
	            if (!validationResult.getInvalidPhones().isEmpty()) {
	                messageBuilder.append("\n\nInvalid phone numbers found in the following locations:");
	                for (String invalidPhone : validationResult.getInvalidPhones()) {
	                    messageBuilder.append("\n").append(invalidPhone);
	                }
	            }

	            // Add email format warnings if any
	            if (!validationResult.getInvalidEmails().isEmpty()) {
	                messageBuilder.append("\n\nInvalid email formats found in the following locations:");
	                for (String invalidEmail : validationResult.getInvalidEmails()) {
	                    messageBuilder.append("\n").append(invalidEmail);
	                }
	            }
	            
	            // Add timestamp to message
	            messageBuilder.append(String.format("Excel file processed at %s. ", 
	                LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss"))));
	            

	            model.addAttribute("message", messageBuilder.toString());
	        } catch (Exception e) {
	            e.printStackTrace();
	            model.addAttribute("error", "Failed to process the Excel file: " + e.getMessage());
	        }
	        return "index";
	    }

	    // New class to track validation issues
	    private class ValidationResult {
	        private List<String> emptyCells = new ArrayList<>();
	        private List<String> invalidPhones = new ArrayList<>();
	        private List<String> invalidEmails = new ArrayList<>();

	        public List<String> getEmptyCells() { return emptyCells; }
	        public List<String> getInvalidPhones() { return invalidPhones; }
	        public List<String> getInvalidEmails() { return invalidEmails; }

	        public void addEmptyCell(String location) { emptyCells.add(location); }
	        public void addInvalidPhone(String location) { invalidPhones.add(location); }
	        public void addInvalidEmail(String location) { invalidEmails.add(location); }
	    }

	    private void validateExcelData(Sheet sheet, ValidationResult validationResult) {
	        // Get column headers for validation
	        Map<Integer, String> columnHeaders = new HashMap<>();
	        Row headerRow = sheet.getRow(0);
	        for (int i = 0; i < headerRow.getLastCellNum() && i < 6; i++) {
	            columnHeaders.put(i, headerRow.getCell(i).getStringCellValue());
	        }
	        
	        // Check each row starting from row 2 (index 1)
	        for (int rowNum = 1; rowNum <= sheet.getLastRowNum(); rowNum++) {
	            Row row = sheet.getRow(rowNum);
	            if (row == null) continue;

	            // Check each cell in columns A to F
	            for (int colNum = 0; colNum < 6; colNum++) {
	                org.apache.poi.ss.usermodel.Cell cell = row.getCell(colNum);
	                String cellValue = getCellValueAsString(cell);
	                
	                if (cellValue == null || cellValue.trim().isEmpty()) {
	                    // Convert column number to letter (0=A, 1=B, etc.)
	                    char columnLetter = (char) ('A' + colNum);
	                    validationResult.addEmptyCell(String.format("Cell %c%d (%s)", 
	                        columnLetter, rowNum + 1, columnHeaders.get(colNum)));
	                }

	                // Validate phone format for column E (index 4)
	                if (colNum == 4 && !cellValue.trim().isEmpty()) {
	                    String phoneValidation = isValidSingaporePhone(cellValue.trim());
	                    if (phoneValidation != null) {
	                        validationResult.addInvalidPhone(String.format("Cell E%d: %s - %s", 
	                            rowNum + 1, cellValue, phoneValidation));
	                    }
	                }

	                // Validate email format for column F (index 5)
	                if (colNum == 5 && !cellValue.trim().isEmpty()) {
	                    if (!isValidEmail(cellValue.trim())) {
	                        validationResult.addInvalidEmail(String.format("Cell F%d: %s", 
	                            rowNum + 1, cellValue));
	                    }
	                }
	            }
	        }
	    }
	    
	 // New class to track upload results
	    private class UploadResult {
	        private int totalRecords = 0;
	        private int newRecords = 0;
	        private int duplicateRecords = 0;

	        public int getTotalRecords() { return totalRecords; }
	        public int getNewRecords() { return newRecords; }
	        public int getDuplicateRecords() { return duplicateRecords; }

	        public void incrementTotalRecords() { totalRecords++; }
	        public void incrementNewRecords() { newRecords++; }
	        public void incrementDuplicateRecords() { duplicateRecords++; }
	    }

	    private String validateExcelData(Sheet sheet) {
	        // Get column headers for validation
	        Map<Integer, String> columnHeaders = new HashMap<>();
	        Row headerRow = sheet.getRow(0);
	        for (int i = 0; i < headerRow.getLastCellNum() && i < 6; i++) {
	            columnHeaders.put(i, headerRow.getCell(i).getStringCellValue());
	        }
	        
	     // New validation for column A (index 0)
	        List<Integer> columnAValues = new ArrayList<>();
	        
	        // Check each row starting from row 2 (index 1)
	        for (int rowNum = 1; rowNum <= sheet.getLastRowNum(); rowNum++) {
	            Row row = sheet.getRow(rowNum);
	            if (row == null) {
	                return String.format("Empty row found at row %d", rowNum + 1);
	            }

	            // Validate Column A specific checks
	            org.apache.poi.ss.usermodel.Cell columnACell = row.getCell(0);
	            String columnAValue = getCellValueAsString(columnACell);
	            
	            // Ensure column A is not empty
	            if (columnAValue == null || columnAValue.trim().isEmpty()) {
	                return String.format("Empty cell found at A%d. Column A must be filled.", rowNum + 1);
	            }

	            // Try to parse the value as an integer
	            int sequenceNumber;
	            try {
	                sequenceNumber = Integer.parseInt(columnAValue.trim());
	            } catch (NumberFormatException e) {
	                return String.format("Invalid number in column A at A%d. Must be a valid integer.", rowNum + 1);
	            }

	            // Check if it starts from 1 in the first row
	            if (rowNum == 1 && sequenceNumber != 1) {
	                return "Column A must start with 1 in the first data row.";
	            }

	            // Check for sequential and non-duplicate values
	            if (columnAValues.contains(sequenceNumber)) {
	                return String.format("Duplicate value %d found in column A at row %d. Values must be unique.", 
	                    sequenceNumber, rowNum + 1);
	            }

	            // Add to list of checked values
	            columnAValues.add(sequenceNumber);

	            // Optional: If you want strictly sequential numbers
	            if (rowNum > 1) {
	                int expectedValue = columnAValues.get(rowNum - 2) + 1;
	                if (sequenceNumber != expectedValue) {
	                    return String.format("Sequence in column A is not continuous. Expected %d, found %d at row %d.", 
	                        expectedValue, sequenceNumber, rowNum + 1);
	                }
	            }
	        }

	        // Check each row starting from row 2 (index 1)
	        for (int rowNum = 1; rowNum <= sheet.getLastRowNum(); rowNum++) {
	            Row row = sheet.getRow(rowNum);
	            if (row == null) {
	                return String.format("Empty row found at row %d", rowNum + 1);
	            }

	            // Check each cell in columns A to F
	            for (int colNum = 0; colNum < 6; colNum++) {
	                org.apache.poi.ss.usermodel.Cell cell = row.getCell(colNum);
	                String cellValue = getCellValueAsString(cell);
	                
	                if (cellValue == null || cellValue.trim().isEmpty()) {
	                    // Convert column number to letter (0=A, 1=B, etc.)
	                    char columnLetter = (char) ('A' + colNum);
	                    return String.format("Empty cell found at %c%d. Please make sure all cells in columns A to F are filled.", 
	                        columnLetter, rowNum + 1);
	                }

	                // Validate phone format for column E (index 4)
	                if (colNum == 4) { // Column E
	                    String phoneValidation = isValidSingaporePhone(cellValue.trim());
	                    if (phoneValidation != null) {
	                        return String.format("Invalid phone number at E%d: %s", rowNum + 1, phoneValidation);
	                    }
	                }

	                // Validate email format for column F (index 5)
	                if (colNum == 5) {  // Column F
	                    if (!isValidEmail(cellValue.trim())) {
	                        return String.format("Invalid email format found at F%d: %s", 
	                            rowNum + 1, cellValue);
	                    }
	                }
	            }
	        }
	        return null; // Return null if no validation errors found
	    }

	    private String isValidSingaporePhone(String phone) {
	        // Remove all non-digit characters
	        String cleanPhone = phone.replaceAll("[^0-9]", "");
	        
	        // Check basic format
	        if (cleanPhone.length() != 8) {
	            return "Mobile numbers must be 8 digits long ";
	        }

	        // Check first digit (must be 8 or 9 for mobile numbers)
	        char firstDigit = cleanPhone.charAt(0);
	        if (firstDigit != '8' && firstDigit != '9') {
	            return "Mobile numbers must start with 8 or 9";
	        }

	        // Check for repeated digits that might indicate invalid numbers
	        boolean hasRepeatedDigits = false;
	        char[] digits = cleanPhone.toCharArray();
	        int repeatCount = 1;
	        for (int i = 1; i < digits.length; i++) {
	            if (digits[i] == digits[i-1]) {
	                repeatCount++;
	                if (repeatCount >= 6) { // If same digit repeats 6 or more times
	                    hasRepeatedDigits = true;
	                    break;
	                }
	            } else {
	                repeatCount = 1;
	            }
	        }
	        
	        if (hasRepeatedDigits) {
	            return "Invalid phone number pattern (Repeated digits)";
	        }

	        return null; // Return null if phone number is valid
	    }

	    private boolean isValidEmail(String email) {
	        // Basic email validation pattern
	        String emailPattern = 
	            "^[a-zA-Z0-9_+&*-]+(?:\\.[a-zA-Z0-9_+&*-]+)*@" + "(?:[a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,7}$";
	        
	        if (email == null || email.isEmpty()) {
	            return false;
	        }

	        // Additional validation checks
	        if (email.length() > 254) {  // RFC 5321
	            return false;
	        }

	        // Check for common invalid patterns
	        if (email.contains("..") || 
	            email.startsWith(".") || 
	            email.endsWith(".") ||
	            !email.contains("@") ||
	            email.indexOf('@') != email.lastIndexOf('@')) {
	            return false;
	        }

	        return email.matches(emailPattern);
	    }

	    private UploadResult processValidExcelData(Sheet sheet) {
	        UploadResult result = new UploadResult();

	        // Process each row starting from row 2 (index 1)
	        for (int i = 1; i <= sheet.getLastRowNum(); i++) {
	            Row row = sheet.getRow(i);
	            if (row == null) continue;

	            result.incrementTotalRecords();

	            // Get column indices
	            int custNameIdx = getColumnIndex(sheet, "CUST_NAME_TX");
	            int tradingNameIdx = getColumnIndex(sheet, "Trading Name");
	            int ctcNameIdx = getColumnIndex(sheet, "CTC-Name");
	            int mobileNoIdx = getColumnIndex(sheet, "MOBILE_NO");
	            int emailIdx = getColumnIndex(sheet, "email_tx");

	            // Read values
	            String custName = getCellValueAsString(row.getCell(custNameIdx));
	            String tradingName = getCellValueAsString(row.getCell(tradingNameIdx));
	            String ctcName = getCellValueAsString(row.getCell(ctcNameIdx));
	            String mobileNo = getCellValueAsString(row.getCell(mobileNoIdx));
	            String email = getCellValueAsString(row.getCell(emailIdx));

	            // Check if record already exists
	            Exceldatabase existingRecord = exceldatabaseService.findExistingRecord(
	                custName, tradingName, ctcName, mobileNo, email
	            );

	            if (existingRecord != null) {
	                result.incrementDuplicateRecords();
	                // Log duplicate record details if needed
	                System.out.println("Duplicate record found: " + custName);
	                continue; // Skip duplicate records
	            }

	            // Create and save new record
	            Exceldatabase dbSample = new Exceldatabase();
	            dbSample.setCust_name_tx(custName);
	            dbSample.setTrading_name(tradingName);
	            dbSample.setCtc_name(ctcName);
	            dbSample.setMobile_no(mobileNo);
	            dbSample.setEmail_tx(email);
	            
	            // Save to database
	            Exceldatabase savedRecord = exceldatabaseService.saveRecord(dbSample);
	            System.out.println("Saved record with ID: " + savedRecord.getId());
	            result.incrementNewRecords();
	        }

	        return result;
	    }
	

	    // Existing helper methods remain the same
	    private String getCellValueAsString(org.apache.poi.ss.usermodel.Cell cell) {
	        if (cell == null) {
	            return "";
	        }
	        switch (cell.getCellType()) {
	            case NUMERIC:
	                double value = cell.getNumericCellValue();
	                if (value == (long) value) {
	                    return String.format("%.0f", value);
	                } else {
	                    return String.valueOf(value);
	                }
	            case STRING:
	                return cell.getStringCellValue();
	            case BOOLEAN:
	                return String.valueOf(cell.getBooleanCellValue());
	            case FORMULA:
	                try {
	                    return String.valueOf(cell.getNumericCellValue());
	                } catch (IllegalStateException e) {
	                    return cell.getStringCellValue();
	                }
	            case BLANK:
	                return "";
	            default:
	                return "";
	        }
	    }

	    private int getColumnIndex(Sheet sheet, String headerName) {
	        Row headerRow = sheet.getRow(0);
	        for (int i = 0; i < headerRow.getLastCellNum(); i++) {
	            if (headerRow.getCell(i).getStringCellValue().equalsIgnoreCase(headerName)) {
	                return i;
	            }
	        }
	        throw new IllegalArgumentException("Column " + headerName + " not found");
	    }
	    
	    @GetMapping("/previewPage")
	    public String showPreviewPage(Model model) {
	        List<Exceldatabase> records = exceldatabaseService.getAllRecords();
	        model.addAttribute("records", records);
	        return "preview";
	    }
	    
	    // Update the existing preview endpoint to be more specific
	    @GetMapping("/downloadForms")
	    public ResponseEntity<InputStreamResource> downloadForms() {
			return null;
	        // Move the existing preview endpoint code here
	        // ... existing preview download code ...
	    }
	    
		//edit Preview
		@GetMapping("/info/edit/{id}")
		public String editinfo(@PathVariable("id") Integer id, Model model) {

			Exceldatabase record = databaseSampleRepository.getReferenceById(id);
			model.addAttribute("record", record);

			return "edit_info";
		}

	    @PostMapping("/info/edit/{id}") 
	    public String saveUpdatedInfo(@PathVariable("id") Integer id,@Valid Exceldatabase record, BindingResult bindingResult) { 
	    	if(bindingResult.hasErrors()) { 
	    		return "edit_info"; 
	        } 
	           	databaseSampleRepository.save(record); 
	           	return "redirect:/previewPage"; 
	        } 
	    
	    @PostMapping("/uploadPDF")
	    @Transactional
	    public String uploadPDF(@RequestParam("file") MultipartFile file, Model model) {
	        try {
	            // Load PDF document
	            PDDocument document = PDDocument.load(file.getInputStream());
	            String pdfText = new PDFTextStripper().getText(document);
	            
	            // Create new database entity
	            Pdfdatabase pdfData = new Pdfdatabase();
	            
	            // Extract company name
	            String companyName = extractValue(pdfText, "Name of Company :", "\n");
	            pdfData.setCompanyName(companyName.trim());
	            
	            // Extract UEN
	            String uen = extractValue(pdfText, "UEN :", "\n");
	            pdfData.setUen(uen.trim());
	            
	            // Extract trading name (company name without PTE. LTD.)
	            String tradingName = companyName.replaceAll("(?i)\\s+PTE\\.?\\s+LTD\\.?$", "").trim();
	            if (tradingName.length() > 25) {
	                tradingName = tradingName.substring(0, 25); // Truncate at 25 characters
	            }
	            pdfData.setTradingName(tradingName);

	            
	            // Extract postal code
	            String address = extractValue(pdfText, "Registered Office Address :", "Date of Address");
	            String postalCode = extractPostalCode(address);
	            pdfData.setPostalCode(postalCode);
	            
	            // Extract director name
	            String officerSection = extractValue(pdfText, "Officer(s)", "Shareholder(s)");
	            String directorName = extractDirectorName(officerSection);
	            pdfData.setDirectorName(directorName);
	            
	            // Extract business entity type
	            String businessEntityType = extractValue(pdfText, "Company Type :", "\n");
	            if (isACRADocument(pdfText)) {
	                pdfData.setBusinessEntityType("REGISTERED WITH ACRA");  // Check that it is registered with ACRA
	                System.out.println("Company Type: " + businessEntityType); // Debugging log
	            } else {
	                pdfData.setBusinessEntityType("OTHERS"); // Fallback in case it's not an ACRA document
	                System.out.println("Business Entity Type set to: Others");
	            }

	            
	            // Save to database
	            pdfDatabaseRepository.save(pdfData);
	            
	            document.close();
	            model.addAttribute("message", "PDF processed successfully");
	            return "index";
	            
	        } catch (Exception e) {
	            e.printStackTrace();
	            model.addAttribute("error", "Failed to process PDF: " + e.getMessage());
	            return "index";
	        }
	    }

	    private boolean isACRADocument(String extractedText) {
	        if (extractedText == null || extractedText.trim().isEmpty()) {
	            return false;
	        }

	        // Debugging: Print extracted text for verification
	        System.out.println("Extracted text (first 500 chars):\n" + extractedText.substring(0, Math.min(500, extractedText.length())));

	        // Ensure extracted text is normalized to avoid issues with spaces or newlines
	        extractedText = extractedText.replaceAll("\\s+", " ").trim();  // Replace multiple spaces/newlines with single space

	        // List of required ACRA keywords
	        boolean hasAcraHeader = extractedText.contains("ACCOUNTING AND CORPORATE REGULATORY AUTHORITY");
	        boolean hasBusinessProfile = extractedText.contains("Business Profile");
	        boolean hasUEN = extractedText.contains("UEN :");
	        boolean hasCompanyName = extractedText.contains("Name of Company :");
	        boolean hasAddress = extractedText.contains("Registered Office Address :");
	        
	        // Check for the signature section (as a sign of official ACRA document)
	     // Check for the signature section, Receipt Number, and Date together
	        boolean hasSignatureSection = extractedText.matches("(?i).ASST REGISTRAR OF COMPANIES & BUSINESS NAMES.")
	                && extractedText.matches("(?i).ACCOUNTING AND CORPORATE REGULATORY AUTHORITY \\(ACRA\\).")
	                && extractedText.matches("(?i).RECEIPT NO\\.\\s:\\s*ACRA\\d{12}.*")
	                && extractedText.matches("(?i).DATE\\s:\\s*\\d{2} [A-Z]{3} \\d{4}.*");

	        // Debugging: Log which conditions are matched
	        System.out.println("ACRA Header Found: " + hasAcraHeader);
	        System.out.println("Business Profile Found: " + hasBusinessProfile);
	        System.out.println("UEN Found: " + hasUEN);
	        System.out.println("Company Name Found: " + hasCompanyName);
	        System.out.println("Address Found: " + hasAddress);
	        System.out.println("Signature Section (with Receipt No. & Date) Found: " + hasSignatureSection);

	        // All conditions must be true for a valid ACRA document
	        boolean isValidAcraDocument = hasAcraHeader && hasBusinessProfile && hasUEN && hasCompanyName && hasAddress && hasSignatureSection;

	        System.out.println("Final ACRA Document Status: " + isValidAcraDocument); // ✅ Debugging log

	        return isValidAcraDocument;
	    }



	    private String extractValue(String text, String startMarker, String endMarker) {
	        int startIndex = text.indexOf(startMarker);
	        if (startIndex == -1) return "";
	        
	        startIndex += startMarker.length();
	        int endIndex = text.indexOf(endMarker, startIndex);
	        
	        if (endIndex == -1) {
	            return text.substring(startIndex).trim();
	        }
	        
	        return text.substring(startIndex, endIndex).trim();
	    }

	    private String extractPostalCode(String address) {
	        if (address == null) return "";
	        
	        String regex = "\\((\\d{6})\\)";
	        java.util.regex.Pattern pattern = java.util.regex.Pattern.compile(regex);
	        java.util.regex.Matcher matcher = pattern.matcher(address);
	        
	        if (matcher.find()) {
	            return matcher.group(1);
	        }
	        return "";
	    }

	    private static String extractDirectorName(String officerSection) {
	    	if (officerSection == null || officerSection.isEmpty()) {
	            return "";
	        }

	        // Split the text into lines
	        String[] lines = officerSection.split("\n");
	        List<String> cleanedLines = new ArrayList<>();

	        // Debug: Print all lines with indexes
	        System.out.println("Lines:");
	        for (int i = 0; i < lines.length; i++) {
	            String trimmedLine = lines[i].trim();
	            cleanedLines.add(trimmedLine);
	            System.out.println(i + ": " + trimmedLine);
	        }

	        String lastSeenName = "";  // Store the last detected name

	        for (int i = 0; i < cleanedLines.size(); i++) {
	            String line = cleanedLines.get(i);

	            // Check if the line contains "DIRECTOR"
	            if (line.contains("DIRECTOR")) {
	                System.out.println("Found 'DIRECTOR' at line: " + i + " -> " + line);

	                // Return the last stored name (ZHOU LIZHONG)
	                if (!lastSeenName.isEmpty()) {
	                    System.out.println("Extracted Director Name at line " + i + ": " + lastSeenName);
	                    return lastSeenName;
	                }
	            }

	            // Store name (assuming uppercase names are at the start of lines)
	            if (i > 0 && cleanedLines.get(i - 1).equals("Address")) {
	                System.out.println("Detected Name at line: " + i + " -> " + line);
	                lastSeenName = line;
	            }
	        }

	        return ""; // Return empty if no director is found
	    }

	    
	    @GetMapping("/previewPDFPage")
	    public String showPreviewPDFPage(Model model) {
	        List<Pdfdatabase> records = exceldatabaseService.getRecords();
	        model.addAttribute("records", records);
	        return "previewPDF";
	    }
	    
	  //edit Preview
	  @GetMapping("/pdf/edit/{id}")
	  public String editpdfinfo(@PathVariable("id") Integer id, Model model) {

	  	Pdfdatabase record = pdfDatabaseRepository.getReferenceById(id);
	  	model.addAttribute("record", record);

	  	return "edit_pdf";
	  }

	  @PostMapping("/pdf/edit/{id}") 
	  public String saveUpdatedpdfInfo(@PathVariable("id") Integer id,@Valid Pdfdatabase record, BindingResult bindingResult) { 
		  if(bindingResult.hasErrors()) { 
			  return "edit_pdf"; 
	  	    } 
		  pdfDatabaseRepository.save(record); 
	  	        return "redirect:/previewPDFPage"; 
	  	    } 
//	    
//	    @GetMapping("/preview")
//	    public ResponseEntity<InputStreamResource> previewAndDownloadForms() {
//	        try {
//	            // Get all records from database
//	            List<Exceldatabase> allRecords = exceldatabaseService.getAllRecords();
//	            
//	            // Create filled forms for each record
//	            for (Exceldatabase record : allRecords) {
//	                File filledForm = fillPdfForm(
//	                    record.getCust_name_tx(),
//	                    record.getTrading_name(),
//	                    record.getCtc_name(),
//	                    record.getMobile_no(),
//	                    record.getEmail_tx()
//	                );
//	                filledForms.add(filledForm);
//	            }
//
//	            // Create a new document for merging
//	            PDDocument mergedDoc = new PDDocument();
//	            List<PDDocument> tempDocs = new ArrayList<>();
//	            
//	            try {
//	                // Process each filled form
//	                for (File filledForm : filledForms) {
//	                    if (filledForm.exists()) {
//	                        PDDocument doc = PDDocument.load(filledForm);
//	                        tempDocs.add(doc);
//	                        
//	                        // Import all pages from the current document
//	                        for (int i = 0; i < doc.getNumberOfPages(); i++) {
//	                            mergedDoc.addPage(doc.getPage(i));
//	                        }
//	                    }
//	                }
//	                
//	                // Create output stream for the merged document
//	                ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
//	                mergedDoc.save(outputStream);
//	                
//	                // Clean up temporary files
//	                cleanupTemporaryFiles(filledForms);
//	                
//	                // Prepare the response
//	                HttpHeaders headers = new HttpHeaders();
//	                headers.setContentType(MediaType.APPLICATION_PDF);
//	                headers.setContentDisposition(ContentDisposition.attachment()
//	                    .filename("Merged_Application_Forms.pdf")
//	                    .build());
//	                
//	                InputStreamResource resource = new InputStreamResource(
//	                    new ByteArrayInputStream(outputStream.toByteArray())
//	                );
//	                
//	                return ResponseEntity.ok()
//	                    .headers(headers)
//	                    .contentLength(outputStream.size())
//	                    .body(resource);
//	                    
//	            } finally {
//	                // Cleanup
//	                cleanupDocuments(tempDocs, mergedDoc);
//	            }
//	            
//	        } catch (Exception e) {
//	            e.printStackTrace();
//	            return ResponseEntity.internalServerError().build();
//	        }
//	    }
//
//	    private File fillPdfForm(String custName, String tradingName, String ctcName, String mobileNo, String email) throws IOException {
//	        // Load your PDF template
//	        File template = new File("path/to/your/template.pdf");
//	        PDDocument pdfDoc = PDDocument.load(template);
//	        
//	        PDDocumentCatalog documentCatalog = pdfDoc.getDocumentCatalog();
//	        PDAcroForm acroForm = documentCatalog.getAcroForm();
//	        
//	        if (acroForm != null) {
//	            // Map field names to values
//	            Map<String, String> fieldMappings = new HashMap<>();
//	            fieldMappings.put("Registered Name:", custName);
//	            fieldMappings.put("Trading Name (Max 25 characters)", tradingName);
//	            fieldMappings.put("Applicant's Mobile:", ctcName);
//	            fieldMappings.put("Office:", mobileNo);
//	            fieldMappings.put("Email Address:", email);
//	            
//	            // Fill form fields
//	            for (PDField field : acroForm.getFields()) {
//	                String fieldName = field.getFullyQualifiedName();
//	                for (Map.Entry<String, String> mapping : fieldMappings.entrySet()) {
//	                    if (fieldName.contains(mapping.getKey())) {
//	                        try {
//	                            field.setValue(mapping.getValue());
//	                        } catch (IOException e) {
//	                            System.err.println("Error setting " + fieldName + ": " + e.getMessage());
//	                        }
//	                    }
//	                }
//	            }
//
//	            // Flatten the form
//	            acroForm.flatten();
//	        }
//
//	        // Save the filled form with a unique name
//	        String timestamp = String.valueOf(System.currentTimeMillis());
//	        File filledForm = new File("filled_form_" + custName.replaceAll("[^a-zA-Z0-9.-]", "_") + "_" + timestamp + ".pdf");
//	        pdfDoc.save(filledForm);
//	        pdfDoc.close();
//
//	        return filledForm;
//	    }
//
//	    private void cleanupTemporaryFiles(List<File> files) {
//	        for (File file : files) {
//	            try {
//	                if (file.exists()) {
//	                    file.delete();
//	                }
//	            } catch (Exception e) {
//	                System.err.println("Failed to delete temporary file: " + file.getName());
//	            }
//	        }
//	        files.clear();
//	    }
//
//	    private void cleanupDocuments(List<PDDocument> tempDocs, PDDocument mergedDoc) {
//	        // Close all temporary documents
//	        for (PDDocument doc : tempDocs) {
//	            try {
//	                if (doc != null) {
//	                    doc.close();
//	                }
//	            } catch (IOException e) {
//	                System.err.println("Error closing document: " + e.getMessage());
//	            }
//	        }
//	        
//	        // Close merged document
//	        if (mergedDoc != null) {
//	            try {
//	                mergedDoc.close();
//	            } catch (IOException e) {
//	                System.err.println("Error closing merged document: " + e.getMessage());
//	            }
//	        }
//	    }
//	
//	    private File fillPdfForm(String custName, String tradingName, String ctcName, String mobileNo, String email) throws IOException {
//	        File template = new File("C:\\Users\\limji\\Downloads\\NETS Application Form_PIF_A_v11-1._Clean.pdf");
//	        PDDocument pdfDoc = PDDocument.load(template);
//	        
//	        PDDocumentCatalog documentCatalog = pdfDoc.getDocumentCatalog();
//	        PDAcroForm acroForm = documentCatalog.getAcroForm();
//	        
//	        if (acroForm != null) {
//	            // Debug: Print all form fields first to see their exact names
//	            System.out.println("\nAvailable PDF Form Fields:");
//	            for (PDField field : acroForm.getFields()) {
//	                System.out.println("Field Name: '" + field.getFullyQualifiedName() + "'");
//	            }
//
//	            // Try to find and fill the Registered Name field
//	            for (PDField field : acroForm.getFields()) {
//	                String fieldName = field.getFullyQualifiedName();
//	                
//	                // Print field being checked
//	                System.out.println("Checking field: " + fieldName);
//
//	                // Check for the exact field name "Registered Name:"
//	                if (fieldName.contains("Registered Name:")) {
//	                    try {
//	                        System.out.println("Found Registered Name field. Attempting to set value: " + custName);
//	                        field.setValue(custName);
//	                        System.out.println("Successfully set value for " + fieldName);
//	                    } catch (IOException e) {
//	                        System.err.println("Error setting value for " + fieldName + ": " + e.getMessage());
//	                        e.printStackTrace();
//	                    }
//	                }
//
//	                // Similar checks for other fields
//	                if (fieldName.contains("Trading Name (Max 25 characters)")) {
//	                    try {
//	                        field.setValue(tradingName);
//	                    } catch (IOException e) {
//	                        System.err.println("Error setting Trading Name: " + e.getMessage());
//	                    }
//	                }
//	                
//	                if (fieldName.contains("Applicant's Mobile:")) {
//	                    try {
//	                        field.setValue(ctcName);
//	                    } catch (IOException e) {
//	                        System.err.println("Error setting Contact Person: " + e.getMessage());
//	                    }
//	                }
//	                
//	                if (fieldName.contains("Office")) {
//	                    try {
//	                        field.setValue(mobileNo);
//	                    } catch (IOException e) {
//	                        System.err.println("Error setting Mobile Number: " + e.getMessage());
//	                    }
//	                }
//	                
//	                if (fieldName.contains("Email Address:")) {
//	                    try {
//	                        field.setValue(email);
//	                    } catch (IOException e) {
//	                        System.err.println("Error setting Email: " + e.getMessage());
//	                    }
//	                }
//	            }
//
//	            // Print the values after setting them
//	            System.out.println("\nField values after setting:");
//	            for (PDField field : acroForm.getFields()) {
//	                System.out.println(field.getFullyQualifiedName() + ": " + field.getValueAsString());
//	            }
//
//	            // Flatten the form
//	            acroForm.flatten();
//	        }
//
//	        // Save the filled form with a unique name
//	        String timestamp = String.valueOf(System.currentTimeMillis());
//	        File filledForm = new File("filled_form_" + custName.replaceAll("[^a-zA-Z0-9.-]", "_") + "_" + timestamp + ".pdf");
//	        pdfDoc.save(filledForm);
//	        pdfDoc.close();
//
//	        return filledForm;
//	    }
//
//	    // Add this helper method to get the exact field names from the PDF
//	    @GetMapping("/getPdfFieldNames")
//	    public ResponseEntity<String> getPdfFieldNames() {
//	        try {
//	            File template = new File("C:\\Users\\limji\\Downloads\\NETS Application Form_PIF_A_v11-1._Clean.pdf");
//	            PDDocument pdfDoc = PDDocument.load(template);
//	            PDDocumentCatalog documentCatalog = pdfDoc.getDocumentCatalog();
//	            PDAcroForm acroForm = documentCatalog.getAcroForm();
//	            
//	            StringBuilder result = new StringBuilder("PDF Form Fields:\n");
//	            
//	            if (acroForm != null) {
//	                for (PDField field : acroForm.getFields()) {
//	                    result.append("Field Name: '")
//	                          .append(field.getFullyQualifiedName())
//	                          .append("' | Type: ")
//	                          .append(field.getClass().getSimpleName())
//	                          .append(" | Current Value: '")
//	                          .append(field.getValueAsString())
//	                          .append("'\n");
//	                }
//	            }
//	            
//	            pdfDoc.close();
//	            return ResponseEntity.ok(result.toString());
//	        } catch (IOException e) {
//	            return ResponseEntity.status(500).body("Error reading PDF fields: " + e.getMessage());
//	        }
//	    }
//	    
//	//    // Helper method to safely set field values
//	//    private void setFieldValue(PDAcroForm acroForm, String fieldName, String value) {
//	//        try {
//	//            PDField field = acroForm.getField(fieldName);
//	//            if (field != null) {
//	//                field.setValue(value);
//	//            } else {
//	//                System.err.println("Field not found: " + fieldName);
//	//            }
//	//        } catch (IOException e) {
//	//            System.err.println("Error setting field " + fieldName + ": " + e.getMessage());
//	//        }
//	//    }
	}