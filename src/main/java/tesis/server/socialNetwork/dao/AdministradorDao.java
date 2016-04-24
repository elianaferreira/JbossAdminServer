package tesis.server.socialNetwork.dao;


import java.util.Date;
import java.util.List;

import javax.ejb.LocalBean;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.inject.Inject;

import org.hibernate.Query;
import org.json.JSONObject;
import org.springframework.stereotype.Controller;

import tesis.server.socialNetwork.entity.AdminAccessTokenEntity;
import tesis.server.socialNetwork.entity.AdminEntity;

//@Controller
//@LocalBean
public class AdministradorDao extends GenericDao<AdminEntity, Integer> {

	@Override
	protected Class<AdminEntity> getEntityBeanType() {
		return AdminEntity.class;
	}
	
	@Inject
	AdminAccessTokenDao adminAccessTokenDao;
	
	
	@TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
	public void guardar(AdminEntity entity){
		//ponemos a minuscula el username del admin
		entity.setAdminName(entity.getAdminName().toLowerCase());
		entity.setFechaIns(new Date());
		entity.setLogged(false);
		entity.setEliminado(false);
		this.save(entity);
		
	}
	
	
	
	@TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
	public void modificar(AdminEntity entity){
		this.update(entity);
	}
	

	
	public AdminEntity verificarAdministrador(String adminName, String accessToken){
		//String consulta = "from AdminEntity ad where ad.adminName = :adminName and ad.password = :password";
		
		
		/*/creamos el JSON de restricciones que sera en base al username
		JSONObject restriccion = new JSONObject();
		restriccion.put("adminName", adminName);
		List<AdminEntity> lista = this.getListOfEntitiesWithRestrictionsLike(AdminEntity.class, restriccion);
		//la lista en teoria seria de un solo elemento
		if(lista == null || lista.size() == 0){
			return null;
		} else{
			AdminEntity admin = lista.get(0);
			if(admin.getLogged()){
				return admin;
			} else {
				return null;
			}
		}*/
		
		JSONObject restriccion = new JSONObject();
		restriccion.put("adminName", adminName);
		List<AdminEntity> lista = this.getListOfEntitiesWithRestrictionsLike(AdminEntity.class, restriccion);
		//la lista en teoria seria de un solo elemento
		if(lista == null || lista.size() == 0){
			return null;
		} else{
			AdminEntity admin = lista.get(0);
			if(admin.getEliminado()){
				return null;
			}
			//verificamos el accessToken
			AdminAccessTokenEntity accessTokenEntity = adminAccessTokenDao.findByClassAndID(AdminAccessTokenEntity.class, accessToken);
			if(accessTokenEntity == null){
				return null;
			} else {
				if(admin.getLogged()){
					return admin;
				} else {
					return null;
				}
			}
		}
	}
	
	
	
	/**
	 * Metodo que se encarga de iniciar la sesion del administrador
	 * 
	 * @param admin
	 * @return
	 */
	@TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
	public JSONObject iniciarSesionAdmin(String adminName, String password){
		JSONObject retorno = null;
		JSONObject restriccion = new JSONObject();
		restriccion.put("adminName", adminName);
		List<AdminEntity> lista = this.getListOfEntitiesWithRestrictionsLike(AdminEntity.class, restriccion);
		//la lista en teoria seria de un solo elemento
		if(lista == null || lista.size() == 0){
			retorno = new JSONObject();
			retorno.put("error", "No existe un administrador con ese nombre.");
			return retorno;
		} else{
			AdminEntity admin = lista.get(0);
			if(!admin.getPassword().equals(password)){
				retorno = new JSONObject();
				retorno.put("error", "La contrase\u00f1a no coincide.");
				return retorno;
			} else {
				if(admin.getEliminado()){
					retorno = new JSONObject();
					retorno.put("error", "Usted ha sido eliminado de la lista de Administradores.");
					return retorno;
				}
				//cambiamos el estado del atributo logged a TRUE
				admin.setLogged(true);
				//hacemos el update
				try{
					//guardamos el accessToken
					String accessToken = adminAccessTokenDao.guardar(adminName);
					if(accessToken == null){
						retorno = new JSONObject();
						retorno.put("error", "Ha ocurrido un error al iniciar sesi\u00f3n.");
						return retorno;
					} else {
						this.update(admin);
						retorno = this.getJsonFromAdmin(admin);
						retorno.put("accessToken", accessToken);
						return retorno;
					}
				} catch (Exception ex){
					ex.printStackTrace();
					retorno = new JSONObject();
					retorno.put("error", "Ha ocurrido un error al iniciar sesi\u00f3n.");
					return retorno;
				}
			}
		}
	}
	
	
	/**
	 * Metodo que cierra la sesion del administrador
	 * @param admin
	 * @return
	 */
	@TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
	public Boolean cerrarSesionAdmin(AdminEntity admin){
		//cambiamos el estado del atributo logged a FALSE
		admin.setLogged(false);
		//hacemos el update
		try{
			this.update(admin);
			return true;
		} catch (Exception ex){
			ex.printStackTrace();
			return false;
		}
	}
	
	
	
	/**
	 * Metodo que retorna un JSON de la entidad del Administrador
	 * @param admin
	 * @return
	 */
	public JSONObject getJsonFromAdmin(AdminEntity admin){
		JSONObject retorno = new JSONObject();
		retorno.put("adminname", admin.getAdminName());
		retorno.put("name", admin.getNombre());
		retorno.put("lastname", admin.getApellido());
		retorno.put("ci", admin.getCi());
		retorno.put("phone", admin.getTelefono());
		retorno.put("email", admin.getEmail());
		retorno.put("address", admin.getDireccion());
		
		return retorno;	
	}
	
	
	/**
	 * Metodo que verifica si un nombre de administrador ya existe
	 * 
	 * @param adminName
	 * @return
	 */
	public Boolean yaExisteAdministrador(String adminName){
		String consulta = "from AdminEntity a where a.adminName = :adminName";
		Query query = this.getSession().createQuery(consulta);
		query.setParameter("adminName", adminName);
		AdminEntity entity = (AdminEntity) query.uniqueResult();
		if(entity == null){
			return false;
		} else {
			return true;
		}
	}
	
	
	public AdminEntity yaExisteAministrador(String adminName){
		String consulta = "from AdminEntity a where a.adminName = :adminName";
		Query query = this.getSession().createQuery(consulta);
		query.setParameter("adminName", adminName);
		AdminEntity entity = (AdminEntity) query.uniqueResult();
		if(entity == null){
			return null;
		} else {
			return entity;
		}
	}
	
	
	/**
	 * Metodo que retorna todos los datos del administrador que pueden ser modificados
	 * 
	 * @param admin
	 * @return
	 */
	public JSONObject getAllDataForEdit(AdminEntity admin){
		JSONObject retorno = new JSONObject();
		retorno.put("admin", admin.getAdminName());
		retorno.put("name", admin.getNombre());
		retorno.put("lastname", admin.getApellido());
		retorno.put("password", admin.getPassword());
		retorno.put("ci", admin.getCi());
		retorno.put("phone", admin.getTelefono());
		retorno.put("email", admin.getEmail());
		retorno.put("address", admin.getDireccion());
		return retorno;
	}
	
	
	/**
	 * Metodo que retorna la lista completa de Administradores activos dentro del sistema
	 * @return
	 */
	public List<AdminEntity> getListaActivos(){
		String consulta = "from AdminEntity a where a.eliminado = false";
		Query query = this.getSession().createQuery(consulta);
		List lista = query.list();
		return lista;
	}
	
	
	/**
	 * Meoto que retorna la lista completa de Administradores que han sido dados de baja
	 * @return
	 */
	public List<AdminEntity> getListaInactivos(){
		String consulta = "from AdminEntity a where a.eliminado = true";
		Query query = this.getSession().createQuery(consulta);
		List lista = query.list();
		return lista;
	}
	
	/**
	 * Eliminamos todos los datos de las tablas dentro de una unica transaccion
	 */
	@TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
	public void deleteAllTables(){
		//FAVs
		String deleteFavs = "delete from FavoritoEntity f";
		Query query = this.getSession().createQuery(deleteFavs);
		query.executeUpdate();
		
		//NOFAVs
		String deleteNoFavs = "delete from NoFavoritoEntity nf";
		query = this.getSession().createQuery(deleteNoFavs);
		query.executeUpdate();
		
		//comentarios
		String comentarios = "delete from ComentarioEntity c";
		query = this.getSession().createQuery(comentarios);
		query.executeUpdate();
		
		//notificaciones
		String notificaciones = "delete from NotificacionEntity n";
		query = this.getSession().createQuery(notificaciones);
		query.executeUpdate();
		
		//solicitudes de amistad
		String solicitudes = "delete from SolicitudAmistadEntity s";
		query = this.getSession().createQuery(solicitudes);
		query.executeUpdate();
		
		String contactos = "delete from ContactoEntity co";
		query = this.getSession().createQuery(contactos);
		query.executeUpdate();
				
		//repost
		String reposts = "delete from RepostEntity re";
		query = this.getSession().createQuery(reposts);
		query.executeUpdate();
				
		String posts = "delete from PostEntity p";
		query = this.getSession().createQuery(posts);
		query.executeUpdate();
				
		//voluntarios
		String voluntarios = "delete from VoluntarioEntity v";
		query = this.getSession().createQuery(voluntarios);
		query.executeUpdate();
		
		
		String campanhas = "delete from CampanhaEntity ca";
		query = this.getSession().createQuery(campanhas);
		query.executeUpdate();
		
		
	}
}
