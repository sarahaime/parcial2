package services;

import modelos.Amigo;
import modelos.Notificacion;
import modelos.Usuario;
import modelos.UsuarioJSON;

import javax.persistence.EntityManager;
import javax.persistence.Query;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class AmigoServices extends GestionDb<Amigo> {

    private static AmigoServices instancia;

    public AmigoServices() {
        super( Amigo.class);
    }

    public static AmigoServices getInstancia(){
        if(instancia==null){
            instancia = new AmigoServices();
        }
        return instancia;
    }

    public boolean solicitarAmigo(Usuario usuario1, Usuario usuario2){
        Amigo amigo = new Amigo();

        amigo.setAmigo(usuario2);
        amigo.setConfirmado(false);
        amigo.setSolicitud(false);

        Amigo amigo2 = new Amigo();

        amigo2.setAmigo(usuario1);
        amigo2.setConfirmado(false);
        amigo2.setSolicitud(true);


        //creo la amistad
        crear( amigo );
        crear( amigo2 );



        usuario1.getAmigos().add(amigo);
        usuario2.getAmigos().add(amigo2);
        UsuarioServices.getInstancia().editar(usuario1);

        //notifico
        Notificacion n = new Notificacion();

        n.setDescripcion(usuario1.getNombre() + " " + usuario1.getApellido() +", quiere agregarte." );
        n.setVinculo("/aceptar?amigo=" + usuario1.getId());
        usuario2.getNotificaciones().add(n);
        UsuarioServices.getInstancia().editar(usuario2);

        return true;
    }


    public boolean aceptarAmigo(Usuario usuario1, Usuario usuario2){

        usuario1.getAmigos().forEach(ami->{
            if(ami.getAmigo().getId().equals( usuario2.getId() )){
                ami.setConfirmado(true);
                UsuarioServices.getInstancia().editar(usuario1);
            }
        });

        usuario2.getAmigos().forEach(ami->{
                 if(ami.getAmigo().getId().equals( usuario1.getId() )){
                ami.setConfirmado(true);
                UsuarioServices.getInstancia().editar(usuario2);
            }
        });

        Notificacion n = new Notificacion();

        n.setDescripcion(usuario1.getNombre() + " " + usuario1.getApellido() +" ha aceptado tu solicitud de amistad.");
        n.setVinculo("/perfil?usuario=" + usuario1.getId());
        usuario2.getNotificaciones().add(n);
        UsuarioServices.getInstancia().editar(usuario2);

        return true;
    }

    public boolean rechazarAmigo(Usuario usuario1, Usuario usuario2){

        Set<Amigo> amigos1 = new HashSet<Amigo>();
        Set<Amigo> amigos2 = new HashSet<Amigo>();

        usuario1.getAmigos().forEach(ami->{
            if( ! ami.getAmigo().getId().equals( usuario2.getId() )){
                amigos1.add(ami);
            }
        });

        UsuarioServices.getInstancia().editar(usuario1);

        usuario2.getAmigos().forEach(ami->{
            if(! ami.getAmigo().getId().equals( usuario1.getId() )){
                amigos2.add(ami);
            }
        });


        Notificacion n = new Notificacion();
        n.setDescripcion(usuario1.getNombre() + " " + usuario1.getApellido() +" ha rechazado tu solicitud de amistad. :(");
        n.setVinculo("/perfil?usuario=" + usuario1.getId());
        usuario2.getNotificaciones().add(n);
        UsuarioServices.getInstancia().editar(usuario2);

        return true;
    }


    public List<Usuario> getAmigosByUsuarioID(long usuarioID){
        EntityManager em = getEntityManager();
        Query query = em.createQuery("select a.amigo from Usuario u JOIN u.amigos a where u.id =:usuarioID and a.confirmado = true");
        query.setParameter("usuarioID", usuarioID);
        List<Usuario> lista = query.getResultList();
        em.close();
        return lista;
    }

 public List<UsuarioJSON> getAmigosJSONByUsuarioID(long usuarioID){
        EntityManager em = getEntityManager();
        Query query = em.createQuery("select a.amigo from Usuario u JOIN u.amigos a where u.id =:usuarioID and a.confirmado = true");
        query.setParameter("usuarioID", usuarioID);
         List<Usuario> tmp = query.getResultList();
        List<UsuarioJSON> lista = new ArrayList<>();
        for(Usuario u : tmp){
            lista.add( new UsuarioJSON( u.getId(), u.getUsername(), u.getNombre() + " " + u.getApellido(), u.getCorreo() )  );
        }

        em.close();
        return lista;
    }




}
