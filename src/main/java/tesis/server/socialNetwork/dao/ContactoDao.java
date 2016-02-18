package tesis.server.socialNetwork.dao;

import java.util.List;

import javax.ejb.LocalBean;
import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;

import org.hibernate.Query;
import org.json.JSONObject;
//import org.springframework.stereotype.Controller;

import tesis.server.socialNetwork.entity.ContactoEntity;
import tesis.server.socialNetwork.entity.VoluntarioEntity;

//@Controller
@LocalBean
public class ContactoDao extends GenericDao<ContactoEntity, Integer> {

	@Override
	protected Class<ContactoEntity> getEntityBeanType() {
		return ContactoEntity.class;
	}
	
	
	@TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
	public void guardar(ContactoEntity contactoEntity){
		this.save(contactoEntity);
	}
	
	
	/**
	 * Metodo que trae los entities de contacto correspondientes a un voluntario
	 * @param voluntario
	 * @return
	 */
	public List<ContactoEntity> getListaContactsEntity(VoluntarioEntity voluntario){
		String consulta = "from ContactoEntity c "
				+ "where c.voluntario = :voluntario1 "
				+ "or c.contacto = :voluntario2";
		Query query = getSession().createQuery(consulta);
		query.setEntity("voluntario1", voluntario);
		query.setEntity("voluntario2", voluntario);
		List lista = query.list();
		return lista;
	}
	
	
	/**
	 * Metodo que retorna la cantidad de contactos de un voluntario.
	 * el getContats del entity de voluntario no funciona cuando el usuario no es el owner de la relacion
	 * @param voluntario
	 * @return
	 */
	public Integer getCantidadContactos(VoluntarioEntity voluntario){
		String consulta = "select count(*) from ContactoEntity c "
				+ "where c.voluntario = :voluntario1 "
				+ "or c.contacto = :voluntario2";
		Query query = getSession().createQuery(consulta);
		query.setEntity("voluntario1", voluntario);
		query.setEntity("voluntario2", voluntario);
		Long cantidadLong = (Long) query.uniqueResult();
		Integer cantidad = cantidadLong.intValue();
		return cantidad;
	}
	
	
	
	/**
	 * Metodo que lista completa de contactos
	 * 
	 * @return
	 */
	public List<ContactoEntity> listarTodosLosContactos(){
		String consulta = "from ContactoEntity c ";
		Query query = getSession().createQuery(consulta);
		List lista = query.list();
		return lista;
	}
	
	
	
	/**
	 * Metodo que retorna la entidad contacto que representa la amistad entre dos voluntarios
	 * @param v1
	 * @param v2
	 * @return
	 */
	public ContactoEntity getContact(VoluntarioEntity v1, VoluntarioEntity v2){
		String consulta = "from ContactoEntity ce where "
				+ "(ce.contacto = :voluntario1 and ce.voluntario = :voluntario2 ) or "
				+ "(ce.voluntario = :voluntario1 and ce.contacto = :voluntario2)";
		Query query = getSession().createQuery(consulta);
		query.setEntity("voluntario1", v1);
		query.setEntity("voluntario2", v2);
		ContactoEntity contacto = (ContactoEntity) query.uniqueResult();
		return contacto;
	}
	
	
	
	/**
	 * Metodo que retorna la lista de entidades VoluntarioEntity que son contactos de un voluntario dado.
	 * 
	 * @param voluntario
	 * @return
	 */
	public List<VoluntarioEntity> getListVolunteersContactsOrderByRanking(VoluntarioEntity voluntario){
		
		/*
		 * select * from voluntario v where v.username in (select case 
				when c.contacto = 'eferreira' then c.voluntario
				when c.voluntario = 'eferreira' then c.contacto 
				else null end as columna from contactos c) order by v.reputacion desc
		 * 
		 * */
		String consulta = "from VoluntarioEntity v where v.userName in ("
				+ "select case "
				+ "when c.contacto = :voluntario then c.voluntario.userName "
				+ "when c.voluntario = :voluntario then c.contacto.userName "
				+ "else null "
				+ "end as columna from ContactoEntity c) order by v.reputacion desc";
		Query query = this.getSession().createQuery(consulta);
		query.setEntity("voluntario", voluntario);
		List lista = query.list();
		return lista;
	}
	
	
	/**
	 * Metodo que retorna los amigos de mis amigos (incluyendome)
	 * 
	 * @param voluntario
	 * @return
	 */
	public List<VoluntarioEntity> getListOfFriendOfFriend(VoluntarioEntity voluntario){
		/*
		 * select * from voluntario v where v.username in (
				select case when c3.contacto in (select case 
					when c.contacto = 'susana' then c.voluntario
					when c.voluntario = 'susana' then c.contacto 
					else null end from contactos c) then c3.voluntario
			
				when c3.voluntario in (select case 
					when c2.contacto = 'susana' then c2.voluntario
					when c2.voluntario = 'susana' then c2.contacto 
					else null end from contactos c2) then c3.contacto
				else null end from contactos c3) order by v.reputacion desc
		 * */
		String consulta = ""
				+ "from VoluntarioEntity v where v in ("
					+ "select case "
						+ "when c3.contacto in (select case "
							+ "when c.contacto = :voluntario then c.voluntario "
							+ "when c.voluntario = :voluntario then c.contacto "
							+ "else null end from ContactoEntity c) then c3.voluntario "
						+ "when c3.voluntario in (select case "
							+ "when c2.contacto = :voluntario then c2.voluntario "
							+ "when c2.voluntario = :voluntario then c2.contacto "
							+ "else null end from ContactoEntity c2) then c3.contacto "
						+ "else null end from ContactoEntity c3) order by v.reputacion desc";
		Query query = this.getSession().createQuery(consulta);
		query.setEntity("voluntario", voluntario);
		query.setMaxResults(10);
		List lista = query.list();
		return lista;
	}
}
