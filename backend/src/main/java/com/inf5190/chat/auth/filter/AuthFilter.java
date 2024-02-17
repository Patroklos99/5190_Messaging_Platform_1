package com.inf5190.chat.auth.filter;

import java.io.IOException;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;

import com.inf5190.chat.auth.session.SessionData;
import com.inf5190.chat.auth.session.SessionDataAccessor;
import com.inf5190.chat.auth.session.SessionManager;

/**
 * Filtre qui intercepte les requêtes HTTP et valide si elle est autorisée.
 */
public class AuthFilter implements Filter {
    private static final String BEARER = "Bearer";

    private final SessionDataAccessor sessionDataAccessor;
    private final SessionManager sessionManager;

    public AuthFilter(SessionDataAccessor sessionDataAccessor, SessionManager sessionManager) {
        this.sessionDataAccessor = sessionDataAccessor;
        this.sessionManager = sessionManager;
    }


    @Override
    public void doFilter(
            ServletRequest request,
            ServletResponse response,
            FilterChain chain) throws IOException, ServletException {
        final HttpServletRequest httpRequest = (HttpServletRequest) request;
        final HttpServletResponse httpResponse = (HttpServletResponse) response;

        if (httpRequest.getMethod().equalsIgnoreCase(HttpMethod.OPTIONS.name())) {
            // permettre aux requêtes "pre-flight" de passer
            chain.doFilter(request, response);
            return;
        }

        final String authHeader = httpRequest.getHeader(HttpHeaders.AUTHORIZATION);
        if (authHeader == null || !authHeader.startsWith(BEARER)) {
            // si la requête ne contient pas le header et le bearer
            this.sendAuthErrorResponse(httpRequest, httpResponse);
            return;
        }

        final String[] authParts = authHeader.split(" ");
        if (authParts.length != 2 && !this.isValidToken(authParts[1])) {
            this.sendAuthErrorResponse(httpRequest, httpResponse);
            return;
        }

        final String token = authParts[1];
        final SessionData data = this.sessionManager.getSession(token);

        if (data == null) {
            this.sendAuthErrorResponse(httpRequest, httpResponse);
            return;
        }

        this.sessionDataAccessor.setToken(httpRequest, token);
        this.sessionDataAccessor.setSessionData(httpRequest, data);

        chain.doFilter(request, response);
    }

    private boolean isValidToken(String token) {
        // Pour le moment on accepte tous les tokens.
        return true;
    }

    protected void sendAuthErrorResponse(HttpServletRequest request, HttpServletResponse response) throws IOException {
        if (request.getRequestURI().contains("auth/logout")) {
            // Si c'est pour le logout, on retourne simplement 200 OK.
            response.setStatus(HttpServletResponse.SC_OK);
        } else {
            response.sendError(HttpServletResponse.SC_FORBIDDEN);
        }
    }
}