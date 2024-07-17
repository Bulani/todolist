package br.com.jvfm.todolist.filter;

import at.favre.lib.crypto.bcrypt.BCrypt;
import br.com.jvfm.todolist.user.IUserRepository;
import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Base64;

@Component
public class FilterTaskAuth extends OncePerRequestFilter {

    @Autowired
    private IUserRepository userRepository;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        if (!request.getServletPath().startsWith("/tasks/")) {
            filterChain.doFilter(request, response);
            return;
        }

        String[] credentials = new String(Base64.getDecoder().decode(request.getHeader("Authorization")
                .substring("Basic".length()).trim())).split(":");

        var user = this.userRepository.findByUsername(credentials[0]);

        if (user == null) {
            response.sendError(HttpStatus.UNAUTHORIZED.value(), "Usuário não autorizado!");
            return;
        }

        if (!BCrypt.verifyer().verify(credentials[1].toCharArray(), user.getPassword()).verified) {
            response.sendError(HttpStatus.UNAUTHORIZED.value(), "Senha inválida!");
            return;
        }

        request.setAttribute("idUser", user.getId());
        filterChain.doFilter(request, response);

    }
}