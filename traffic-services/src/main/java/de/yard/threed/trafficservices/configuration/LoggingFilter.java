package de.yard.threed.trafficservices.configuration;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;
import org.springframework.util.StreamUtils;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.util.ContentCachingResponseWrapper;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ReadListener;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletInputStream;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletRequestWrapper;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.Part;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.Iterator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * logging filter will log the json body of the http request and the body of the http response by analysing
 * the HttpServletRequest and the HttpServletResponse objects.
 * Note: when reading the response, it gets consumed and has to be copied again to the response wrapper.
 * <p>
 * Logging is implemented according:
 * - exactly one level INFO line per request and response without line breaks
 * - privacy/security critical data cleared (will be logged in DEBUG mode)
 */
@Component
@Configuration
public class LoggingFilter extends OncePerRequestFilter {
    private static final Logger log = LoggerFactory.getLogger(LoggingFilter.class);

    public static final String LINEBREAK_REGEX = "\\R+";
    public static final String LINEBREAK_REPLACEMENT = " ";
    public static final Pattern LINEBREAK_REGEX_PATTERN = Pattern.compile(LINEBREAK_REGEX);

    @SuppressWarnings({"resource", "NullableProblems"})
    @Override
    protected void doFilterInternal(final HttpServletRequest request, final HttpServletResponse response,
                                    final FilterChain filterChain) throws ServletException, IOException {

        final CachedHttpServletRequest requestWrapper = CachedHttpServletRequest.buildHttpServletRequest(request);
        final ContentCachingResponseWrapper responseWrapper = new ContentCachingResponseWrapper(response);
        final String requestUrl = request.getRequestURL().toString();
        final boolean isActuator = requestUrl.contains("actuator");
        String method = requestWrapper.getMethod();
        long startTime = System.currentTimeMillis();

        if (!isActuator) {
            logRequest(method, requestWrapper);
        }

        filterChain.doFilter(requestWrapper, responseWrapper);
        logResponse(method, responseWrapper, startTime);
    }

    private void logRequest(String method, CachedHttpServletRequest requestWrapper) {

        String headers = headersToString(requestWrapper);
        String body = flatLineBreaks(requestWrapper.getPayload());
        String querystring = StringUtils.defaultString(requestWrapper.getQueryString());
        log.info("Request (method={},url={},header=[{}],querystring={}): {}", method, requestWrapper.getRequestURL(), headers, querystring, body);
    }

    public static String headersToString(HttpServletRequest request) {
        Iterator<String> headerIterator = request.getHeaderNames().asIterator();
        StringBuilder headers = new StringBuilder();
        while (headerIterator.hasNext()) {
            String header = headerIterator.next();
            String value = request.getHeader(header);
            if ("authorization".equals(StringUtils.lowerCase(header))) {
                log.debug("Suppressed info log for authorization header value '{}'", value);
                value = "...";
            }
            headers.append(header).append("=").append(value).append(",");
        }
        return headers.toString();
    }

    private String flatLineBreaks(String text) {
        return LINEBREAK_REGEX_PATTERN.matcher(text).replaceAll(LINEBREAK_REPLACEMENT);
    }

    private void logResponse(String method, ContentCachingResponseWrapper responseWrapper, long startTime) throws IOException {
        String body = "...";
        if (!"GET".equals(method)) {
            body = flatLineBreaks(IOUtils.toString(responseWrapper.getContentInputStream(), StandardCharsets.UTF_8));
        }
        log.info("Response (status={}, duration={} ms): {}", responseWrapper.getStatus(), (System.currentTimeMillis() - startTime), body);
        responseWrapper.copyBodyToResponse();
    }

}

/**
 * ContentCachingRequestWrapper cannot be used for caching the request body because InputStream() can only be called once.
 * This solution is from https://www.baeldung.com/spring-reading-httpservletrequest-multiple-times
 * <p>
 * As of SpringBoot 2.2, for some reason multipart parts needs to be wrapped separately. Otherwise these are silently consumed and cause code 400 ("missing 'type' parameter").
 */

abstract class CachedHttpServletRequest extends HttpServletRequestWrapper {
    CachedHttpServletRequest(HttpServletRequest request) {
        super(request);
    }

    abstract String getPayload();

    static CachedHttpServletRequest buildHttpServletRequest(HttpServletRequest request) throws IOException {
        String contentType = request.getContentType();
        if (contentType != null && contentType.toLowerCase().contains("multipart/form-data")) {
            return new CachedMultipartHttpServletRequest(request);
        } else {
            return new CachedBodyHttpServletRequest(request);
        }
    }
}

class CachedBodyHttpServletRequest extends CachedHttpServletRequest {
    private static final Logger LOGGER = LoggerFactory.getLogger(CachedBodyHttpServletRequest.class);

    private byte[] cachedBody;

    public CachedBodyHttpServletRequest(HttpServletRequest request) throws IOException {
        super(request);
        InputStream requestInputStream = request.getInputStream();
        this.cachedBody = StreamUtils.copyToByteArray(requestInputStream);
    }

    @Override
    public ServletInputStream getInputStream() {
        return new CachedBodyServletInputStream(this.cachedBody);
    }

    @Override
    public BufferedReader getReader() {
        ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(this.cachedBody);
        return new BufferedReader(new InputStreamReader(byteArrayInputStream));
    }

    public String getPayload() {
        return new String(cachedBody);
    }

    @Override
    public Collection<Part> getParts() throws IOException, ServletException {
        Collection<Part> parts = super.getParts();
        if (parts.size() == 0) {
            LOGGER.warn("no parts found");
        }
        return parts;
    }
}

class CachedMultipartHttpServletRequest extends CachedHttpServletRequest {
    private static final Logger LOGGER = LoggerFactory.getLogger(CachedMultipartHttpServletRequest.class);

    public CachedMultipartHttpServletRequest(HttpServletRequest request) {
        super(request);
    }

    /**
     * Might contain file content. So better logging only for field "type".
     *
     * @return log phrase
     */
    public String getPayload() {
        StringBuilder s = new StringBuilder();
        try {
            Collection<Part> parts = super.getParts();
            for (Part part : parts) {
                String contentDisposition = StringUtils.defaultString(part.getHeader("Content-Disposition"));
                LOGGER.debug("contentDisposition=" + contentDisposition);
                if (contentDisposition.contains("name=\"type\"")) {
                    String name = IOUtils.toString(part.getInputStream(), StandardCharsets.UTF_8.name());
                    LOGGER.debug("name=" + name);
                    s.append(String.format("type=%s", name));
                }
            }
        } catch (Exception e) {
            LOGGER.warn("getParts() failed with " + e.getMessage());
        }
        s.append("<multipart content unlogged>");
        return s.toString();
    }
}

class CachedBodyServletInputStream extends ServletInputStream {
    private static final Logger LOGGER = LoggerFactory.getLogger(CachedBodyServletInputStream.class);

    private InputStream cachedBodyInputStream;

    public CachedBodyServletInputStream(byte[] cachedBody) {
        this.cachedBodyInputStream = new ByteArrayInputStream(cachedBody);
    }

    @Override
    public boolean isReady() {
        return true;
    }

    @Override
    public void setReadListener(ReadListener listener) {
        throw new IllegalStateException("not implemented");
    }

    @Override
    public int read() throws IOException {
        return cachedBodyInputStream.read();
    }

    @Override
    public boolean isFinished() {
        try {
            return cachedBodyInputStream.available() == 0;
        } catch (IOException e) {
            LOGGER.error("isFinished got IOException", e);
            return true;
        }
    }
}


