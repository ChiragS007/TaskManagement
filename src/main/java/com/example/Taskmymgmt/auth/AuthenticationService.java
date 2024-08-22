package com.example.Taskmymgmt.auth;


import com.example.Taskmymgmt.Config.JwtService;
import com.example.Taskmymgmt.Entity.UserEntity;
import com.example.Taskmymgmt.Repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

// this is the class where we will be implementing the process for register and authenticate


@Service
@RequiredArgsConstructor
public class AuthenticationService {

    private final UserRepository userRepository;

    private final PasswordEncoder passwordEncoder;

    private final JwtService jwtService;

    private final AuthenticationManager authenticationManager;

    private final UserDetailsService userDetailsService;

    public AuthenticationResponse register(RegisterRequest request) {

        if (userRepository.existsByUsername(request.getUsername())) {
            throw new RuntimeException("User already exists");
        }

        var user = UserEntity.builder()
                .username(request.getUsername())
                .password(passwordEncoder.encode(request.getPassword()))
                .roles(UserEntity.ERole.DEFAULT)
                .build();

        userRepository.save(user);

        var jwtToken = jwtService.generateToken(user);
        return AuthenticationResponse.builder()
                .token(jwtToken)
                .build();
    }

    public AuthenticationResponse authenticate(AuthenticationRequest request) {  // this method is form for the auth manager to authenticate the request based on the username and password

        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getUsername(),
                        request.getPassword()
                )
        );
        var user = userRepository.findByUsername(request.getUsername()).orElseThrow(() -> new UsernameNotFoundException("User not found"));;
        var jwtToken = jwtService.generateToken(user);
        return AuthenticationResponse.builder()
                .token(jwtToken)
                .build();

    }

    public void createAdmin() {
        String adminUsername = "test_admin";
        String adminPassword = "test_admin";

        if (userRepository.existsByUsername(adminUsername)) {
            throw new RuntimeException("Admin user already exists");
        }

        var admin = UserEntity.builder()
                .username(adminUsername)
                .password(passwordEncoder.encode(adminPassword))
                .roles(UserEntity.ERole.ADMIN)  // Assign the ADMIN role
                .build();

        userRepository.save(admin);
    }
}
