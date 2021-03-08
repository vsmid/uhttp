package hr.yeti.uhttp;

import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpContext;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpPrincipal;
import hr.yeti.uhttp.internal.ConfigAware;
import hr.yeti.uhttp.internal.Lookup;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpCookie;
import java.net.InetSocketAddress;
import java.net.URI;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.lang.System.Logger.Level.ERROR;

public class Exchange extends HttpExchange implements ConfigAware {

    private final System.Logger logger = System.getLogger(Exchange.class.getName());

    private HttpExchange httpExchange;
    private final Lookup lookup;
    private Properties config;
    private Map<String, String> queryParams;
    private List<HttpCookie> cookies;

    public Exchange(HttpExchange httpExchange, Lookup lookup) {
        this.httpExchange = httpExchange;
        this.lookup = lookup;
    }

    @Override
    public String getConfigProperty(String name) {
        return config.getProperty(name);
    }

    @Override
    public void setConfig(Properties config) {
        this.config = config;
    }

    public HttpCookie getCookie(String name) {
        return getCookies().stream()
                .filter(cookie -> cookie.getName().equals(name))
                .findFirst()
                .orElse(null);
    }

    public List<HttpCookie> getCookies() {
        if (cookies != null) {
            return cookies;
        }
        cookies = httpExchange.getRequestHeaders()
                .entrySet()
                .stream()
                .filter(e -> e.getKey().matches("(^[sS]et-)?[cC]ookie(2)?$"))
                .map(Map.Entry::getValue)
                .flatMap(List::stream)
                .map(HttpCookie::parse)
                .flatMap(List::stream)
                .collect(Collectors.toList());
        return cookies;
    }

    public void setHeader(String key, String value) {
        httpExchange.getResponseHeaders().add(key, value);
    }

    public String getHeader(String key) {
        return httpExchange.getRequestHeaders().getFirst(key);
    }

    public String getPathParam(String name) {
        Matcher matcher = lookup.getSignature().matcher(httpExchange.getRequestURI().getPath());
        boolean match = matcher.find();
        return match ? matcher.group(name) : null;
    }

    public Map<String, String> getQueryParams() {
        if (queryParams != null) {
            return queryParams;
        }
        Map<String, String> params = parseQuery(httpExchange.getRequestURI().getQuery());
        queryParams = params;
        return queryParams;
    }

    public String getQueryParam(String name) {
        return getQueryParams().get(name);
    }

    private Map<String, String> parseQuery(String query) {
        Map<String, String> params = new HashMap<>();
        if (query != null && query.length() > 0) {
            Stream.of(query.split("&"))
                    .forEach(pair -> {
                        String[] keyValue = pair.split("=");
                        if (keyValue.length == 2) {
                            params.put(keyValue[0], URLDecoder.decode(keyValue[1], StandardCharsets.UTF_8));
                        }
                    });
        }
        return params;
    }

    public void reply() {
        reply(200, null, null);
    }

    public void reply(int status) {
        reply(status, null, null);
    }

    public void reply(int status, byte[] data) {
        reply(status, "text/plain", data);
    }

    public void reply(int status, String contentType, byte[] data) {
        try (OutputStream out = httpExchange.getResponseBody()) {
            if (contentType != null) {
                httpExchange.getResponseHeaders().add("Content-Type", contentType);
            }
            httpExchange.sendResponseHeaders(status, data == null || data.length == 0 ? -1 : data.length);
            if (data != null && data.length > 0) {
                out.write(data);
            }
        } catch (IOException ex) {
            logger.log(ERROR, ex);
        }
    }

    public void reply(String data) {
        reply(200, data);
    }

    public void reply(int status, String data) {
        reply(status, "text/plain", data.getBytes(StandardCharsets.UTF_8));
    }

    public String getBody() {
        try {
            return new String(httpExchange.getRequestBody().readAllBytes());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Headers getRequestHeaders() {
        return httpExchange.getRequestHeaders();
    }

    @Override
    public Headers getResponseHeaders() {
        return httpExchange.getResponseHeaders();
    }

    @Override
    public URI getRequestURI() {
        return httpExchange.getRequestURI();
    }

    @Override
    public String getRequestMethod() {
        return httpExchange.getRequestMethod();
    }

    @Override
    public HttpContext getHttpContext() {
        return httpExchange.getHttpContext();
    }

    @Override
    public void close() {
        httpExchange.close();
    }

    @Override
    public InputStream getRequestBody() {
        return httpExchange.getRequestBody();
    }

    @Override
    public OutputStream getResponseBody() {
        return httpExchange.getResponseBody();
    }

    @Override
    public void sendResponseHeaders(int rCode, long responseLength) throws IOException {
        httpExchange.sendResponseHeaders(rCode, responseLength);
    }

    @Override
    public InetSocketAddress getRemoteAddress() {
        return httpExchange.getRemoteAddress();
    }

    @Override
    public int getResponseCode() {
        return httpExchange.getResponseCode();
    }

    @Override
    public InetSocketAddress getLocalAddress() {
        return httpExchange.getLocalAddress();
    }

    @Override
    public String getProtocol() {
        return httpExchange.getProtocol();
    }

    @Override
    public Object getAttribute(String name) {
        return httpExchange.getAttribute(name);
    }

    @Override
    public void setAttribute(String name, Object value) {
        httpExchange.setAttribute(name, value);
    }

    @Override
    public void setStreams(InputStream i, OutputStream o) {
        httpExchange.setStreams(i, o);
    }

    @Override
    public HttpPrincipal getPrincipal() {
        return httpExchange.getPrincipal();
    }
}
