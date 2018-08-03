import rutas.ManejoRutasGenerales;
import rutas.ManejoRutasShant;
import rutas.RutasImagen;
import services.*;
import soap.PublicacionWebService;
import soap.SoapArranque;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import java.sql.SQLException;

import static spark.Spark.port;
import static spark.Spark.staticFiles;

public class Main {
    public static void main(String[] args)throws Exception {

        //Arranque del servidor
        SoapArranque.stop();
        SoapArranque.init();

        //Iniciando el servicio
        BootStrapServices.startDb();


        //Prueba de Conexión.
        DB.getInstancia().testConexion();

        //indicando los recursos publicos, con esto se puede acceder a ellos sin hacerle metodos get ni post ni nada de eso
        staticFiles.location("/templates");


        EntityManagerFactory emf =  Persistence.createEntityManagerFactory("parcial2");
        EntityManager entityManager = emf.createEntityManager();
        entityManager.getTransaction().begin();
        entityManager.close();


        //usuario administrador por defecto
//       UsuarioServices usuarioServices = new UsuarioServices();
//        if(usuarioServices.listaUsuarios().size() < 1){
//            Usuario administrador = new Usuario();
//            administrador.setNombre("Nombre del Administrador");
//            administrador.setUsername("admin");
//            administrador.setAdministrador(true);
//            administrador.setAutor(true);
//            administrador.setPassword("admin");
//            if( usuarioServices.crearUsuario(administrador)){
//                System.out.println("Usuario administrador creado..");
//            }
//        }

//        ArticuloServices as = new ArticuloServices();
//        as.crearArticulo("El agua post", "cuerpesit00ao", 33, "comida, frio, sueño");
//        EtiquetaServices es = new EtiquetaServices();
//        es.crearEtiqueta("COCINA");
//        es.crearEtiqueta("PROGRAMACION");
//        es.crearEtiqueta("YOLO");
//        es.crearEtiqueta("FOTOGRAFIA");


        //Seteando el puerto en Heroku
        port(getHerokuAssignedPort());

        //Las rutas
        new RutasImagen().rutas();
        new ManejoRutasGenerales().rutas();
        new ManejoRutasShant().rutas();


       new Filtros().aplicarFiltros();

       new ManejoExcepciones().manejoExcepciones();


    }

    static int getHerokuAssignedPort() {
        ProcessBuilder processBuilder = new ProcessBuilder();
        if (processBuilder.environment().get("PORT") != null) {
            return Integer.parseInt(processBuilder.environment().get("PORT"));
        }
        return 4567; //return default port if heroku-port isn't set (i.e. on localhost)
    }

}

