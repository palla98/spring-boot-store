package com.codewithmosh.store.auth;

import com.codewithmosh.store.users.Role;
import lombok.AllArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.HttpStatusEntryPoint;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@AllArgsConstructor
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {

        http.
            // 1. primo step per dire che bisogna creare una stateless session
            sessionManagement(c -> c.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            // 2. disabilitare la CSRF (cross site request forgery)
            .csrf(AbstractHttpConfigurer::disable)
            // 3. autorizzazione delle richieste
            .authorizeHttpRequests(c -> c
                .requestMatchers("/carts/**").permitAll() // tutte permesse da /carts in poi
                .requestMatchers("/admin/**").hasRole(Role.ADMIN.name())
                .requestMatchers(HttpMethod.POST, "/users").permitAll() // autorizzo le post di /users
                .requestMatchers(HttpMethod.POST, "/auth/login").permitAll()
                .requestMatchers(HttpMethod.POST, "/auth/refresh").permitAll()
                .requestMatchers(HttpMethod.POST, "/checkout/webhook").permitAll()
                //.requestMatchers(HttpMethod.POST, "/auth/validate").permitAll()  lo togliamo cosi vediamo se il filtro funziona
                .anyRequest().authenticated() // tutto il resto è protetto (403 forbidden)
            )
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class) // questo filtro mi viene chimato prima di tutti perchè è quello che si occupa dell autenticazione e validazione token
                .exceptionHandling(c -> {
                            c.authenticationEntryPoint( //ora di default invece che restituire 403 ritorna 401
                                    new HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED));
                            c.accessDeniedHandler((request, response, accessDeniedException) -> response.setStatus(HttpStatus.FORBIDDEN.value()));
                        }
                );

        return http.build();
    }

    private final UserDetailsService  userDetailsService;

    //ogni volta che avremo bisogno di un password encoder lui entra in azione
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }


    //l AuthController punta all AuthentictionManager e quest ultimo ritorna una configurazione
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    //automaticamente grazie a spring il manager va a vedere se ci sono dei provider, trova questo e lo usa
    @Bean
    public AuthenticationProvider authenticationProvider() {
        //utilizziamo il DaoProvider (default per spring)
        var provider = new DaoAuthenticationProvider();
        provider.setPasswordEncoder(passwordEncoder()); //gli diciamo che vogliamo utilizzare questo encoder
        provider.setUserDetailsService(userDetailsService); // gli diciamo che vogliamo questo UserDetails
        return provider;
    }


}
