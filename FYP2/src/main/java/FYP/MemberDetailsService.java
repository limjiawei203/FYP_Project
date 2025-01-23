package FYP;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class MemberDetailsService implements UserDetailsService {

    @Autowired
    private MemberRepository memberRepository;

    @Override
    public MemberDetails loadUserByUsername(String nric) throws UsernameNotFoundException {
        Member member = memberRepository.findByNric(nric);

        if (member == null) {
            throw new UsernameNotFoundException("Could not find user");
        }

        return new MemberDetails(member);
    }
}
