/**
 * 
 * I declare that this code was written by me, limji. 
 * I will not copy or allow others to copy my code. 
 * I understand that copying code is considered as plagiarism.
 * 
 * Student Name: Lim Jia Wei
 * Student ID: 22018405
 * Class: C372-3D-E63C-A
 * Date created: 2024-Jan-03 10:10:33 am 
 * 
 */

package FYP;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import jakarta.validation.Valid;

@Controller
public class MemberController {
	@Autowired
	private MemberRepository memberRepository;
	
	@GetMapping("/members")
	public String viewMember(Model model) {

		
		List<Member> listMember = memberRepository.findAll();
		model.addAttribute("listMember", listMember);
		return "view_members";

	}

	// add
	@GetMapping("/members/add")
	public String addMember(Model model) {
		model.addAttribute("member", new Member());
		return "add_member";
	}

	@PostMapping("/members/save")
	public String saveMember(@Valid Member member,BindingResult bindingResult, RedirectAttributes redirectAttribute) {
		if(bindingResult.hasErrors()) {
			return "add_member";
		}
		BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
		String encodedPassword = passwordEncoder.encode(member.getPassword());
		
		member.setPassword(encodedPassword);
		member.setRole("ROLE_USER");
		
		memberRepository.save(member);
			
		redirectAttribute.addFlashAttribute("success", "Member registered!");
		
		return "redirect:/members";
	}

	// edit

	@GetMapping("/members/edit/{id}")
	public String editMember(@PathVariable("id") Integer id, Model model) {

		Member member = memberRepository.getReferenceById(id);
		model.addAttribute("member", member);

		return "edit_member";
	}

	@PostMapping("/members/edit/{id}")
	public String saveUpdatedMember(@PathVariable("id") Integer id,@Valid Member member, BindingResult bindingResult, RedirectAttributes redirectAttribute) {
		if(bindingResult.hasErrors()) {
			return "edit_member";
		}
		BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
		String encodedPassword = passwordEncoder.encode(member.getPassword());
		
		member.setPassword(encodedPassword);
		member.setRole("ROLE_USER");
		
		memberRepository.save(member);
			
		redirectAttribute.addFlashAttribute("success", "Member updated!");
		
		memberRepository.save(member);
		return "redirect:/members";
	}

	// delete

	@GetMapping("/member/delete/{id}")
	public String deleteMember(@PathVariable("id") Integer id) {

		memberRepository.deleteById(id);
	

		return "redirect:/members";
	}

}
