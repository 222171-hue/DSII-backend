package com.medicalcenter.apirsfinalproject.security;

import com.medicalcenter.apirsfinalproject.entity.Role;
import com.medicalcenter.apirsfinalproject.entity.User;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.GrantedAuthority;

import java.util.Collection;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CustomUserDetailsTest {

    @Test
    void getAuthoritiesReturnsRolePrefixed() {
        User user = new User();
        user.setCorreo("juan@unamba.edu.pe");
        user.setPassword("encrypted");
        user.setRol(Role.ADMIN);
        CustomUserDetails details = new CustomUserDetails(user);

        Collection<? extends GrantedAuthority> authorities = details.getAuthorities();
        assertEquals(1, authorities.size());
        assertEquals("ROLE_ADMIN", authorities.iterator().next().getAuthority());
    }

    @Test
    void accessorsReturnUserData() {
        User user = new User();
        user.setCorreo("juan@unamba.edu.pe");
        user.setPassword("encrypted");
        user.setRol(Role.STUDENT);
        CustomUserDetails details = new CustomUserDetails(user);

        assertEquals("juan@unamba.edu.pe", details.getUsername());
        assertEquals("encrypted", details.getPassword());
        assertEquals(user, details.getUser());
        assertTrue(details.isAccountNonExpired());
        assertTrue(details.isAccountNonLocked());
        assertTrue(details.isCredentialsNonExpired());
        assertTrue(details.isEnabled());
    }
}
