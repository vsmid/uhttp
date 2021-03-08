package hr.yeti.uhttp.internal;

import java.util.Properties;

public interface ConfigAware {
    String getConfigProperty(String name);
    void setConfig(Properties config);
}
