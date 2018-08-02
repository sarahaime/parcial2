package rutas;

import com.google.gson.JsonSyntaxException;
import modelos.ErrorRespuesta;
import modelos.Publicacion;
import org.hibernate.annotations.Persister;
import services.PublicacionServices;
import services.UsuarioServices;
import spark.Request;
import spark.Response;
import spark.serialization.Serializer;
import utilidades.JsonUtilidades;

import java.text.SimpleDateFormat;

import static spark.Spark.*;

public class RutasRest {

    public final static String ACCEPT_TYPE_JSON = "application/json";
    public final static String ACCEPT_TYPE_XML = "application/xml";
    public final static int BAD_REQUEST = 400;
    public final static int ERROR_INTERNO = 500;

    public RutasRest() {
        //Manejo de Excepciones.
        exception(JsonSyntaxException.class, (exception, request, response) -> {
            manejarError(RutasRest.BAD_REQUEST, exception, request, response);
        });

        exception(IllegalArgumentException.class, (exception, request, response) -> {
            manejarError(RutasRest.BAD_REQUEST, exception, request, response);
        });

        exception(Exception.class, (exception, request, response) -> {
            manejarError(RutasRest.ERROR_INTERNO, exception, request, response);
        });

        path("/rest", () -> {
            //filtros especificos:
            afterAfter("/*", (request, response) -> { //indicando que todas las llamadas retorna un json.
                if (request.headers("Accept").equalsIgnoreCase(ACCEPT_TYPE_XML)) {
                    response.header("Content-Type", ACCEPT_TYPE_XML);
                } else {
                    response.header("Content-Type", ACCEPT_TYPE_JSON);
                }

            });

            get("/publicaciones", (req, resp) -> {
                PublicacionServices publicacionServices = new PublicacionServices();
                String correo = req.queryParams("correo");

                if (correo == "") {
                    return "El correo no esta especificado.";
                }

                if (publicacionServices.listaPublicacionByCorreo(correo).isEmpty()) {
                    return "Este correo no tiene publicaciones registradas.";
                }

                return publicacionServices.listaPublicacionByCorreo(correo);
            }, JsonUtilidades.json());

            post("/publicaciones", (req, resp) -> {
                String descripcion = req.queryMap("descripcion").value();
                String fecha = req.queryMap("fecha").value();
                String id = req.queryMap("id").value();

                if (descripcion == "") {
                    return "Debe de especificar una descripcion.";
                }

                if (fecha == "") {
                    return "Debe de especificar una fecha";
                }

                Publicacion publicacion = new Publicacion();
                publicacion.setDescripcion(descripcion);
                publicacion.setFecha(new SimpleDateFormat("mm-dd-yyyy").parse(fecha));
                publicacion.setUsuario(new UsuarioServices().getUsuario(Long.parseLong(id)));

                new PublicacionServices().crear(publicacion);

                return "Publicacion creada exitosamente.";
            }, JsonUtilidades.json());
        });
    }

    private static void manejarError(int codigo, Exception exception, Request request, Response response) {
        response.status(codigo);
        response.body(JsonUtilidades.toJson(new ErrorRespuesta(100, exception.getMessage())));
        exception.printStackTrace();
    }
}
