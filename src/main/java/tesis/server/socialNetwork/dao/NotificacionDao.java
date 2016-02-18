package tesis.server.socialNetwork.dao;

import java.sql.Timestamp;
import java.util.Date;
import java.util.List;

import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;

import org.hibernate.Query;

import tesis.server.socialNetwork.entity.CampanhaEntity;
import tesis.server.socialNetwork.entity.NotificacionEntity;
import tesis.server.socialNetwork.entity.VoluntarioEntity;
import tesis.server.socialNetwork.utils.Utiles;

public class NotificacionDao extends GenericDao<NotificacionEntity, Integer> {

	@Override
	protected Class<NotificacionEntity> getEntityBeanType() {
		return NotificacionEntity.class;
	}
	
	
	@TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
	public void guardar(NotificacionEntity entity){
		Date date = new Date();
		Timestamp timestamp = new Timestamp(date.getTime());
		entity.setFechaCreacionNotificacion(timestamp);
		entity.setAceptada(false);
		entity.setRechazada(false);
		this.save(entity);
	}
	
	
	@TransactionAttribute(TransactionAttributeType.REQUIRED)
	public void eliminar(NotificacionEntity entity){
		this.delete(entity);
	}
	
	
	@TransactionAttribute(TransactionAttributeType.REQUIRED)
	public void modificar(NotificacionEntity entity){
		Date date = new Date();
		Timestamp timestamp = new Timestamp(date.getTime());
		entity.setFechaVisualizacion(timestamp);
		this.update(entity);
	}
	
	
	/**
	 * 
	 * @param username
	 * @param ultimaActualizacion puede ser null
	 * @param sinceId indica el ID de la notificacion que esta en el tope de la pila ---> https://dev.twitter.com/rest/reference/get/statuses/mentions_timeline
	 * @return
	 */
	
	//TODO verificar que no este enviando solicitudes de amistad ya aceptadas
	public List<NotificacionEntity> getListaNotificacion(String username, Integer ultimoID){
		String consulta = "";
		if(ultimoID == null){
			consulta = "from NotificacionEntity n where n.voluntarioTarget.userName= :username and n.aceptada != true and n.rechazada != true order by n.idNotificacion desc";
		} else {
			consulta = "from NotificacionEntity n where n.voluntarioTarget.userName= :username and n.aceptada != true and n.rechazada != true and n.idNotificacion > :ultimoID order by n.idNotificacion asc";
		}
		
		Query query = this.getSession().createQuery(consulta);
		query.setParameter("username", username.toLowerCase());
		if(ultimoID != null){
			query.setParameter("ultimoID", ultimoID);
		}
		//query.setMaxResults(70);
		List<NotificacionEntity> lista = query.list();
			
		return lista;
	}
	
	
	/**
	 * Metodo que crea y guarda una notificacion de solicitud de amistad
	 * 
	 * @param solicitante
	 * @param solicitado
	 */
	public void crearNotificacionSolicitudAmistad(VoluntarioEntity solicitante, VoluntarioEntity solicitado){
		try {
			NotificacionEntity notif = new NotificacionEntity();
			notif.setTipoNotificacion(Utiles.NOTIF_NUEVA_SOLICITUD_AMISTAD);
			notif.setMensaje("Te ha enviado una solicitud de amistad");
			notif.setVoluntarioCreadorNotificacion(solicitante);
			notif.setVoluntarioTarget(solicitado);
			this.guardar(notif);
		} catch(Exception e){
			e.printStackTrace();
		}
	}
	
	
	/**
	 * Metodo que busca una notificacion de acuerdo a los voluntarios implicados
	 * 
	 * @param solicitante
	 * @param solicitado
	 * @return
	 */
	public NotificacionEntity buscarPorVoluntarios(VoluntarioEntity solicitante, VoluntarioEntity solicitado){
		String consulta = "from NotificacionEntity n where n.voluntarioCreadorNotificacion = :solicitante and n.voluntarioTarget = :target";
		Query query = this.getSession().createQuery(consulta);
		query.setEntity("solicitante", solicitante);
		query.setEntity("target", solicitado);
		NotificacionEntity entity = (NotificacionEntity) query.uniqueResult();
		return entity;
	}
	
	
	/**
	 * Metodo que busca una notificacion por la campanha y el voluntario invitado
	 * 
	 * @param target
	 * @param campanha
	 * @return
	 */
	public NotificacionEntity buscarPorCampanhaYvoluntario(VoluntarioEntity target, CampanhaEntity campanha){
		String consulta = "from NotificacionEntity n where n.voluntarioTarget = :target and campanha = :campanha";
		Query query = this.getSession().createQuery(consulta);
		query.setEntity("target", target);
		query.setEntity("campanha", campanha);
		NotificacionEntity entity = (NotificacionEntity) query.uniqueResult();
		return entity;
	}

}
