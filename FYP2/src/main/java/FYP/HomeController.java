package FYP;

import java.io.IOException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.validation.BindingResult;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import jakarta.validation.Valid;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.core.io.Resource;
import java.io.File;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.http.ResponseEntity;

import java.io.*;
import java.util.*;

@Controller
public class HomeController {

	@Autowired
	private MemberRepository memberRepository;
	
	@Autowired
	private ExceldatabaseService exceldatabaseService;
	
	@Autowired
	private FormRepository formRepository;
			
	@Autowired
	private DatabaseSampleRepository databaseSampleRepository;
	 
	//@Autowired
	//private PdfService pdfService; 

	@GetMapping("/403")
	public String error403() {
		return "403";
	}

	// Register
	@GetMapping("/admin/register")
	public String addMember(Model model) {
		model.addAttribute("member", new Member());
		return "Register";
	}

	@PostMapping("/admin/registered")
    public String saveMember(@Valid Member member, BindingResult bindingResult, Model model, RedirectAttributes redirectAttribute) {
        // First check for validation errors
        if (bindingResult.hasErrors()) {
            return "Register";
        }
        
        // Check for duplicate NRIC
        if (memberRepository.existsByNric(member.getNric())) {
            model.addAttribute("nricError", "This NRIC is already registered");
            return "Register";
        }
        
        // Proceed with registration if no duplicates
        BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
        String encodedPassword = passwordEncoder.encode(member.getPassword());
        member.setPassword(encodedPassword);
        member.setRole("ROLE_ADMIN");
        
        // Save the member
        memberRepository.save(member);
        
        // Send confirmation email
        try {
        	exceldatabaseService.sendRegistrationConfirmationEmail(member);
            redirectAttribute.addFlashAttribute("success", "Admin registered! A confirmation email has been sent.");
        } catch (Exception e) {
            System.err.println("Failed to send confirmation email: " + e.getMessage());
            redirectAttribute.addFlashAttribute("success", "Admin registered! (Email notification failed)");
        }
        
        return "redirect:/";
    }
	
	@GetMapping("/")
	public String viewForm() {
		return "index";
    }
	
	@GetMapping("/mockpass")
	public String Loginmockpass() {
		return "mockpass";
	}
	

	
}
