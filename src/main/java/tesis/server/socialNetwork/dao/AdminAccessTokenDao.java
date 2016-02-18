package tesis.server.socialNetwork.dao;


import java.sql.Timestamp;
import java.util.Date;

import org.hibernate.Query;

import tesis.server.socialNetwork.entity.AdminAccessTokenEntity;
import tesis.server.socialNetwork.utils.Utiles;

public class AdminAccessTokenDao extends GenericDao<AdminAccessTokenEntity, String> {

	@Override
	protected Class<AdminAccessTokenEntity> getEntityBeanType() {
		return AdminAccessTokenEntity.class;
	}

	//solo puede haber una accessToken activo a la vez, por ello para guardar uno se debe eliminar el anterior
	/**
	 * Metodo que crea y guarda un accessToken para el administrador
	 * @param adminUsername
	 * @return
	 */
	public String guardar(String adminUsername){
		//obtenemos el timeStamp actual
		Date date = new Date();
		Timestamp timestamp = new Timestamp(date.getTime());
		String accessToken = Utiles.getMD5(adminUsername+timestamp.toString());
		try{
			deleteExistingAccessToken();
			AdminAccessTokenEntity entity = new AdminAccessTokenEntity();
			entity.setCurrentAccessToken(accessToken);
			this.save(entity);
			return accessToken;
		} catch(Exception e){
			e.printStackTrace();
			return null;
		}
	}
	
	
	private void deleteExistingAccessToken(){
		String consulta = "delete from AdminAccessTokenEntity a";
		Query query = getSession().createQuery(consulta);
		query.executeUpdate();
	}

}
