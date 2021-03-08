package hr.yeti.uhttp;

import hr.yeti.uhttp.internal.ConfigAware;
import hr.yeti.uhttp.internal.Registry;
import hr.yeti.uhttp.internal.RegistryAware;

import java.util.Properties;

public abstract class Api implements ConfigAware, RegistryAware {

    private Registry registry;
    private Properties config;

    public abstract void describe();

    protected void GET(String path, Operation operation) {
        registry.add("GET", path, operation);
    }

    protected void POST(String path, Operation operation) {
        registry.add("POST", path, operation);
    }

    protected void PUT(String path, Operation operation) {
        registry.add("PUT", path, operation);
    }

    protected void DELETE(String path, Operation operation) {
        registry.add("DELETE", path, operation);
    }

    protected void HEAD(String path, Operation operation) {
        registry.add("HEAD", path, operation);
    }

    protected void TRACE(String path, Operation operation) {
        registry.add("TRACE", path, operation);
    }

    protected void PATCH(String path, Operation operation) {
        registry.add("PATCH", path, operation);
    }

    protected void OPTIONS(String path, Operation operation) {
        registry.add("OPTIONS", path, operation);
    }

    @Override
    public String getConfigProperty(String name) {
        return config.getProperty(name);
    }

    @Override
    public void setConfig(Properties config) {
        if (this.config == null) {
            this.config = config;
        }
    }

    @Override
    public void setRegistry(Registry registry) {
        if (this.registry == null) {
            this.registry = registry;
        }
    }
}
