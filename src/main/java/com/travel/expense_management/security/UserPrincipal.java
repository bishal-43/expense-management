package com.travel.expense_management.security;

import com.travel.expense_management.entity.User;
import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;


@Getter
public class UserPrincipal implements UserDetails {

    private final Long id;

    private final String fullName;

    private final String email;

    private final String password;

    private final Collection<?extends GrantedAuthority> authorities;


    private UserPrincipal(
            Long id,
            String fullName,
            String email,
            String password,
            Collection<
                    ? extends GrantedAuthority
            > authorities
    ) {

        this.id = id;

        this.fullName = fullName;

        this.email = email;

        this.password = password;

        this.authorities = authorities;
    }



    public static UserPrincipal from(
            User user
    ) {

        return new UserPrincipal(

                user.getId(),

                user.getFullName(),

                user.getEmail(),

                user.getPassword(),

                List.of(
                        new SimpleGrantedAuthority(
                                "ROLE_"
                                        + user
                                        .getRole()
                                        .name()
                        )
                )
        );
    }


    @Override
    public String getUsername() {

        return email;
    }

    @Override
    public boolean isAccountNonExpired() {

        return true;
    }

    @Override
    public boolean isAccountNonLocked() {

        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {

        return true;
    }

    @Override
    public boolean isEnabled() {

        return true;
    }
}
