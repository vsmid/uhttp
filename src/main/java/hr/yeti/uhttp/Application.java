package hr.yeti.uhttp;

import com.sun.net.httpserver.Authenticator;
import com.sun.net.httpserver.Filter;
import com.sun.net.httpserver.HttpContext;
import com.sun.net.httpserver.HttpServer;
import hr.yeti.uhttp.internal.Processor;
import hr.yeti.uhttp.internal.Registry;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.net.InetSocketAddress;
import java.net.URI;
import java.util.List;
import java.util.Properties;
import java.util.ServiceLoader;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import static java.lang.System.Logger.Level.ERROR;
import static java.lang.System.Logger.Level.INFO;

public class Application {

    private final System.Logger logger = System.getLogger(Application.class.getName());

    private Properties config;
    private Registry registry;
    private Builder settings;
    private HttpServer httpServer;

    private Application(Builder builder) throws IOException {
        settings = builder;

        httpServer = HttpServer.create();
        registry = new Registry(builder.contextRoot);
        config = new Properties();

        Processor processor = new Processor();
        processor.setConfig(config);
        processor.setRegistry(registry);

        HttpContext context = httpServer.createContext(builder.contextRoot, processor);

        if (builder.properties != null) {
            config.putAll(builder.properties);
        }

        if (builder.middleware != null) {
            context.getFilters()
                    .addAll(
                            List.of(builder.middleware)
                    );
        }

        if (builder.authenticator != null) {
            context.setAuthenticator(builder.authenticator);
        }

        ServiceLoader.load(Api.class)
                .stream()
                .forEach(
                        apiProvider -> {
                            Api api = apiProvider.get();
                            api.setRegistry(registry);
                            api.setConfig(config);
                            api.describe();
                        }
                );

        if (builder.api != null) {
            for (Class<?> apiClass : builder.api) {
                try {
                    Api api = (Api) apiClass.getDeclaredConstructor().newInstance();
                    api.setRegistry(registry);
                    api.setConfig(config);
                    api.describe();
                } catch (InstantiationException
                        | IllegalAccessException
                        | InvocationTargetException
                        | NoSuchMethodException
                        | SecurityException ex) {
                    logger.log(ERROR, ex);
                }
            }
        }

        if (builder.hook != null) {
            Runtime.getRuntime().addShutdownHook(builder.hook);
        }

        httpServer.setExecutor(builder.executor);
    }

    public static Builder newBuilder() {
        return new Builder();
    }

    public static class Builder {

        private String host = "127.0.0.1";
        private int port = 3005;
        private int backlog = 10;
        private String contextRoot = "/";
        private Class<?>[] api;
        private Filter[] middleware;
        private Authenticator authenticator;
        private Runnable[] runner;
        private Executor executor = Executors.newCachedThreadPool();
        private Thread hook;
        private Properties properties;

        public Builder contextRoot(String contextRoot) {
            int start = contextRoot.startsWith("/") ? 1 : 0;
            int end = contextRoot.length() - (contextRoot.endsWith("/") ? 1 : 0);
            this.contextRoot += contextRoot.substring(start, end);
            return this;
        }

        public Builder executor(Executor executor) {
            this.executor = executor;
            return this;
        }

        public Builder backlog(int backlog) {
            this.backlog = backlog;
            return this;
        }

        public Builder host(String host) {
            this.host = host;
            return this;
        }

        public Builder port(int port) {
            this.port = port;
            return this;
        }

        public Builder api(Class<? extends Api>... api) {
            this.api = api;
            return this;
        }

        public Builder middleware(Filter... middleware) {
            this.middleware = middleware;
            return this;
        }

        public Builder authenticator(Authenticator authenticator) {
            this.authenticator = authenticator;
            return this;
        }

        public Builder beforeStart(Runnable... runner) {
            this.runner = runner;
            return this;
        }

        public Builder shutdownHook(Thread hook) {
            this.hook = hook;
            return this;
        }

        public Builder configuration(Properties properties) {
            this.properties = properties;
            return this;
        }

        public Application build() {
            try {
                return new Application(this);
            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }
    }

    public void start() {
        try {
            if (settings.runner != null) {
                for (Runnable runner : settings.runner) {
                    runner.run();
                }
            }

            InetSocketAddress inetSocketAddress = new InetSocketAddress(settings.host, settings.port);
            httpServer.bind(inetSocketAddress, settings.backlog);
            this.httpServer.start();

            logger.log(INFO, "Service started on port {0}.", String.valueOf(settings.port));
        } catch (IOException ex) {
            logger.log(ERROR, ex);
        }
    }

    public void stop(int delay) {
        this.httpServer.stop(delay);
        logger.log(INFO, "Service stopped.");
    }

    public URI getUri() {
        return URI.create("http://" + settings.host + ":" + settings.port + settings.contextRoot);
    }
}
