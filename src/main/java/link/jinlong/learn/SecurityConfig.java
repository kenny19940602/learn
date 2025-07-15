package link.jinlong.learn;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {

        @Bean
        SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
                http
                                .authorizeHttpRequests(request -> request
                                                .requestMatchers("/cashcards/**")
                                                .hasRole("CARD-OWNER")
                                                .requestMatchers("/hello/**", "/actuator/**")
                                                .permitAll())
                                .httpBasic(Customizer.withDefaults())
                                .csrf(csrf -> csrf.disable());
                return http.build();
        }

        @Bean
        PasswordEncoder passwordEncoder() {
                return new BCryptPasswordEncoder();
        }

        @Bean
        UserDetailsService userDetailsService() {
                UserDetails sarah = User.builder()
                                .username("sarah1")
                                .password(passwordEncoder().encode("abc123"))
                                .roles("CARD-OWNER")
                                .build();
                UserDetails hankOwnsNoCards = User.builder()
                                .username("hank-owns-no-cards")
                                .password(passwordEncoder().encode("qrs456"))
                                .roles("NON-OWNER")
                                .build();
                UserDetails kumar = User.builder()
                                .username("kumar")
                                .password(passwordEncoder().encode("xyz789"))
                                .roles("CARD-OWNER")
                                .build();
                return new InMemoryUserDetailsManager(sarah, hankOwnsNoCards, kumar);
        }

}
