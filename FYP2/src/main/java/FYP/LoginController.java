/**
 * 
 * I declare that this code was written by me, limji. 
 * I will not copy or allow others to copy my code. 
 * I understand that copying code is considered as plagiarism.
 * 
 * Student Name: Lim Jia Wei
 * Student ID: 22007259
 * Class: E63C
 * Date created: 2024-Jan-10 12:28:17 PM 
 * 
 */

package FYP;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;

/**
 * @author limji
 *
 */
@Controller
public class LoginController {
	
	@GetMapping("/login")
	public String login() {
		return "login";
	}
	
}
