package tesis.server.socialNetwork.dao;

import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

import javax.ejb.EJB;
import javax.ejb.LocalBean;
import javax.ejb.Stateless;
import javax.inject.Inject;

import org.hibernate.Query;
//import org.springframework.stereotype.Controller;

import tesis.server.socialNetwork.entity.NoFavoritoEntity;
import tesis.server.socialNetwork.entity.PostEntity;
import tesis.server.socialNetwork.utils.Utiles;

//@Controller
@LocalBean
public class NoFavoritoDao extends GenericDao<NoFavoritoEntity, Integer> {

	@Inject
	private VoluntarioDao voluntarioDao;
		
	@Override
	protected Class<NoFavoritoEntity> getEntityBeanType() {
		return NoFavoritoEntity.class;
	}

	
	public void guardar(NoFavoritoEntity noFavoritoEntity){
		this.save(noFavoritoEntity);
	}
	
	
	public void eliminar(NoFavoritoEntity noFavoritoEntity){
		this.delete(noFavoritoEntity);
	}
	
	/**
	 * Metodo que retorna una marcacion de noFavorito de acuerdo al post y 
	 * al autor de la marcacion.
	 * 
	 * @param idPostMarcado
	 * @param usernameAutorMarcacion
	 * @return
	 */
	public NoFavoritoEntity buscarMarcacion(Integer idPostMarcado, String usernameAutorMarcacion){
		String consulta = "from NoFavoritoEntity f "
				+ "where f.post.idPost = :idPost "
				+ "and f.autor.userName = :autorUsername";
		Query query = this.getSession().createQuery(consulta);
		query.setInteger("idPost", idPostMarcado);
		query.setString("autorUsername", usernameAutorMarcacion);
		NoFavoritoEntity noFav = (NoFavoritoEntity) query.uniqueResult();
		return noFav;  
	}
	
	
	/**
	 * Metodo que retorna la lista de no favoritos para un post dado.
	 * 
	 * @param postEntity
	 * @return
	 */
	public List<NoFavoritoEntity> listaNoFavoritosByPost(PostEntity postEntity){
		String consulta = "from NoFavoritoEntity f "
				+ "where f.post.idPost = :idPost ";
		Query query = this.getSession().createQuery(consulta);
		query.setInteger("idPost", postEntity.getIdPost());
		List<NoFavoritoEntity> listaRetorno = query.list();
		return listaRetorno;
	}
	
	
	/**
	 * Metodo que retorna la cantidad de noFavs que recibio un post dado
	 * 
	 * @param postEntity
	 * @return
	 */
	public Integer cantidadNoFavoritosByPost(PostEntity postEntity){
		//hacemos el calculo de cuantos noFavs tiene un post dado
		String consulta = "select count(*) from NoFavoritoEntity f "
				+ "where f.post.idPost = :idPost";
		Query query = this.getSession().createQuery(consulta);
		query.setInteger("idPost", postEntity.getIdPost());
		Long cantidadLong = (Long) query.uniqueResult();
		Integer cantidad = cantidadLong.intValue();
		return cantidad;
	}

}
