package tesis.server.socialNetwork.dao;

import java.sql.Timestamp;
import java.util.Date;
import java.util.List;

import javax.ejb.EJB;
import javax.ejb.LocalBean;
import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.inject.Inject;
import javax.ws.rs.POST;

import org.hibernate.Query;
import org.json.JSONObject;
//import org.springframework.stereotype.Controller;

import tesis.server.socialNetwork.entity.ComentarioEntity;


//@Controller
@LocalBean
public class ComentarioDao extends GenericDao {
	
	@Inject
	private VoluntarioDao voluntarioDao;

	@Override
	protected Class<ComentarioEntity> getEntityBeanType() {
		return ComentarioEntity.class;
	}
	
	
	@TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
	public void guardar(ComentarioEntity comentarioEntity){
		Date date = new Date();
		comentarioEntity.setFecha(new Timestamp(date.getTime()));
		this.save(comentarioEntity);
	}
	
	
	/**
	 * Metodo que lista todos los comentarios de un post
	 * @param idPostSolicitado
	 * @return
	 */
	public List<ComentarioEntity> listarComentariosDePost(Integer idPostSolicitado){
		String consulta = "from ComentarioEntity c where c.post.idPost = :idPost";
		Query query = this.getSession().createQuery(consulta);
		query.setInteger("idPost", idPostSolicitado);
		List lista = query.list();
		return lista;
	}

	
	/**
	 * Metodo que parsea un comentario en un JSON
	 * @param comentario
	 * @return
	 */
	public JSONObject getJSONFromComment(ComentarioEntity comentario){
		JSONObject retorno = new JSONObject();
		retorno.put("idComentario", comentario.getIdComentario());
		retorno.put("respuesta", comentario.getCuerpoDelComentario());
		retorno.put("autor", voluntarioDao.getJSONFromVoluntario(comentario.getAutor()));
		retorno.put("fechaComentario", comentario.getFecha());
		
		return retorno;
	}
}
