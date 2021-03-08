# µhttp - zero dependency http applications

JDK 11+ compatible.

#### Showcase
```java
package hr.yeti.demo;

import hr.yeti.uhttp.Api;
import hr.yeti.uhttp.Operation;
import hr.yeti.uhttp.Application;

    public static class Cars extends Api {
        @Override
        public void describe() {
            GET("cars", exchange -> exchange.reply("Mazda"));
            GET("cars/(?<name>[a-zA-Z]+)", exchange -> {
                String carName = exchange.getPathParam("name");
                exchange.reply(200, carName);
            });
            POST("cars", exchange -> exchange.reply(201));
            DELETE("cars/(?<id>\\d)", deleteCar);
        }
        
        Operation deleteCar = exchange -> exchange.reply(204);
    }

    public static void main(String[] args) {
        // Builder also allows you to set:
        // - http port
        // - hostname
        // - filter chain
        // - authentication
        // - shutdown hook
        // - context root
        // - configuration
        // - executor for custom thread pool configuration
        // - etc.
        Application.newBuilder()
             // You can also use module-info to provide Api implementations
             // You can also use META-INF/services/hr.yeti.uhttp.Api to provide Api implementations
            .api(Cars.class)
            .build()
            .start();
    }
}
```

#### Dependency (not yet published to Maven Central so build locally)
```xml
<dependency>
    <groupId>hr.yeti</groupId>
    <artifactId>uhttp</artifactId>
    <version>1.0</version>
</dependency>
```

#### Build
```shell script
git clone https://github.com/vsmid/uhttp.git
cd uhttp
mvn clean install
```
