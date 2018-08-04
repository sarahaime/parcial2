package rutas;

import com.google.gson.JsonSyntaxException;
import modelos.ErrorRespuesta;
import modelos.Publicacion;
import modelos.Usuario;
import org.hibernate.annotations.Persister;
import services.PublicacionServices;
import services.UsuarioServices;
import spark.Filter;
import spark.Request;
import spark.Response;
import spark.Spark;
import spark.serialization.Serializer;
import utilidades.JsonUtilidades;

import javax.servlet.MultipartConfigElement;
import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;

import static spark.Spark.*;

public class RutasRest {

    public final static String ACCEPT_TYPE_JSON = "application/json";
    public final static String ACCEPT_TYPE_XML = "application/xml";
    public final static int BAD_REQUEST = 400;
    public final static int ERROR_INTERNO = 500;

    public void RutasRest() {
        File uploadDir = new File("fotos");
        uploadDir.mkdir();

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
            /*fterAfter("/*", (request, response) -> { //indicando que todas las llamadas retorna un json.
                if (request.headers("Accept").equalsIgnoreCase(ACCEPT_TYPE_XML)) {
                    response.header("Content-Type", ACCEPT_TYPE_XML);
                } else {
                    response.header("Content-Type", ACCEPT_TYPE_JSON);
                }

            });*/

            HashMap<String, String> corsHeaders = new HashMap<>();
            corsHeaders.put("Access-Control-Allow-Methods", "GET,PUT,POST,DELETE,OPTIONS");
            corsHeaders.put("Access-Control-Allow-Origin", "*");
            corsHeaders.put("Access-Control-Allow-Headers", "*");
            corsHeaders.put("Access-Control-Allow-Credentials", "true");

            afterAfter("", (request, response) -> corsHeaders.forEach(response::header));

            get("/publicaciones", (req, resp) -> {
                resp.header("Access-Control-Allow-Origin", "*");

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

            post("/publicaciones/crear", (req, resp) -> {
                resp.header("Access-Control-Allow-Origin", "*");

                //super importante, para leer los campos ya se se codifican diferente gracias a la imagen
                req.attribute("org.eclipse.jetty.multipartConfig", new MultipartConfigElement("/temp"));

                String descripcion = req.queryParams("descripcion");
                String correo = req.queryParams("correo");

                System.out.println("La real descripcion: " + descripcion);

                if (descripcion == "") {
                    return "Debe de especificar una descripcion.";
                }

                if (correo == "") {
                    return "Debe de especificar un correo";
                }

                Usuario usuario = new UsuarioServices().getUsuarioByEmail(correo);

                if (usuario == null) {
                    return "El correo no esta registrado en nuestra base de datos";
                }

                Publicacion publicacion = new Publicacion();
                publicacion.setDescripcion(descripcion);
                publicacion.setFecha(new Date());
                publicacion.setUsuario(usuario);

                /**Para las imagenes**/
                String img = RutasImagen.guardarImagen("foto", uploadDir, req);
                publicacion.setImg(img);
                publicacion.setMuro_de(usuario.getId());
                if(!"-1".equalsIgnoreCase(img)) publicacion.setNaturaleza("FOTO");


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
