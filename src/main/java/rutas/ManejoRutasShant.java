package rutas;

import modelos.Album;
import modelos.Notificacion;
import modelos.Publicacion;
import modelos.Usuario;
import services.*;
import spark.ModelAndView;
import spark.Session;
import spark.template.thymeleaf.ThymeleafTemplateEngine;

import javax.servlet.MultipartConfigElement;
import java.io.File;
import java.text.SimpleDateFormat;
import java.util.*;

import static spark.Spark.get;
import static spark.Spark.post;

public class ManejoRutasShant {

    public void rutas() {
        File uploadDir = new File("fotos");
        uploadDir.mkdir();

        new RutasRest();

        //Usuario admin por defecto
        if (new UsuarioServices().getUsuarioByEmail("admin@admin.com") == null) {
            Usuario usuario = new Usuario();
            usuario.setPassword("admin");
            usuario.setNombre("Shantall");
            usuario.setApellido("Giron");
            usuario.setCorreo("admin@admin.com");
            usuario.setLugar_nacimiento("Santiago");
            usuario.setCiudad_residencia("Santiago");
            usuario.setFotoPerfil("/img/badge4.png");
            usuario.setFotoPortada("img/top-header1.jpg");
            usuario.setAdmin(true);
            usuario.setWebsite("sysservices.site");
            usuario.setTelefono("8095820000");
            usuario.setPais("Republica Dominicana");
            usuario.setProvincia("Santiago");
            usuario.setCiudad("Santiago de los caballeros");
            usuario.setDescripcion_personal("Soy una joven normal... aveces");
            usuario.setGenero("Femenino");
            usuario.setReligion("Católica");
            usuario.setLugar_de_nacimiento("Santiago de los caballeros");
            usuario.setOcupacion("Estudiante");
            usuario.setInclinacion_politica("ninguna");
            usuario.setFacebook("www.facebook.com/admin");
            usuario.setTwitter("www.twitter.com/admin");
            usuario.setSpotify("www.spotify.com/sgiron");

            new UsuarioServices().crearUsuario(usuario);
        }

        get("/login", (request, response) -> {
            Map<String, Object> modelo = new HashMap<>();

            String mensaje = "";

            if (request.queryMap().hasKeys() ) {
                if (request.queryMap("register").booleanValue())
                    mensaje = "Usuario registrado satisfactoriamente.";
            }

            modelo.put("mensaje", mensaje);

            return renderThymeleaf(modelo,"/login");
        });

        post("/login", (request, response) -> {
            boolean iniciarsesion = Boolean.parseBoolean(request.queryParams("iniciarsesion"));

            if (iniciarsesion) {
                String correo = request.queryParams("correo");
                String contrasena = request.queryParams("contrasena");

                Usuario user = new UsuarioServices().autenticarUsuario(correo, contrasena);

                if (user == null) {
                    request.session().invalidate();
                    response.redirect("/login");
                } else {
                    try {
                        if("on".equalsIgnoreCase(request.queryParams("recordar"))){
                            response.cookie("/", "usuario", Long.toString(user.getId()), 7*24*60*60*1000, false);
                            response.cookie("/", "pass", user.getPassword(), 7*24*60*60*1000, false);
                        }else{
                            response.cookie("/", "usuario", Long.toString(user.getId()), 0, false);
                            response.cookie("/", "pass", Long.toString(user.getId()), 0, false);
                        }
                    }catch (Exception e){  }

                    Session session = request.session(true);
                    session.attribute("usuario", user);
                    response.redirect("/inicio");
                }


            } else {
                Usuario usuario = new Usuario();

                String nombre = request.queryParams("nombre");
                String apellido = request.queryParams("apellido");
                String correo = request.queryParams("correo");
                String contrasena = request.queryParams("contrasena");
                String cumpleanos = request.queryParams("cumpleanos");


                usuario.setNombre(nombre);
                usuario.setApellido(apellido);
                usuario.setCorreo(correo);
                usuario.setPassword(contrasena);
                usuario.setFecha_nacimiento(new SimpleDateFormat("mm/dd/yyyy").parse(cumpleanos));

                usuario.setFotoPerfil("/img/badge3.png");
                usuario.setFotoPortada("/img/top-header1.jpg");
                usuario.setAdmin(true);


                new UsuarioServices().crearUsuario(usuario);

                Usuario user =  UsuarioServices.getInstancia().getUsuarioByEmail(correo);

                Session session = request.session(true);
                session.attribute("usuario", user);
                response.redirect("/inicio");
            }

            return null;
        });

        get("/inicio", (request, response) -> {
            Map<String, Object> modelo = new HashMap<>();
            Usuario u = UsuarioServices.getLogUser(request);
            System.out.println(u.getAmigos());
            List<Publicacion> publicaciones = new PublicacionServices().listaPublicacion(u.getId());

            for( Publicacion p:publicaciones ){
                p.setLeGusta(LikePublicacionServices.getInstancia().getLikesByPublicacionYUsuarioID(p.getId(), u.getId()));
            }

            modelo.put("publicaciones", publicaciones );
            modelo.put("usuario", u);
            modelo.put("usuarios", UsuarioServices.getInstancia().getEnemigos(u.getId()));
            return renderThymeleaf(modelo,"/inicio");
        });

        post("/publicacion", (request, response) -> {

            //super importante, para leer los campos ya se se codifican diferente gracias a la imagen
            request.attribute("org.eclipse.jetty.multipartConfig", new MultipartConfigElement("/temp"));

            String descripcion = request.queryParams("descripcion");
            String etiquetas = request.queryParams("etiquetas");
            if(etiquetas.length() > 3) {
                descripcion = descripcion + '\n' + "con: " + etiquetas;
            }
            List<String> tagsList = Arrays.asList(etiquetas.split(",[ ]*"));



            Publicacion publicacion = new Publicacion();
            publicacion.setDescripcion(descripcion);
            publicacion.setUsuario(UsuarioServices.getLogUser(request));
            publicacion.setFecha(new Date());


            String img = RutasImagen.guardarImagen("foto", uploadDir, request);

            publicacion.setImg(img);

            publicacion.setMuro_de(Long.parseLong(request.queryParams("muro")));

            if(!"-1".equalsIgnoreCase(img)) publicacion.setNaturaleza("FOTO");


            PublicacionServices.getInstancia().crear(publicacion);

            Usuario u = UsuarioServices.getLogUser(request);
            for (String correo : tagsList) {
                try {

                    Notificacion n = new Notificacion();
                    n.setDescripcion(u.getNombre() + " " + u.getApellido() + " te ha etiquetado en una publicación.");
                    n.setVinculo("/publicacion?id=" + publicacion.getId());
                    Usuario notificado = UsuarioServices.getInstancia().getUsuarioByEmail(correo);
                    notificado.getNotificaciones().add(n);
                    UsuarioServices.getInstancia().editar(notificado);

                }catch (Exception r){

                }
            }
            response.redirect("/inicio");

            return "";
        });

        post("/ubicacion", (request, response) -> {
            String descripcion = request.queryParams("descripcion");
            Publicacion publicacion = new Publicacion();
            publicacion.setDescripcion(descripcion);
            publicacion.setUsuario(UsuarioServices.getLogUser(request));
            publicacion.setFecha(new Date());
            String img = request.queryParams("ubicacion");
            publicacion.setImg(img);
            publicacion.setMuro_de(Long.parseLong(request.queryParams("muro")));
            publicacion.setNaturaleza("UBICACION");
            PublicacionServices.getInstancia().crear(publicacion);

            response.redirect("/inicio");

            return "";
        });

        post("/album", (request, response) -> {
            //super importante, para leer los campos ya se se codifican diferente gracias a la imagen
            request.attribute("org.eclipse.jetty.multipartConfig", new MultipartConfigElement("/temp"));

            String titulo = request.queryParams("titulo");

            List<Publicacion> publicaciones = new ArrayList<>();

            String etiquetas = request.queryParams("etiquetas");
            List<String> tagsList = Arrays.asList(etiquetas.split(",[ ]*"));

            for(int i = 1; i<=5; i++){
                String img = RutasImagen.guardarImagen("foto" + i, uploadDir, request);

                if( "-1".equalsIgnoreCase(img)) continue;
                Publicacion publicacion = new Publicacion();

                publicacion.setDescripcion("Foto: " + i);
                publicacion.setUsuario(UsuarioServices.getLogUser(request));
                publicacion.setFecha(new Date());
                publicacion.setImg(img);
                publicacion.setMuro_de(Long.parseLong(request.queryParams("muro")));

                if(i == 1){
                    publicacion.setNaturaleza("ALBUM");
                    if(etiquetas.length() > 3) {
                        titulo = titulo + "\n\r" + "\ncon: " + etiquetas;
                    }
                    publicacion.setDescripcion(titulo);
                } else publicacion.setNaturaleza("ALBUM_FOTO");

                PublicacionServices.getInstancia().crear(publicacion);
                publicaciones.add(publicacion);
            }

            Album album = new Album( new HashSet<>(publicaciones) );
            AlbumServices.getInstancia().crear(album);

            publicaciones.get(0).setAlbum_id(album.getId());
            PublicacionServices.getInstancia().editar( publicaciones.get(0) );

            Usuario u = UsuarioServices.getLogUser(request);
            for(String correo: tagsList) {
                try {
                    Notificacion n = new Notificacion();
                    n.setDescripcion(u.getNombre() + " " + u.getApellido() + " te ha etiquetado en una publicación.");
                    n.setVinculo("/publicacion?id=" + publicaciones.get(0).getId());
                    Usuario notificado = UsuarioServices.getInstancia().getUsuarioByEmail(correo);
                    notificado.getNotificaciones().add(n);
                    UsuarioServices.getInstancia().editar(notificado);
                } catch (Exception e) {

                }
            }

            response.redirect("/inicio");

            return "";
        });

        get("/perfil", (request, response) -> {
            Map<String, Object> modelo = new HashMap<>();
            String usuarioid = request.queryParams("usuario");
            String publicacionid = request.queryParams("publicacion");
            modelo.put("usuario", UsuarioServices.getLogUser(request));

            if(usuarioid != null) {
                Usuario amigo = UsuarioServices.getInstancia().getUsuario(Long.parseLong(usuarioid));

                modelo.put("amigo", amigo);
                List<Publicacion> publicaciones =  PublicacionServices.getInstancia().listaPublicacionByUduarioID(amigo.getId());
                Long usuario_id = Long.parseLong(usuarioid);
                for( Publicacion p:publicaciones ){
                    p.setLeGusta(LikePublicacionServices.getInstancia().getLikesByPublicacionYUsuarioID(p.getId(), usuario_id));
                }

                modelo.put("publicaciones",publicaciones);
            }

            if( publicacionid != null ){
                Publicacion publicacion = PublicacionServices.getInstancia().find(Long.parseLong(publicacionid));
                publicacion.setLikeCount(LikePublicacionServices.getInstancia().getLikesByPublicacionID(publicacion.getId()));
                publicacion.setLeGusta(LikePublicacionServices.getInstancia().getLikesByPublicacionYUsuarioID(publicacion.getId(), Long.parseLong(usuarioid)));
                Usuario amigo = UsuarioServices.getInstancia().getUsuario( publicacion.getUsuario().getId() );
                modelo.put("amigo", amigo);


                if( publicacion.getAlbum_id() != null){
                    modelo.put("album", AlbumServices.getInstancia().find(publicacion.getAlbum_id()));
                }else{
                    modelo.put("publicacion", publicacion);
                }
                modelo.put("comentarios", ComentarioServices.getInstancia().getComentarioByPublicacionID(publicacion.getId()));
            }

            return renderThymeleaf(modelo,"/perfil");
        });

        get("/cerrarsesion", (request, response) -> {
            request.session().invalidate();
            response.redirect("/login");
            return null;
        });

      /*
        post("/borrarPublicacion", (request, response) -> {
            PublicacionServices as = new PublicacionServices();
            Session session = request.session(true);
            int publicacionid = Integer.parseInt(request.queryParams("publicacionid"));
            int usuarioid = (int)( (Usuario)session.attribute("usuario")).getId();
            as.borrarPublicacion(publicacionid, usuarioid);
            response.redirect("/inicio");
            return "";
        });

        get("/editarPublicacion", (request, response)->{
            int id = Integer.parseInt(request.queryParams("id"));
            PublicacionServices as = new PublicacionServices();
            Publicacion publicacion = as.getPublicacion((long) id);
            Map<String, Object> modelo = new HashMap<>();
            modelo.put("registeredUser", getLogUser(request));
            modelo.put("publicacion", publicacion);
            String etiqueta = "";

            return renderThymeleaf(modelo, "/editarPublicacion");
        });

        post("/editarPublicacion", (request, response) -> {
            PublicacionServices as = new PublicacionServices();
            String descripcion = request.queryParams("descripcion");
            String usuario = request.queryParams("usuario");
            String tags = request.queryParams("etiquetas");
            String img = request.queryParams("img");
            long id = (long) Integer.parseInt( request.queryParams("id"));

            as.actualizarPublicacion(descripcion, usuario, tags, id);

            response.redirect("/inicio");
            return "";
        });

        private Usuario getLogUser(Request request){

            Usuario usuario = new Usuario();
            Session session = request.session(true);

            if(request.cookie("usuario") != null){
                UsuarioServices us = new UsuarioServices();
                usuario = us.getUsuario(Integer.parseInt(request.cookie("usuario")));
                session.attribute("usuario", usuario);
            }

            if(session.attribute("usuario") != null) usuario = session.attribute("usuario");

            return usuario;
        }*/
    }

    // Declaración para simplificar el uso del motor de template Thymeleaf.
    public static String renderThymeleaf(Map<String, Object> model, String templatePath) {
        return new ThymeleafTemplateEngine().render(new ModelAndView(model, templatePath));
    }

}
