package hr.yeti.uhttp.internal;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import hr.yeti.uhttp.Exchange;

import java.io.IOException;
import java.util.Properties;

import static java.lang.System.Logger.Level.ERROR;

public class Processor implements HttpHandler, ConfigAware, RegistryAware {

    private final System.Logger logger = System.getLogger(Processor.class.getName());

    private Registry registry;
    private Properties config;

    @Override
    public void handle(HttpExchange httpExchange) {
        Lookup lookup = registry.lookup(httpExchange.getRequestMethod(), httpExchange.getRequestURI());
        try (Exchange exchange = new Exchange(httpExchange, lookup)) {
            exchange.setConfig(config);
            if (lookup.getErrorCode() != -1) {
                exchange.reply(lookup.getErrorCode());
            } else {
                try {
                    lookup.getOperation().execute(exchange);
                } catch (IOException ex) {
                    logger.log(ERROR, ex);
                    exchange.reply(500);
                }
            }
        }
    }

    @Override
    public String getConfigProperty(String name) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setConfig(Properties config) {
        this.config = config;
    }

    @Override
    public void setRegistry(Registry registry) {
        this.registry = registry;
    }
}
