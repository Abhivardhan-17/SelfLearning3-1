
package com.socialdashboard.security;

import com.socialdashboard.model.AppUser;
import com.socialdashboard.repository.UserRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

@Component
public class JwtAuthFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final UserRepository users;

    public JwtAuthFilter(JwtService jwtService, UserRepository users) {
        this.jwtService = jwtService;
        this.users = users;
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain chain
    ) throws ServletException, IOException {

        String header = request.getHeader("Authorization");

        if (header != null && header.startsWith("Bearer ")) {
            try {
                String email = jwtService.extractEmail(header.substring(7));

                if (SecurityContextHolder.getContext().getAuthentication() == null) {

                    AppUser appUser = users.findByEmail(email).orElse(null);

                    if (appUser != null && (appUser.isEmailVerified() || "ADMIN".equalsIgnoreCase(appUser.getRole()))) {

                        String role = appUser.getRole();

                        if (role == null || role.isBlank()) {
                            role = "USER";
                        }

                        var authorities = List.of(
                                new SimpleGrantedAuthority(
                                        "ROLE_" + role.toUpperCase()
                                )
                        );

                        var principal = User.withUsername(email)
                                .password("")
                                .authorities(authorities)
                                .build();

                        var authentication =
                                new UsernamePasswordAuthenticationToken(
                                        principal,
                                        null,
                                        principal.getAuthorities()
                                );

                        authentication.setDetails(
                                new WebAuthenticationDetailsSource()
                                        .buildDetails(request)
                        );

                        SecurityContextHolder.getContext()
                                .setAuthentication(authentication);
                    }
                }

            } catch (Exception ex) {
                org.slf4j.LoggerFactory.getLogger(JwtAuthFilter.class).error("JwtAuthFilter error: ", ex);
                SecurityContextHolder.clearContext();
            }
        }

        chain.doFilter(request, response);
    }
}
