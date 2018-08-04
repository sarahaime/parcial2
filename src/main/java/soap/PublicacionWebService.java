package soap;

import modelos.Publicacion;
import modelos.Usuario;
import rutas.RutasImagen;
import services.PublicacionServices;
import services.UsuarioServices;

import javax.jws.WebMethod;
import javax.jws.WebService;
import java.io.File;
import java.util.Date;
import java.util.List;

@WebService
public class PublicacionWebService{
     private File uploadDir = new File("fotos");

    @WebMethod
    public String holaMundo(String n){
        System.out.println("estoy saludando**************");
        System.out.println(n);
        return n;
    }

    @WebMethod
    public List<Publicacion> getPublicacionesByEmail(String email){
        PublicacionServices publicacionServices = new PublicacionServices();
        String correo = email;

        System.out.println("El correp:::" + correo);

        if (correo == "") {
            return null;
        }

        if (publicacionServices.listaPublicacionByCorreo(correo).isEmpty()) {
            return null;
        }

        return publicacionServices.listaPublicacionByCorreo(correo);
    }



    @WebMethod
    public String crear(String correo, String descripcion, String img){

        System.out.println("Descripcion soap: " + descripcion);

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
      //  String img = RutasImagen.guardarImagen("foto", uploadDir, req);
        publicacion.setImg(img);
        publicacion.setMuro_de(usuario.getId());
        if(!"-1".equalsIgnoreCase(img)) publicacion.setNaturaleza("FOTO");


        new PublicacionServices().crear(publicacion);

        return "Publicacion creada exitosamente.";
    }




}
