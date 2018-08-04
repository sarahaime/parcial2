package services;

import modelos.Usuario;
import org.jasypt.util.password.StrongPasswordEncryptor;
import spark.Request;
import spark.Session;

import javax.persistence.EntityManager;
import javax.persistence.Query;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;



public class UsuarioServices extends GestionDb<Usuario> {

    private static UsuarioServices instancia;

    public UsuarioServices() {
        super(Usuario.class);
    }

    public static UsuarioServices getInstancia(){
        if(instancia==null){
            instancia = new UsuarioServices();
        }
        return instancia;
    }

    public List<Usuario> usuarios() {
        EntityManager em = getEntityManager();
        Query query = em.createQuery("select u from Usuario u");
        List<Usuario> lista = query.getResultList();
        em.close();
        return lista;
    }

    public Usuario getUsuario(long id) {
        Usuario usuario =  find(id);
        return usuario;
    }

    public Usuario getUsuarioByUserName(String username) {
        Usuario usuario = null;
        EntityManager em = getEntityManager();
        Query query = em.createQuery("select u from Usuario u where u.username =:username");
        query.setParameter("username", username.trim());
        List<Usuario> lista = query.getResultList();
        if(lista.size() > 0) usuario = lista.get(0);
        em.close();
        return usuario;
    }

    public Usuario getUsuarioByEmail(String correo) {
        Usuario usuario = null;
        EntityManager em = getEntityManager();
        Query query = em.createQuery("select u from Usuario u where u.correo =:correo");
        query.setParameter("correo", correo.trim());
        List<Usuario> lista = query.getResultList();
        if(lista.size() > 0) usuario = lista.get(0);
        em.close();
        return usuario;
    }

    public boolean crearUsuario(Usuario usuario){
        //para guardar la contraseña cifrada
        StrongPasswordEncryptor passwordEncryptor = new StrongPasswordEncryptor();
        String encryptedPassword = passwordEncryptor.encryptPassword(usuario.getPassword());
        usuario.setPassword(encryptedPassword);

        //creo al usuario
        crear( usuario );

        return true;
    }

    public List<Usuario> getEnemigos(long usuarioID){
        EntityManager em = getEntityManager();
        Query query = em.createQuery("select usr from Usuario usr where usr.id <>:usrid and usr.id not in" +
                " ( select a.amigo.id from Usuario u JOIN u.amigos a where u.id =:usuarioID)");
        query.setParameter("usuarioID", usuarioID);
        query.setParameter("usrid", usuarioID);
        List<Usuario> lista = query.getResultList();
        em.close();
        return lista;
    }

    public boolean actualizarUsuario(Usuario usuario){
        editar(usuario);
        return true;
    }

    public Usuario autenticarUsuario(String correo, String pass ){
        Usuario usuario = getUsuarioByEmail(correo );

        if( usuario == null ){ return  null; }

        StrongPasswordEncryptor passwordEncryptor = new StrongPasswordEncryptor();
        if (passwordEncryptor.checkPassword(pass, usuario.getPassword())) {
            return usuario;
        } else {
            return null;
        }
    }

    public static Usuario getLogUser(Request request){
        Usuario usuario = null;
        Session session = request.session(true);
        if(request.cookie("usuario") != null){
            UsuarioServices us = new UsuarioServices();
            usuario = us.getUsuario(Integer.parseInt(request.cookie("usuario")));
            session.attribute("usuario", usuario);
        }
        if(session.attribute("usuario") != null) usuario = session.attribute("usuario");

        return usuario;
    }

}


