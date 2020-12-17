package com.jacobsonmt.mags.server;

import com.jacobsonmt.mags.server.filters.TokenAuthFilter;
import com.jacobsonmt.mags.server.settings.ClientSettings;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;

@Configuration
@EnableWebSecurity
@Order(1)
@Log4j2
public class TokenAPISecurityConfig extends WebSecurityConfigurerAdapter {

    @Value("${application.http.auth-token-header-name}")
    private String authTokenHeaderName;

    @Value("${application.http.client-header-name}")
    private String clientHeaderName;

    @Autowired
    private ClientSettings clientSettings;

    @Override
    protected void configure( HttpSecurity httpSecurity) throws Exception {

        log.info( "info------------------------------------------------" );
        log.info( "auth token header name: " + authTokenHeaderName );
        log.info( "client header name: " + clientHeaderName );
        log.info( "tokens:" );
        for ( ClientSettings.ApplicationClient ac : clientSettings.getClients().values() ) {
            log.info( "token: " + ac );
        }
        log.info( "info------------------------------------------------" );

        TokenAuthFilter filter = new TokenAuthFilter(clientHeaderName, authTokenHeaderName);
        filter.setAuthenticationManager( authentication -> {
            String client = (String) authentication.getPrincipal();
            String token = (String) authentication.getCredentials();
            ClientSettings.ApplicationClient auth_token = clientSettings.getClients().get(client);
//            if (auth_token == null || !auth_token.equals( token )) {
            if (auth_token == null || !auth_token.getToken().equals( token )) {
                throw new BadCredentialsException("Invalid authentication.");
            }
            authentication.setAuthenticated(true);
            return authentication;
        } );
        httpSecurity.
                antMatcher("/api/**").
                csrf().disable().
                sessionManagement().sessionCreationPolicy( SessionCreationPolicy.STATELESS).
                and().addFilter(filter).authorizeRequests().anyRequest().authenticated();
    }

}
