import rutas.ManejoRutasGenerales;
import rutas.ManejoRutasShant;
import rutas.RutasImagen;
import rutas.RutasRest;
import services.BootStrapServices;
import services.DB;
import soap.SoapArranque;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;

import static spark.Spark.port;
import static spark.Spark.staticFiles;

public class Main {
    public static void main(String[] args)throws Exception{
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


        //Seteando el puerto en Heroku
        port(getHerokuAssignedPort());

        //Las rutas
        new RutasImagen().rutas();
        new ManejoRutasGenerales().rutas();
        new ManejoRutasShant().rutas();

        new RutasRest().RutasRest();


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

