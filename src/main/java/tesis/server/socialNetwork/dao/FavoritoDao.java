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

import tesis.server.socialNetwork.entity.FavoritoEntity;
import tesis.server.socialNetwork.entity.PostEntity;
import tesis.server.socialNetwork.utils.Utiles;

//@Controller
@LocalBean
public class FavoritoDao extends GenericDao<FavoritoEntity, Integer> {

	@Inject
	private VoluntarioDao voluntarioDao;
	
	
	@Override
	protected Class<FavoritoEntity> getEntityBeanType() {
		return FavoritoEntity.class;
	}
	
	
	public void guardar(FavoritoEntity favoritoEntity){
		this.save(favoritoEntity);
	}
	
	
	public void eliminar(FavoritoEntity favoritoEntity){
		this.delete(favoritoEntity);
	}
	
	
	/**
	 * Metodo que retorna una marcacion de favorito de acuerdo al post y 
	 * al autor de la marcacion.
	 * 
	 * @param idPostMarcado
	 * @param usernameAutorMarcacion
	 * @return
	 */
	public FavoritoEntity buscarMarcacion(Integer idPostMarcado, String usernameAutorMarcacion){
		String consulta = "from FavoritoEntity f "
				+ "where f.post.idPost = :idPost "
				+ "and f.autor.userName = :autorUsername";
		Query query = this.getSession().createQuery(consulta);
		query.setInteger("idPost", idPostMarcado);
		query.setString("autorUsername", usernameAutorMarcacion);
		FavoritoEntity fav = (FavoritoEntity) query.uniqueResult();
		return fav;  
	}
	
	
	
	/**
	 * Metodo que obtiene la lista de favoritos para un post dado.
	 * 
	 * @param postEntity
	 * @return
	 */
	public List<FavoritoEntity> listaFavoritosByPost(PostEntity postEntity){
		String consulta = "from FavoritoEntity f "
				+ "where f.post = :postEntity";
		Query query = this.getSession().createQuery(consulta);
		query.setEntity("postEntity", postEntity);
		List<FavoritoEntity> listaRetorno = query.list();
		return listaRetorno; 
	}
	
	
	/**
	 * Metodo que retorna la cantidad de favs que recibio un post dado
	 * 
	 * @param postEntity
	 * @return
	 */
	public Integer cantidadFavoritosByPost(PostEntity postEntity){
		//hacemos el calculo de cuantos favs tiene un post dado
		String consulta = "select count(*) from FavoritoEntity f "
				+ "where f.post.idPost = :idPost";
		Query query = this.getSession().createQuery(consulta);
		query.setInteger("idPost", postEntity.getIdPost());
		Long cantidadLong = (Long) query.uniqueResult();
		Integer cantidad = cantidadLong.intValue();
		return cantidad;
	}

}
