package soap;

import com.sun.net.httpserver.HttpContext;
import org.eclipse.jetty.http.spi.HttpSpiContextHandler;
import org.eclipse.jetty.http.spi.JettyHttpContext;
import org.eclipse.jetty.http.spi.JettyHttpServer;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.ContextHandlerCollection;

import javax.xml.ws.Endpoint;
import java.lang.reflect.Method;

public class SoapArranque {

    public static void stop() throws Exception {

        //Deteniendo el servidor
        Server server = new Server(7777);
        ContextHandlerCollection contextHandlerCollection = new ContextHandlerCollection();
        server.setHandler(contextHandlerCollection);
        server.stop();

        //El contexto donde estoy agrupando.
        HttpContext context = build(server, "/ws");

        //El o los servicios que estoy agrupando en ese contexto
        PublicacionWebService wsa = new PublicacionWebService();
        Endpoint endpoint = Endpoint.create(wsa);
        endpoint.publish(context);
        // Para acceder al wsdl en http://localhost:7777/ws/PublicacionWebService?wsdl

    }

    public static void init() throws Exception {

        //inicializando el servidor
        Server server = new Server(7777);
        ContextHandlerCollection contextHandlerCollection = new ContextHandlerCollection();
        server.setHandler(contextHandlerCollection);
        server.start();

        //El contexto donde estoy agrupando.
        HttpContext context = build(server, "/ws");

        //El o los servicios que estoy agrupando en ese contexto
        PublicacionWebService wsa = new PublicacionWebService();
        Endpoint endpoint = Endpoint.create(wsa);
        endpoint.publish(context);
        // Para acceder al wsdl en http://localhost:7777/ws/PublicacionWebService?wsdl

    }

    private static HttpContext build(Server server, String contextString) throws Exception {
        JettyHttpServer jettyHttpServer = new JettyHttpServer(server, true);
        JettyHttpContext ctx = (JettyHttpContext) jettyHttpServer.createContext(contextString);
        Method method = JettyHttpContext.class.getDeclaredMethod("getJettyContextHandler");
        method.setAccessible(true);
        HttpSpiContextHandler contextHandler = (HttpSpiContextHandler) method.invoke(ctx);
        contextHandler.start();
        return ctx;
    }
}
