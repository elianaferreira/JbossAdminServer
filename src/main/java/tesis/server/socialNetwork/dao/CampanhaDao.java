package tesis.server.socialNetwork.dao;

import java.sql.Timestamp;
import java.util.Date;
import java.util.List;

import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.inject.Inject;

import org.hibernate.Query;
import org.json.JSONObject;

import tesis.server.socialNetwork.entity.CampanhaEntity;
import tesis.server.socialNetwork.entity.NotificacionEntity;
import tesis.server.socialNetwork.entity.VoluntarioEntity;
import tesis.server.socialNetwork.utils.Utiles;

public class CampanhaDao extends GenericDao<CampanhaEntity, Integer> {
	
	@Inject
	NotificacionDao notificacionDao;

	@Override
	protected Class<CampanhaEntity> getEntityBeanType() {
		return CampanhaEntity.class;
	}

	@TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
	public void guardar(CampanhaEntity entity){
		entity.setActiva(true);
		this.save(entity);
	}
	
	@TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
	public void modificar(CampanhaEntity entity){
		this.update(entity);
	}
	
	public CampanhaEntity buscarPorNombre(String nombreBuscar){
		String consulta = "from CampanhaEntity c where c.nombreCampanha=:nombre";
		Query query = this.getSession().createQuery(consulta);
		query.setString("nombre", nombreBuscar);
		CampanhaEntity c =  (CampanhaEntity) query.uniqueResult();
		 
		 return c;
		
	}
	
	
	public List<CampanhaEntity> getAll(){
		String consulta = "from CampanhaEntity c";
		Query query = this.getSession().createQuery(consulta);
		
		List lista = query.list();
		return lista;
	}
	
	
	public JSONObject getJSONFromCampanha(CampanhaEntity c, String username){
		JSONObject jsonRetorno = new JSONObject();
		jsonRetorno.put("id", c.getIdCampanha());
		jsonRetorno.put("nombre", c.getNombreCampanha());
		jsonRetorno.put("mensaje", c.getMensaje());
		jsonRetorno.put("fechaInicio", c.getFechaLanzamiento());
		jsonRetorno.put("fechaFin", c.getFechaFinalizacion());
		jsonRetorno.put("cantAdheridos", c.getVoluntariosAdheridos().size());
		jsonRetorno.put("cantInvitados", c.getVoluntariosInvitados().size());
		
		//verificamos si es un usuario el que lo solicita
		if(username != ""){
			//buscamos en la lista de adheridos
			List<VoluntarioEntity> listaAdheridos = c.getVoluntariosAdheridos();
			for(int j=0; j<listaAdheridos.size(); j++){
				if(listaAdheridos.get(j).getUserName().equals(username)){
					jsonRetorno.put("adherido", true);
					break;
				}
			}
		}
		
		return jsonRetorno;
	}
	
	
	/**
	 * Metodo que guarda una entidad notificacion para el voluntario
	 * @param listaInvitados
	 */
	public void guardarNotificacionParaVoluntarios(CampanhaEntity campanha, List<VoluntarioEntity> listaInvitados){
		for(int i=0; i<listaInvitados.size(); i++){
			try{
				NotificacionEntity notificacion = new NotificacionEntity();
				notificacion.setTipoNotificacion(Utiles.NOTIF_INVITADO_CAMPANHA);
				notificacion.setMensaje("El Administrador te ha invitado a unirte.");
				notificacion.setVoluntarioTarget(listaInvitados.get(i));
				notificacion.setCampanha(campanha);
				notificacionDao.guardar(notificacion);
			} catch(Exception e){
				e.printStackTrace();
			}
		}
	}
	
	
	/**
	 * Metodo que retorna todas la campanhas de forma paginada en base a la fecha de lanzamiento
	 * 
	 * @param ultimaFecha
	 * @return
	 */
	public List<CampanhaEntity> listaCampanhas(Date ultimaFecha){
		
		//"from PostEntity p where p.quienDebeSolucionar = :ente and p.fechaPost < :ultimaActualizacion order by p.fechaPost desc
		String consulta = "from CampanhaEntity c where c.timestampGuardado < ultimaActualizacion order by c.fechaLanzamiento desc";
		Query query = this.getSession().createQuery(consulta);
		query.setParameter("ultimaActualizacion", ultimaFecha);
		query.setMaxResults(2);
		List lista = query.list();
		
		return lista;
	}
	
	
	/**
	 * Metodo que retorna la lista de campanhas de forma paginada en base al ID
	 * 
	 * @param idUltima
	 * @return
	 */
	public List<CampanhaEntity> listaCampanhas(Integer idUltima){
		String consulta = "";
		if(idUltima == null){
			consulta = "from CampanhaEntity c order by c.idCampanha desc";
		} else {
			consulta = "from CampanhaEntity c where c.idCampanha < :id order by c.idCampanha desc";
		}
		Query query = this.getSession().createQuery(consulta);
		if(idUltima != null){
			query.setInteger("id", idUltima);
		}
		
		query.setMaxResults(2);
		List lista = query.list();
		
		return lista;
	}
	
	
	/**
	 * Metodo que retorna la lista de campanhas que hasta la fecha aun siguen vigentes
	 * 
	 * @return
	 */
	public List<CampanhaEntity> getCampanhasVigentes(){
		Date currentDate = new Date();
		String consulta = "from CampanhaEntity c where c.fechaFinalizacion >= :currentDate order by c.idCampanha desc";
		Query query = this.getSession().createQuery(consulta);
		query.setParameter("currentDate", currentDate);
		query.setMaxResults(2);
		List lista = query.list();
		
		return lista;
	}
	
	
	/**
	 * retorna las campanhas de forma paginada en base al ultimo ID consultado y dependiendo de si se solicitan
	 * los mas recientes o los mas viejos
	 * 
	 * @param ultimoID
	 * @param masRecientes
	 * @return
	 */
	public List<CampanhaEntity> getCampanhasVigentesPaginado(Integer ultimoID, Boolean masRecientes){
		Date currentDate = new Date();
		String consulta = "";
		if(masRecientes){
			consulta = "from CampanhaEntity c where c.fechaFinalizacion >= :currentDate and c.idCampanha > :ultimoID order by c.idCampanha asc";
		} else {
			//mas antiguos
			consulta = "from CampanhaEntity c where c.fechaFinalizacion >= :currentDate and c.idCampanha < :ultimoID order by c.idCampanha desc";
		}
		
		Query query = this.getSession().createQuery(consulta);
		query.setParameter("currentDate", currentDate);
		query.setParameter("ultimoID", ultimoID);
		query.setMaxResults(2);
		List lista = query.list();
		
		return lista;
	}
}
