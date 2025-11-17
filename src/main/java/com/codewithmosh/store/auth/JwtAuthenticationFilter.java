package com.codewithmosh.store.auth;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.AllArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

@AllArgsConstructor
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        var authHeader = request.getHeader("Authorization");
        //1. in pratica controllo se nella request non passo nulla riguardo il token e quindi vado al filtro successivo
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }
        //2. controllo se il token è valido altrimenti passo al filtro successivo
        var token = authHeader.replace("Bearer ", "");
        var jwt = jwtService.parseToken(token);
        if (jwt == null || jwt.isExpired()) {
            filterChain.doFilter(request, response);
            return;
        }

        //arrivato a sto punto il token è valido e posso autenticare l utente
        //stavolta a diffenza del login per l oggetto authentication mi prendo l email dal token per non fare query per ottenerla dal db
        var authentication = new UsernamePasswordAuthenticationToken(
                jwt.getUserId(),
                null,
                List.of(new SimpleGrantedAuthority("ROLE_" +  jwt.getRole()))
        );
        //settiamo tutti i dettagli necessari presi dalla request
        authentication.setDetails(
                new WebAuthenticationDetailsSource().buildDetails(request)
        );
        //salva le info dello user appena autenticato in questo contesto di sicurezza:
        SecurityContextHolder.getContext().setAuthentication(authentication);

        //infine, passimao il controllo al prossimo filtro:
        filterChain.doFilter(request, response);
    }
}
