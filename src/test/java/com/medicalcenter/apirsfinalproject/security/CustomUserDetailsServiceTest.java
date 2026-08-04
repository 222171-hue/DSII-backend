package com.medicalcenter.apirsfinalproject.security;

import com.medicalcenter.apirsfinalproject.entity.Role;
import com.medicalcenter.apirsfinalproject.entity.User;
import com.medicalcenter.apirsfinalproject.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CustomUserDetailsServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private CustomUserDetailsService userDetailsService;

    @Test
    void loadUserByUsernameReturnsCustomUserDetails() {
        User user = new User();
        user.setCorreo("juan@unamba.edu.pe");
        user.setRol(Role.STUDENT);
        when(userRepository.findByCorreo("juan@unamba.edu.pe")).thenReturn(Optional.of(user));

        UserDetails details = userDetailsService.loadUserByUsername("juan@unamba.edu.pe");

        assertInstanceOf(CustomUserDetails.class, details);
        assertEquals("juan@unamba.edu.pe", details.getUsername());
    }

    @Test
    void loadUserByUsernameThrowsWhenNotFound() {
        when(userRepository.findByCorreo("missing@unamba.edu.pe")).thenReturn(Optional.empty());

        assertThrows(UsernameNotFoundException.class,
                () -> userDetailsService.loadUserByUsername("missing@unamba.edu.pe"));
    }
}
