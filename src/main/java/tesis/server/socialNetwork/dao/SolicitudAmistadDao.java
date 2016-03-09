package tesis.server.socialNetwork.dao;

import java.util.ArrayList;
import java.util.List;

import javax.ejb.EJB;
import javax.ejb.LocalBean;
import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.inject.Inject;

import org.hibernate.Criteria;
import org.hibernate.Query;
import org.hibernate.criterion.Restrictions;
import org.json.JSONObject;
//import org.springframework.stereotype.Controller;

import tesis.server.socialNetwork.entity.SolicitudAmistadEntity;
import tesis.server.socialNetwork.entity.VoluntarioEntity;
import tesis.server.socialNetwork.utils.Base64;


//@Controller
@LocalBean
public class SolicitudAmistadDao extends GenericDao<SolicitudAmistadEntity, Integer> {

	//acceso a Base de Datos
	@Inject
	private VoluntarioDao voluntarioDao;
	
	@Override
	protected Class<SolicitudAmistadEntity> getEntityBeanType() {
		return SolicitudAmistadEntity.class;
	}
	
	
	@TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
	public void guardar(SolicitudAmistadEntity solicitudAmistadEntity){
		//por defecto el valor de 'aceptada' sera FALSE
		solicitudAmistadEntity.setAceptada(false);
		this.save(solicitudAmistadEntity);
	}
	
	
	@TransactionAttribute(TransactionAttributeType.REQUIRED)
	public void modificar(SolicitudAmistadEntity solicitudAmistadEntity){
		this.update(solicitudAmistadEntity);
	}
	
	
	@TransactionAttribute(TransactionAttributeType.REQUIRED)
	public void eliminar(SolicitudAmistadEntity entity){
		this.delete(entity);
	}
	
	/**
	 * Metodo que trae la lista de solicitudes pendientes del voluntario solicitado
	 * 
	 * @param username
	 * @return
	 */
	public List<SolicitudAmistadEntity> getListaSolicitudesPendientes(String username){
		Criteria criteriaSolicitud = getSession().createCriteria(SolicitudAmistadEntity.class);
		criteriaSolicitud.createCriteria("usuarioSolicitado", "v");
		criteriaSolicitud.add(Restrictions.eq("v.userName", username));
		criteriaSolicitud.add(Restrictions.eq("aceptada", false));
		List<SolicitudAmistadEntity> listaPendientes = criteriaSolicitud.list();
		return listaPendientes;
	}
	
	
	/**
	 * Metodo que convierte una solicitud de amistad a un objeto JSON
	 * 
	 * @param solicitudAmistadEntity
	 * @return
	 */
	public JSONObject getJSONStringFromSolicitud(SolicitudAmistadEntity solicitudAmistadEntity){
		JSONObject retorno = new JSONObject();
		
		retorno.put("id", solicitudAmistadEntity.getIdSolicitudAmistad());
		retorno.put("voluntariosolicitante", voluntarioDao.getJSONFromVoluntario(solicitudAmistadEntity.getUsuarioSolicitante()));
		/*if(solicitudAmistadEntity.getUsuarioSolicitante().getFotoDePerfil() != null){
			retorno.put("fotoPerfil", Base64.encodeToString(solicitudAmistadEntity.getUsuarioSolicitante().getFotoDePerfil(), Base64.DEFAULT));
		}*/
		retorno.put("aceptada", solicitudAmistadEntity.getAceptada());
		
		return retorno;
	}
	
	
	
	/**
	 * Metodo que retorna la lista de solicitudes como un string.
	 * Cada solicitud es un objeto JSON.
	 * Es necesario quitar la informacion del solicitado del JSON, no es necesaria esta informacion
	 * 
	 * @param listaSolicitudes
	 * @return
	 */
	public String getListParsedFromSolicitudes(List<SolicitudAmistadEntity> listaSolicitudes){
		//creamos la lista a ser retornada
		List<JSONObject> retorno = new ArrayList<JSONObject>();
		//recorremos la lista del parametro
		for(int i=0; i<listaSolicitudes.size(); i++){
			JSONObject solicitud = getJSONStringFromSolicitud(listaSolicitudes.get(i));
			retorno.add(solicitud);
		}
		
		return retorno.toString();
	}
	
	public Boolean tienesSolicitudPendiente(VoluntarioEntity solicitante, VoluntarioEntity solicitado){
		String consulta = "from SolicitudAmistadEntity s where s.usuarioSolicitante = :solicitante and s.usuarioSolicitado = :solicitado";
		Query query = this.getSession().createQuery(consulta);
		query.setEntity("solicitante", solicitante);
		query.setEntity("solicitado", solicitado);
		List lista = query.list();
		if(lista.isEmpty()){
			return false;
		} else {
			return true;
		}
	}
	
	
	public Boolean teHaSolicitadoAmistad(VoluntarioEntity solicitante, VoluntarioEntity solicitado){
		String consulta = "from SolicitudAmistadEntity s where s.usuarioSolicitado = :solicitante and s.usuarioSolicitante = :solicitado";
		Query query = this.getSession().createQuery(consulta);
		query.setEntity("solicitante", solicitante);
		query.setEntity("solicitado", solicitado);
		List lista = query.list();
		if(lista.isEmpty()){
			return false;
		} else {
			return true;
		}
	}
	
	
	/**
	 * Metodo que retorna la solicitud en base al los usuarios solicitantes y solicitado
	 * 
	 * @param solicitante
	 * @param solicitado
	 * @return
	 */
	public SolicitudAmistadEntity getSolicitudFromVolunteers(VoluntarioEntity solicitante, VoluntarioEntity solicitado){
		String consulta = "from SolicitudAmistadEntity s where s.usuarioSolicitado = :solicitado and s.usuarioSolicitante = :solicitante";
		Query query = this.getSession().createQuery(consulta);
		query.setEntity("solicitante", solicitante);
		query.setEntity("solicitado", solicitado);
		SolicitudAmistadEntity entity = (SolicitudAmistadEntity) query.uniqueResult();
		return entity;
	}
}
