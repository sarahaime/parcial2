package services;

import modelos.Publicacion;

import javax.persistence.EntityManager;
import javax.persistence.Query;
import java.sql.*;
import java.util.*;

import static java.lang.Math.max;



public class PublicacionServices extends GestionDb<Publicacion>{


    private static PublicacionServices instancia;

    public static PublicacionServices getInstancia(){
        if(instancia==null){
            instancia = new PublicacionServices();
        }
        return instancia;
    }

    public PublicacionServices(){
        super(Publicacion.class);
    }



    public List<Publicacion> listaPublicacion(long usuarioID ) {
        EntityManager em = getEntityManager();
        Query query = em.createQuery("select p from Publicacion p where  p.usuario.id =:miid or p.usuario.id in" +
                " ( select a.amigo.id from Usuario u JOIN u.amigos a where u.id =:usuarioID and a.confirmado=:valor) " +
                "order by p.id desc");
        query.setParameter("usuarioID", usuarioID);
        query.setParameter("miid", usuarioID);
        query.setParameter("valor", true);
        List<Publicacion> lista = query.getResultList();
        em.close();
        return lista;
    }

    public List<Publicacion> listaPublicacionByUduarioID(Long usuarioid) {
        EntityManager em = getEntityManager();
        Query query = em.createQuery("select a from Publicacion a WHERE muro_de =:usuarioid order by a.id desc");
        query.setParameter("usuarioid", usuarioid);
        List<Publicacion> lista = query.getResultList();
        em.close();
        return lista;
    }

    public List<Publicacion> listaPublicacionByCorreo(String correo) {
        EntityManager em = getEntityManager();
        Query query = em.createQuery("select p from Publicacion p where p.usuario.correo = :correo");
        query.setParameter("correo", correo);
        List<Publicacion> lista = query.getResultList();

        for (Publicacion p : lista){
            p.setUsuario(null);
        }

        em.close();
        return lista;
    }

    public List<Publicacion> listaPublicacion(int pagina, int sz) {
        pagina = max(pagina,1);
        EntityManager em = getEntityManager();
        Query queryList = em.createQuery("select a from Publicacion a order by a.id desc");
        queryList.setFirstResult((pagina-1)*sz);
        queryList.setMaxResults(sz);
        List<Publicacion> lista = queryList.getResultList();
        em.close();
        return lista;
    }



    public long getCantidadPublicaciones(){
        EntityManager em = getEntityManager();
        Query queryCount= em.createQuery("select count(a.id) from Publicacion a");
        long cant = (long) queryCount.getSingleResult();
        em.close();
        return cant;
    }


    public boolean crearPublicacion(String descripcion, long usuarioid, String tags){
        Publicacion publicacion = new Publicacion();
        publicacion.setUsuario( new UsuarioServices().getUsuario(usuarioid) );
        publicacion.setDescripcion(descripcion);
       // publicacion.setEtiquetas(generarEtiquetas(tags));
        crear(publicacion);
        return true;
    }

    //Actualizar las publicaciones
    public boolean actualizarPublicacion(long id, String descripcion, long usuarioid, String tags){
        Publicacion publicacion = find(id);
        publicacion.setDescripcion(descripcion);
     //   publicacion.setEtiquetas(generarEtiquetas(tags));
        editar(publicacion);
        return true;
    }

    //Metodo borrar las publicaciones
    public boolean borrarPublicacion(long id, long usuarioid){
        eliminar(id);
        return true;
    }

}















