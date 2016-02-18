package tesis.server.socialNetwork.dao;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

import javax.ejb.EJB;
import javax.ejb.LocalBean;
import javax.ejb.Stateless;
import javax.inject.Inject;

import org.hibernate.Query;
import org.json.JSONObject;
//import org.springframework.stereotype.Controller;

import tesis.server.socialNetwork.entity.PostEntity;
import tesis.server.socialNetwork.entity.RepostEntity;
import tesis.server.socialNetwork.entity.VoluntarioEntity;
import tesis.server.socialNetwork.utils.SortedByDate;
import tesis.server.socialNetwork.utils.Utiles;

//@Controller
@LocalBean
public class RepostDao extends GenericDao<RepostEntity, Integer> {

	@Override
	protected Class<RepostEntity> getEntityBeanType() {
		return RepostEntity.class;
	}
	
	@Inject
	private PostDao postDao;
	

	public void guardar(RepostEntity repost){
		Date date = new Date();
		repost.setFechaRepost(new Timestamp(date.getTime()));
		this.save(repost);
	}
	
	public void eliminar(RepostEntity repost){
		this.delete(repost);
	}
	
	
	/**
	 * Metodo que retorna los repost de un usuario a partir de una fecha dada
	 * 
	 * @param usernameAutor
	 * @param ultimaActualizacion
	 * @return
	 */
	public List<RepostEntity> getReposts(String username, Timestamp ultimaActualizacion, Boolean nuevos){
		/*String consulta = "from RepostEntity r where r.autorRepost.userName = :autor and "
				+ "r.fechaRepost > :ultimaactualizacion order by r.fechaRepost desc";
		
		Query query = this.getSession().createQuery(consulta);
		query.setParameter("autor", usernameAutor);
		query.setParameter("ultimaactualizacion", ultimaActualizacion);
		List lista = query.list();
		
		return lista;*/
		System.out.println("Usario " + username + "; timestamp: " + ultimaActualizacion.toString() + "; son nuevos?: " + nuevos.toString());
		String condicionActualizacion = "";
		//String condicionNuevos = " and rp.fechaRepost> :ultimaactualizacion order by rp.fechaRepost asc";
		//String condicionViejos = " and rp.fechaRepost< :ultimaactualizacion order by rp.fechaRepost desc";
		
		//order doesn't matter
		String condicionNuevos = " and rp.fechaRepost> :ultimaactualizacion";
		String condicionViejos = " and rp.fechaRepost< :ultimaactualizacion";
		if(nuevos){
			condicionActualizacion = condicionNuevos;
		} else {
			condicionActualizacion = condicionViejos;
		}
		
		String consulta = "from RepostEntity rp "
				+ "where (rp.autorRepost in "
				+ "(select c.voluntario from ContactoEntity c where c.contacto.userName= :username )"
				+ "or rp.autorRepost in "
				+ "(select c1.contacto from ContactoEntity c1 where c1.voluntario.userName= :username) "
				+ "or rp.autorRepost in "
				+ "(select v.userName from VoluntarioEntity v where v.userName= :username))"
				+ condicionActualizacion;
		Query query = this.getSession().createQuery(consulta);
		query.setParameter("username", username.toLowerCase());
		query.setParameter("ultimaactualizacion", ultimaActualizacion);
		//limitar la cantidad de registros
		query.setMaxResults(5);
		List lista = query.list();
		
		return lista;
	}
	
	
	/**
	 * Metodo que retorna los reposts hechos por el usuario solicitante
	 * @param username
	 * @param ultimaActualizacion
	 * @param nuevos
	 * @return
	 */
	public List<RepostEntity> getOwnReposts(String username, Timestamp ultimaActualizacion, Boolean nuevos){
		System.out.println("Usario " + username + "; timestamp: " + ultimaActualizacion.toString() + "; son nuevos?: " + nuevos.toString());
		String condicionActualizacion = "";
		String condicionNuevos = " and rp.fechaRepost> :ultimaactualizacion order by rp.fechaRepost asc";
		String condicionViejos = " and rp.fechaRepost< :ultimaactualizacion order by rp.fechaRepost desc";
		if(nuevos){
			condicionActualizacion = condicionNuevos;
		} else {
			condicionActualizacion = condicionViejos;
		}
		
		String consulta = "from RepostEntity rp where rp.autorRepost.userName = :username"
				+ condicionActualizacion;
		Query query = this.getSession().createQuery(consulta);
		query.setParameter("username", username.toLowerCase());
		query.setParameter("ultimaactualizacion", ultimaActualizacion);
		//limitar la cantidad de registros
		query.setMaxResults(3);
		List lista = query.list();
		
		return lista;
	}

	
	/**
	 * Metodo que retorna el JSON correspondiente de un repost
	 * 
	 * @param repost
	 * @return
	 */
	public JSONObject getJSONFromRepost(RepostEntity repost, String usernameSolicitante){
		JSONObject retorno = new JSONObject();
		retorno.put("idRepost", repost.getIdRepost());
		retorno.put("fecha", repost.getFechaRepost());
		retorno.put("autor", repost.getAutorRepost().getNombreReal());
		retorno.put("post", postDao.getJSONFromPost(usernameSolicitante, repost.getPost()));
		
		return retorno;
	}
	
	
	/**
	 * Metodo que retorna solo los posts cuya diferencia con el post original sea X tiempo,
	 * si varios voluntarios repostearon el mismo post despues del lapso de tiempo, los mas cercanos
	 * en fecha entre si son descartados tomando solo uno de ellos, y se mostraran solo dependiendo de la fecha entre los
	 * reposts.
	 * 
	 * @param listaOriginal
	 * @return
	 */
	public List<RepostEntity> getRepostsMasDistantesDelPost(List<RepostEntity> listaOriginal){
		List<RepostEntity> listaFinalRetorno = new ArrayList<RepostEntity>();
		/*
		 * agregamos el repost a la lista final si entre la fecha del repost y el post paso X tiempo y
		 * 1. la lista de reposts esta vacia o
		 * 2. entre el ultimo respost de este post y este repost actual han pasado mas de X tiempo
		 * 
		 */
		for(int i=0; i<listaOriginal.size(); i++){
			RepostEntity repostPendiente = listaOriginal.get(i);
			long diffEntreOriginalYRepost = repostPendiente.getFechaRepost().getTime() - repostPendiente.getPost().getFechaPost().getTime();
			long horasPasadasEntreRepostYPost = TimeUnit.HOURS.convert(diffEntreOriginalYRepost, TimeUnit.MILLISECONDS);
			//verificamos si es mayor a la diferencia aceptada (para no tener un post y su repost muy juntos)
			if(horasPasadasEntreRepostYPost > Utiles.HORAS_ENTRE_POST_Y_REPOST){
				//si la lista esta vacia lo agregamos
				if(listaFinalRetorno.isEmpty()){
					listaFinalRetorno.add(repostPendiente);
				} else {
					//verificamos si hay otros reposts en la lista pertenecientes al mismo post
					//paso hacia atras
					Boolean existeOtroRepostDelMismoPost = false;
					for(int j=listaFinalRetorno.size()-1; j>=0; j--){
						RepostEntity repostAgregado = listaFinalRetorno.get(j);
						if(repostAgregado.getPost().getIdPost() == repostPendiente.getPost().getIdPost()){
							existeOtroRepostDelMismoPost = true;
							//lo agregamos solo si la fecha es superior al ultimo (en teoria esta ordenado ya que asi se guarda en la BD)
							long diffEntreReposts = repostPendiente.getFechaRepost().getTime() - repostAgregado.getFechaRepost().getTime();
							long horasPasadasEntreRepost = TimeUnit.HOURS.convert(diffEntreReposts, TimeUnit.MILLISECONDS);
							if(horasPasadasEntreRepost > Utiles.HORAS_ENTRE_MISMO_REPOST){
								listaFinalRetorno.add(repostPendiente);
							}
							//se va solo hasta el ultimo repost del mismo post, despues ya no avanza
							break;
						}
					}
					//si la lista ya esta cargada pero es el primer repost del post
					if(!existeOtroRepostDelMismoPost){
						listaFinalRetorno.add(repostPendiente);
					}
				}
			}
			
		}
		return listaFinalRetorno;
	}
	
	
	public List<JSONObject> getHomeTimeline(VoluntarioEntity voluntario, Timestamp timestamp){
		
		String consultaPost = "from PostEntity p where p.voluntario = :usuario and p.fechaPost < :ultimaActualizacion order by p.fechaPost desc";
		Query queryPost = this.getSession().createQuery(consultaPost);
		queryPost.setParameter("usuario", voluntario);
		queryPost.setParameter("ultimaActualizacion", timestamp);
		queryPost.setMaxResults(3);
		List<PostEntity> listaPost = queryPost.list();
		
		String consultaRespost = "from RepostEntity r where r.autorRepost = :usuario and r.fechaRepost < :ultimaActualizacion order by r.fechaRepost desc";
		Query queryRepost = this.getSession().createQuery(consultaRespost);
		queryRepost.setParameter("usuario", voluntario);
		queryRepost.setParameter("ultimaActualizacion", timestamp);
		queryRepost.setMaxResults(3);
		List<RepostEntity> listaRepost = queryRepost.list();
		
		
		//creammos JSON de cada uno y lo seteamos a la lista de retorno
		List<JSONObject> arrayRetorno = new ArrayList<JSONObject>();
		for(int i=0; i<listaPost.size(); i++){
			arrayRetorno.add(postDao.getJSONFromPost("", listaPost.get(i)));
		}
		for(int j=0; j<listaRepost.size(); j++){
			arrayRetorno.add(this.getJSONFromRepost(listaRepost.get(j), ""));
		}
		
		Collections.sort(arrayRetorno, new SortedByDate());
		
		return arrayRetorno;
	}
}
