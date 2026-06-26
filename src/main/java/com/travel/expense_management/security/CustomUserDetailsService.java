package com.travel.expense_management.security;

import com.travel.expense_management.entity.User;
import com.travel.expense_management.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        User user = userRepository.
                findByEmail(email)
                .orElseThrow(
                        () -> new UsernameNotFoundException(String.format("User not found with email: ", email))
                );
        return UserPrincipal.from(user);
    }
}
