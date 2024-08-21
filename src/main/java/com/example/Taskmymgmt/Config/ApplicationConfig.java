package com.example.Taskmymgmt.Config;


import com.example.Taskmymgmt.Repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

// This is a fancy way of storing the UserDetails service implementation

// This application config will hold all the bean and the configurations

@Configuration
@RequiredArgsConstructor
public class ApplicationConfig {

    private final UserRepository userRepository;

    @Bean // here we are implementing the user details service
    public UserDetailsService userDetailsService() {   // check TT in the jwtauthenticator where it is being used
    // This userdetailsservice is directly of the spring framework
        return username -> userRepository.findByUsername(username) // here we have used repository to fetch the data of user from the db
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));  // we are using a lambda expression
    }


    //
    @Bean // This is the data access object which is responsible to fetch the userdetails and also encode password so and so forth
    public AuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider(); // Data Access Object
        authProvider.setUserDetailsService(userDetailsService());
        authProvider.setPasswordEncoder(passwordEncoder());
        return authProvider;
    }

    @Bean // as the name indicates it is responsible to manage the authentication step
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
