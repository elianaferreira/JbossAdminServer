package tesis.server.socialNetwork.dao;


import java.util.List;

import javax.ejb.LocalBean;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.inject.Inject;

import org.json.JSONException;
import org.json.JSONObject;
//import org.springframework.stereotype.Controller;

//import com.sun.xml.ws.api.tx.at.Transactional;


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
		try {
			restriccion.put("adminName", adminName);
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		List<AdminEntity> lista = this.getListOfEntitiesWithRestrictionsLike(AdminEntity.class, restriccion);
		//la lista en teoria seria de un solo elemento
		if(lista == null || lista.size() == 0){
			return null;
		} else{
			AdminEntity admin = lista.get(0);
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
				retorno.put("error", "La contrasena no coincide.");
				return retorno;
			} else {
				//cambiamos el estado del atributo logged a TRUE
				admin.setLogged(true);
				//hacemos el update
				try{
					//guardamos el accessToken
					String accessToken = adminAccessTokenDao.guardar(adminName);
					if(accessToken == null){
						retorno = new JSONObject();
						retorno.put("error", "Ha ocurrido un error al iniciar sesion.");
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
					retorno.put("error", "Ha ocurrido un error al iniciar sesion.");
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
		retorno.put("nombre", admin.getNombre());
		retorno.put("apellido", admin.getApellido());
		//retorno.put("password", admin.getPassword());
		
		return retorno;	
	}
}
