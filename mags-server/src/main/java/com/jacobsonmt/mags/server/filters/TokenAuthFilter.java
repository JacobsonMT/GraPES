package com.jacobsonmt.mags.server.filters;

import org.springframework.security.web.authentication.preauth.AbstractPreAuthenticatedProcessingFilter;

import javax.servlet.http.HttpServletRequest;

public class TokenAuthFilter extends AbstractPreAuthenticatedProcessingFilter {

    private String principalRequestHeader;
    private String tokenRequestHeader;

    public TokenAuthFilter(String principalRequestHeader, String tokenRequestHeader) {
        this.principalRequestHeader = principalRequestHeader;
        this.tokenRequestHeader = tokenRequestHeader;
    }

    @Override
    protected Object getPreAuthenticatedPrincipal( HttpServletRequest request) {
        return request.getHeader(principalRequestHeader);
    }

    @Override
    protected Object getPreAuthenticatedCredentials(HttpServletRequest request) {
        return request.getHeader(tokenRequestHeader);
    }
}
