package hr.yeti.uhttp;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Properties;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class HttpTest {

    static Application application;
    static URI uri;

    @BeforeAll
    public static void beforeAll() {
        application = Application.newBuilder()
                .port(2000)
                .configuration(new Properties() {{
                    put("message", "Hello!");
                }})
                .api(Cars.class).build();
        uri = application.getUri();
        application.start();
    }

    @AfterAll
    public static void afterAll() {
        application.stop(0);
    }

    @Test
    public void should_process_simple_http_request() throws IOException, InterruptedException {
        HttpRequest httpRequest = HttpRequest.newBuilder().uri(uri.resolve("cars")).GET().build();
        HttpResponse<String> response = HttpClient.newHttpClient().send(httpRequest, HttpResponse.BodyHandlers.ofString());

        assertEquals("Suzuki, Mazda", response.body());
    }

    @Test
    public void should_read_path_param() throws IOException, InterruptedException {
        HttpRequest httpRequest = HttpRequest.newBuilder().uri(uri.resolve("cars/3005")).GET().build();
        HttpResponse<String> response = HttpClient.newHttpClient().send(httpRequest, HttpResponse.BodyHandlers.ofString());

        assertEquals("3005", response.body());
    }

    @Test
    public void should_use_provided_properties() throws IOException, InterruptedException {
        HttpRequest httpRequest = HttpRequest.newBuilder().uri(uri.resolve("cars/prop")).GET().build();
        HttpResponse<String> response = HttpClient.newHttpClient().send(httpRequest, HttpResponse.BodyHandlers.ofString());

        assertEquals("Hello!", response.body());
    }

    public static class Cars extends Api {

        @Override
        public void describe() {
            GET("/cars", exchange -> exchange.reply(200, "Suzuki, Mazda"));
            GET("/cars/(?<id>\\d+)", exchange -> exchange.reply(200, exchange.getPathParam("id")));
            GET("/cars/prop", exchange -> exchange.reply(200, getConfigProperty("message")));
        }
    }
}
