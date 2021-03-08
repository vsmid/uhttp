package hr.yeti.uhttp;

import java.io.IOException;

@FunctionalInterface
public interface Operation {

    void execute(Exchange exchange) throws IOException;
}
