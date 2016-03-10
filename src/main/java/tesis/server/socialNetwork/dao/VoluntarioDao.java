package tesis.server.socialNetwork.dao;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.ejb.LocalBean;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.inject.Inject;

import org.hibernate.Criteria;
import org.hibernate.Query;
import org.hibernate.criterion.Restrictions;
import org.json.JSONObject;

import tesis.server.socialNetwork.entity.ContactoEntity;
import tesis.server.socialNetwork.entity.VoluntarioEntity;
import tesis.server.socialNetwork.utils.Utiles;


//@Controller
@LocalBean
public class VoluntarioDao extends GenericDao<VoluntarioEntity, String> {

	@Inject
	private ContactoDao contactoDao;
	
	
	@Override
	protected Class<VoluntarioEntity> getEntityBeanType() {
		return VoluntarioEntity.class;
	}
	
	
	@TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
	public void guardar(VoluntarioEntity voluntarioEntity){
		//ponemos el username (ID) todo a minuscula porque el find es case sensitive
		voluntarioEntity.setUserName(voluntarioEntity.getUserName().toLowerCase());
		//agregamos la fecha de inscripcion del objeto
		voluntarioEntity.setFechaIns(new Date());
		voluntarioEntity.setReputacion(1);
		voluntarioEntity.setActivo(true);
		this.save(voluntarioEntity);
	}
	
	
	@TransactionAttribute(TransactionAttributeType.REQUIRED)
	public void modificar(VoluntarioEntity voluntarioEntity){
		//el username lo ponemos a minuscula
		voluntarioEntity.setUserName(voluntarioEntity.getUserName().toLowerCase());
		this.update(voluntarioEntity);
	}
	
	
	/**
	 * Metodo que verifica la autenticidad de un usuario,
	 * retorna el usuario en caso correcto, sino null.
	 * 
	 * @param username
	 * @param password
	 * @return
	 */
	public VoluntarioEntity verificarUsuario(String username, String password){
		VoluntarioEntity voluntario = this.findByClassAndID(VoluntarioEntity.class, username.toLowerCase());
		if(voluntario == null){
			//si el usuario no existe
			return null;
		} else{
			//si el usuario existe pero el password no es correcto
			if(!voluntario.getPassword().equals(password)){
				return null;
			} else{
				return voluntario;
			}
		}
	}
	
	
	
	/**
	 * Metodo que verifica si un nombre de usuario ya esta en la Base de Datos
	 * @param newUsername
	 * @return
	 */
	public Boolean verificarUsernameRepetido(String newUsername){
		//traemos de la Base de Datos
		VoluntarioEntity entity = this.findByClassAndID(VoluntarioEntity.class, newUsername.toLowerCase());
		if(entity == null){
			//quiere decir que no existe todavia en la BD, por ende NO es repetido
			return false;
		} else{
			return true;
		}
	}
	
	
	/**
	 * Metodo que pasa una entidad Voluntario a un objeto JSON
	 * 
	 * @param voluntarioEntity
	 * @return
	 */
	public JSONObject getJSONFromVoluntario(VoluntarioEntity voluntarioEntity){
		/*
		 * antes de enviar verificamos la reputacion del voluntario
		 */
		Integer currentReputation = 1;
		//reportes
		Integer cantidadPosts = this.cantidadPosts(voluntarioEntity);
		currentReputation += (cantidadPosts*Utiles.PUNTAJE_POR_REPORTAR);
		
		//solucionados
		Integer cantidadSolucionados = this.cantidadSolucionadosPorVoluntario(voluntarioEntity);
		currentReputation += (cantidadSolucionados*Utiles.PUNTAJE_POR_SOLUCIONAR);
		
		//favoritos
		Integer cantidadFavoritos = this.cantidadFavoritosParaVoluntario(voluntarioEntity);
		currentReputation += (cantidadFavoritos*Utiles.PUNTAJE_FAVORITO);
		
		//noFavoritos
		Integer cantidadNoFavoritos = this.cantidadNoFavoritosParaVoluntario(voluntarioEntity);
		currentReputation += (cantidadNoFavoritos*Utiles.PUNTAJE_NO_FAVORITO);
		if(currentReputation != voluntarioEntity.getReputacion()){
			voluntarioEntity.setReputacion(currentReputation);
			this.modificar(voluntarioEntity);
		}
		
		JSONObject retorno = new JSONObject();
		//cuando un dato no esta cargado simplemente no lo agrega
		retorno.put("username", voluntarioEntity.getUsernameString());
		retorno.put("usernamestring", voluntarioEntity.getUsernameString());
		retorno.put("nombre", voluntarioEntity.getNombreReal());
		retorno.put("telefono", voluntarioEntity.getTelefono());
		retorno.put("email", voluntarioEntity.getEmail());
		retorno.put("ci", voluntarioEntity.getCi());
		retorno.put("direccion", voluntarioEntity.getDireccion());
		retorno.put("cantAmigos", contactoDao.getCantidadContactos(voluntarioEntity));
		retorno.put("reputacion", voluntarioEntity.getReputacion());
		retorno.put("cantReportes", this.cantidadPosts(voluntarioEntity));
		retorno.put("activo", voluntarioEntity.getActivo());
		if(voluntarioEntity.getFotoPerfilLink() != null){
			retorno.put("fotoPerfilLink", voluntarioEntity.getFotoPerfilLink());
		}
		
		return retorno;
	}
	
	
	/**
	 * Metodo que verifica si dos voluntarios ya son amigos entre si.
	 * 
	 * @param voluntario1
	 * @param voluntario2
	 * @return
	 */
	public boolean yaEsContacto(VoluntarioEntity voluntario1, VoluntarioEntity voluntario2){
		/*/verificamos que vountario2 se encuentre en la lista de contactos de voluntario1
		List<ContactoEntity> listaContactos = voluntario1.getContactos();
		if(listaContactos.isEmpty()){
			return false;
		} else {
			//por cada uno de los contactos vemos si el solicitante o el solicitado coincide con voluntario2
			for(ContactoEntity contacto : listaContactos){
				if((contacto.getVoluntario().equals(voluntario2)) || contacto.getContacto().equals(voluntario2)){
					//ya son amigos
					return true;
				}
			}
		}
		return false;*/
		
		//buscamos si hay alguna referencia en la entidad de contactos
		/*String consulta = "select exists (select ce.idAmistad from ContactoEntity ce where "
				+ "(ce.contacto = :voluntario1 and ce.voluntario = :voluntario2 ) or "
				+ "(ce.voluntario = :voluntario1 and ce.contacto = :voluntario2))";*/
		
		Query query = this.getSession().createQuery("from ContactoEntity ce where "
				+ "(ce.contacto = :voluntario1 and ce.voluntario = :voluntario2 ) or "
				+ "(ce.voluntario = :voluntario1 and ce.contacto = :voluntario2)");
		query.setEntity("voluntario1", voluntario1);
		query.setEntity("voluntario2", voluntario2);
		Boolean sonAmigos = query.setMaxResults(1).uniqueResult() != null;
		return sonAmigos;
	}
	
	
	//Busqueda
	public List<VoluntarioEntity> buscarUsuarios(String criterio){
		List<VoluntarioEntity> listaResultado = new ArrayList<VoluntarioEntity>();
		
		//verificamos si cual de los parametros es vacio o si ambos estan cargados
		if(criterio.isEmpty() && criterio.isEmpty()){
			//no hacemos nada si el criterio es vacio (NO se va a retornar la lista completa de usuarios!)
		} else {
			//quiere decir el criterio esta cargado
			Criteria criteria = getSession().createCriteria(VoluntarioEntity.class);
			criteria.add(Restrictions.or(Restrictions.like("userName", "%"+criterio+"%").ignoreCase(), Restrictions.like("nombreReal", "%"+criterio+"%").ignoreCase()));
			listaResultado = criteria.list();
		}
		if(listaResultado == null || listaResultado.isEmpty()){
			return null;
		} else {
			//cada elemento de la lista lo transformamos a un JSON
			/*for (int i = 0; i < listaResultado.size(); i++) {
				retorno.put(this.getJSONFromVoluntario(listaResultado.get(i)));
			}*/
			return listaResultado;
		}
	}
	
	
	/**
	 * Metodo que retorna la cantidad de posts hechos por un usuario
	 * @param voluntario
	 * @return
	 */
	public Integer cantidadPosts(VoluntarioEntity voluntario){
		String consulta = "select count(*) from PostEntity p "
				+ "where p.voluntario = :voluntario";
		Query query = this.getSession().createQuery(consulta);
		query.setEntity("voluntario", voluntario);
		Long cantidadL = (Long) query.uniqueResult();
		Integer cantidad = cantidadL.intValue();
		
		return cantidad;
	}
	
	
	/**
	 * Metodo que retorna la lista de contactos de un voluntarios
	 * @param voluntarioSolicitante
	 * @return
	 */
	public List<VoluntarioEntity> getListaContactos(VoluntarioEntity voluntarioSolicitante){
		//llamamos al metodo que obtiene la lista contactosEntity correspondientes
		List<ContactoEntity> listaEntities = contactoDao.getListaContactsEntity(voluntarioSolicitante);
		if(listaEntities == null || listaEntities.size() == 0){
			return null;
		} else {
			List<VoluntarioEntity> listaRetorno = new ArrayList<VoluntarioEntity>();
			for(ContactoEntity contacto: listaEntities){
				if(contacto.getVoluntario().getUserName() != voluntarioSolicitante.getUserName()){
					listaRetorno.add(contacto.getVoluntario());
				} else if(contacto.getContacto().getUserName() != voluntarioSolicitante.getUserName()){
					listaRetorno.add(contacto.getContacto());
				}
			}
			return listaRetorno;
		}
	}
	
	
	/**
	 * Metodo que retorna la cantidad total de voluntarios dentro de la red
	 * 
	 * @return
	 */
	public Integer cantidadVoluntariosTotal(){
		String consulta = "select count(*) from VoluntarioEntity v ";
		Query query = this.getSession().createQuery(consulta);
		Long cantidadLong = (Long) query.uniqueResult();
		Integer cantidadTotal = cantidadLong.intValue();
		
		return cantidadTotal;
	}
	
	
	/**
	 * Metodo que retorna la lista completa de usuarios
	 * 
	 * @return
	 */
	public List<VoluntarioEntity> getListAllUsers(){
		String consulta = "from VoluntarioEntity v order by nombreReal";
		Query query = getSession().createQuery(consulta);
		List listaSimple = query.list();
		return listaSimple;
	}
	
	
	/**
	 * Metodo que retorna la lista completa de 
	 * @return
	 */
	public List<VoluntarioEntity> getListUsersByRanking(){
		String consulta = "from VoluntarioEntity v order by v.reputacion desc";
		Query query = getSession().createQuery(consulta);
		List listaSimple = query.list();
		return listaSimple;
	}
	
	
	/**
	 * Metodo que calcula y actualiza la reputacion de un voluntario dependiendo de si:
	 * 1. posteo un nuevo reporte.
	 * 2. soluciono un reporte.
	 * 3. otro voluntario marco como FAV un reporte suyo.
	 * 4. otro voluntario quito su marca de FAV a un reporte suyo.
	 * 5. otro voluntario marco como NOFAV un reporte suyo.
	 * 6. otro voluntario quito su marca de NOFAV a un reporte suyo.
	 * 
	 * @param voluntario
	 * @param nuevoPost
	 * @param solucionaste
	 * @param nuevoFavorito
	 * @param quitoUnFavorito
	 * @param nuevoNoFavorito
	 * @param quitoUnNoFavorito
	 */
	public void updateReputation(VoluntarioEntity voluntario, Boolean nuevoPost, Boolean solucionaste, Boolean esRelevante, Boolean nuevoFavorito, Boolean quitoUnFavorito, 
			Boolean nuevoNoFavorito, Boolean quitoUnNoFavorito){
		
		Integer reputacion = voluntario.getReputacion();
		if(nuevoPost){
			reputacion += Utiles.PUNTAJE_POR_REPORTAR;
		}
		if(solucionaste){
			reputacion += Utiles.PUNTAJE_POR_SOLUCIONAR;
		}
		if(esRelevante){
			reputacion += Utiles.PUNTAJE_POR_RELEVANCIA;
		}
		if(nuevoFavorito){
			reputacion += Utiles.PUNTAJE_FAVORITO;
		}
		if(quitoUnFavorito){
			reputacion -= Utiles.PUNTAJE_FAVORITO;
		}
		if(nuevoNoFavorito){
			//es puntaje es negativo
			reputacion += Utiles.PUNTAJE_NO_FAVORITO;
		}
		if(quitoUnNoFavorito){
			//doble negativo suma
			reputacion -= Utiles.PUNTAJE_NO_FAVORITO;
		}
		
		
		voluntario.setReputacion(reputacion);
		this.modificar(voluntario);
	}
	
	
	/**
	 * Metodo que retorna la cantidad de reportes solucionados por el voluntario
	 * @param voluntarioEntity
	 * @return
	 */
	public Integer cantidadSolucionadosPorVoluntario(VoluntarioEntity voluntarioEntity){
		String consulta = "select count(*) from PostEntity p where p.solucionado = true and p.voluntarioQueSoluciona = :voluntario";
		Query query = this.getSession().createQuery(consulta);
		query.setEntity("voluntario", voluntarioEntity);
		Long cantidadLong = (Long) query.uniqueResult();
		Integer cantidadTotal = cantidadLong.intValue();
		
		return cantidadTotal;
	}
	
	
	/**
	 * Metodo que retorna la cantidad de favoritos recibidos por un voluntario
	 * @param voluntarioEntity
	 * @return
	 */
	public Integer cantidadFavoritosParaVoluntario(VoluntarioEntity voluntarioEntity){
		String consulta = "select count(*) from FavoritoEntity f where f.post.voluntario = :voluntario";
		Query query = this.getSession().createQuery(consulta);
		query.setEntity("voluntario", voluntarioEntity);
		Long cantidadLong = (Long) query.uniqueResult();
		Integer cantidadTotal = cantidadLong.intValue();
		
		return cantidadTotal;
	}
	
	
	
	/**
	 * Metodo que retorna la cantidad de no favoritos recibidos por un voluntario
	 * @param voluntarioEntity
	 * @return
	 */
	public Integer cantidadNoFavoritosParaVoluntario(VoluntarioEntity voluntarioEntity){
		String consulta = "select count(*) from NoFavoritoEntity nf where nf.post.voluntario = :voluntario";
		Query query = this.getSession().createQuery(consulta);
		query.setEntity("voluntario", voluntarioEntity);
		Long cantidadLong = (Long) query.uniqueResult();
		Integer cantidadTotal = cantidadLong.intValue();
		
		return cantidadTotal;
	}
	
	
	public JSONObject getSimpleJSONFromVoluntario(VoluntarioEntity voluntarioEntity){
		/*
		 * antes de enviar verificamos la reputacion del voluntario
		 */
		Integer currentReputation = 1;
		//reportes
		Integer cantidadPosts = this.cantidadPosts(voluntarioEntity);
		currentReputation += (cantidadPosts*Utiles.PUNTAJE_POR_REPORTAR);
		
		//solucionados
		Integer cantidadSolucionados = this.cantidadSolucionadosPorVoluntario(voluntarioEntity);
		currentReputation += (cantidadSolucionados*Utiles.PUNTAJE_POR_SOLUCIONAR);
		
		//favoritos
		Integer cantidadFavoritos = this.cantidadFavoritosParaVoluntario(voluntarioEntity);
		currentReputation += (cantidadFavoritos*Utiles.PUNTAJE_FAVORITO);
		
		//noFavoritos
		Integer cantidadNoFavoritos = this.cantidadNoFavoritosParaVoluntario(voluntarioEntity);
		currentReputation += (cantidadNoFavoritos*Utiles.PUNTAJE_NO_FAVORITO);
		if(currentReputation != voluntarioEntity.getReputacion()){
			voluntarioEntity.setReputacion(currentReputation);
			this.modificar(voluntarioEntity);
		}
		
		JSONObject retorno = new JSONObject();
		//cuando un dato no esta cargado simplemente no lo agrega
		retorno.put("username", voluntarioEntity.getUsernameString());
		retorno.put("usernamestring", voluntarioEntity.getUsernameString());
		retorno.put("nombre", voluntarioEntity.getNombreReal());
		retorno.put("reputacion", voluntarioEntity.getReputacion());
		retorno.put("categoria", voluntarioEntity.getCategoria());
		
		return retorno;
	}
	
	
	/**
	 * Metodo que retorna la lista completa de voluntarios de categoria A
	 * @return
	 */
	public List<VoluntarioEntity> getListCategoryA(){
		String consulta = "from VoluntarioEntity v where v.categoria = 'A'";
		Query query = getSession().createQuery(consulta);
		List listaSimple = query.list();
		return listaSimple;
	}
	
	
	/**
	 * Metodo que retorna la lista completa de voluntarios de Categoria B
	 * @return
	 */
	public List<VoluntarioEntity> getListCategoryB(){
		String consulta = "from VoluntarioEntity v where v.categoria = 'B'";
		Query query = getSession().createQuery(consulta);
		List listaSimple = query.list();
		return listaSimple;
	}
	
	
	
		
}


