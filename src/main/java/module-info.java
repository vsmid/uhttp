module hr.yeti.uhttp {
    exports hr.yeti.uhttp;

    uses hr.yeti.uhttp.Api;

    requires java.logging;
    requires transitive jdk.httpserver;
    requires java.net.http;
}