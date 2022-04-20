package de.yard.threed.services.configuration;

import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 *
 */
@Configuration
@Order(Ordered.HIGHEST_PRECEDENCE)
public class CorsConfig implements Filter {

    /**
     * destroy needs to be overridden with an empty method
     */
    @Override
    public void destroy() {
    }

    /**
     * corsFilter defines the returned header attributes Access-Control-Allow-Origin, Access-Control-Allow-Methods,
     * Access-Control-Max-Age and Access-Control-Allow-Headers.
     *
     * @param req   http servlet request
     * @param res   http servlet response
     * @param chain existing filter chain
     * @throws IOException      any IO exception
     * @throws ServletException any servlet exception
     */
    @Override
    public void doFilter(final ServletRequest req, final ServletResponse res, final FilterChain chain)
            throws IOException, ServletException {
        final HttpServletResponse response = (HttpServletResponse) res;
        final HttpServletRequest request = (HttpServletRequest) req;
        response.setHeader("Access-Control-Allow-Origin", "*");
        response.setHeader("Access-Control-Allow-Methods", "POST, GET, DELETE, PUT, PATCH");
        response.setHeader("Access-Control-Max-Age", "3600");
        response.setHeader("Access-Control-Allow-Headers", "Access-Control-Allow-Headers, Authorization, Origin, Accept, X-Requested-With, Content-Type, Access-Control-Request-Method, Access-Control-Request-Headers, Content-Disposition, X-Forwarded-Prefix, X-Auth-Identity");
        response.setHeader("Access-Control-Allow-Credentials", "true");

        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            response.setStatus(HttpServletResponse.SC_OK);
        } else {
            chain.doFilter(req, res);
        }
    }

    /**
     * init needs to be overridden with an empty method
     */
    @Override
    public void init(final FilterConfig filterConfig) {
    }
}