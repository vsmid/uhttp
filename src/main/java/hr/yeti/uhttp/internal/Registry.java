package hr.yeti.uhttp.internal;

import hr.yeti.uhttp.Operation;

import java.net.URI;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Pattern;

public class Registry {
    
    private final Map<String, Operation> operations;
    private final String contextRoot;

    public Registry(String contextRoot) {
        this.contextRoot = contextRoot;
        this.operations = new ConcurrentHashMap<>();
    }

    public void add(String method, String path, Operation operation) {
        operations.put(createKey(method, path), operation);
    }

    private String createKey(String method, String path) {
        String key = method;
        key += "~";
        key += contextRoot;
        if (!key.endsWith("/")) {
            key += "/";
        }
        key += path.substring(path.startsWith("/") ? 1 : 0);
        return key;
    }

    public Lookup lookup(String httpMethod, URI uri) {
        Operation operation = null;
        Pattern signature = null;
        int errorCode = -1;

        Optional<String> path = operations.keySet()
                .stream()
                .filter(key -> uri.getPath().matches(key.split("~")[1]))
                .findFirst();

        if (path.isPresent()) {
            signature = Pattern.compile(path.get().substring(path.get().indexOf("~") + 1));
            operation = operations.get(httpMethod + "~" + signature.toString());
            if (operation == null) {
                errorCode = 405;
            }
        } else {
            errorCode = 404;
        }

        return new Lookup(signature, operation, errorCode);
    }
}
