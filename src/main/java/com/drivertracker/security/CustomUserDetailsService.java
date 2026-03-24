package com.drivertracker.security;

import com.drivertracker.model.User;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    // In-memory users for now - in production this would be a database
    private final Map<String, User> users = new HashMap<>();

    public CustomUserDetailsService() {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

        // Pre-load two test users
        users.put("driver1", new User(
                "driver1",
                "driver1",
                encoder.encode("password123"),
                User.Role.DRIVER
        ));

        users.put("passenger1", new User(
                "passenger1",
                "passenger1",
                encoder.encode("password123"),
                User.Role.PASSENGER
        ));
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = users.get(username);

        if (user == null) {
            throw new UsernameNotFoundException("User not found: " + username);
        }

        return new org.springframework.security.core.userdetails.User(
                user.getUserId(),
                user.getPassword(),
                List.of(new SimpleGrantedAuthority("ROLE_" + user.getRole().name()))
        );
    }

    // Expose raw user for our AuthService
    public User findUser(String username) {
        return users.get(username);
    }
}