package tesis.server.socialNetwork.entity;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;


/**
 * Clase que guarda la referencia a una amistad hecha.
 * Esta relacion es bidireccional, por lo que es necesario guardar una sola vez y sera tomada para ambos usuarios.
 * 
 * @author eFerreira
 *
 */
@Entity
@Table(name="CONTACTOS")
public class ContactoEntity {
	
	private Integer idAmistad;
	private VoluntarioEntity voluntario;
	private VoluntarioEntity contacto;
	
	
	
	//getters y setters
	
	@Id
	@GeneratedValue(strategy=GenerationType.SEQUENCE, generator="SN_AMISTAD_SEQ")
    @SequenceGenerator(name="SN_AMISTAD_SEQ",sequenceName="SN_AMISTAD_SEQ")
	@Column(name="ID_AMISTAD", nullable=false)
	public Integer getIdAmistad() {
		return idAmistad;
	}
	public void setIdAmistad(Integer idAmistad) {
		this.idAmistad = idAmistad;
	}
	
	
	@ManyToOne
	@JoinColumn(name="VOLUNTARIO")
	public VoluntarioEntity getVoluntario() {
		return voluntario;
	}
	public void setVoluntario(VoluntarioEntity voluntario) {
		this.voluntario = voluntario;
	}
	
	@ManyToOne
	@JoinColumn(name="CONTACTO")
	public VoluntarioEntity getContacto() {
		return contacto;
	}
	public void setContacto(VoluntarioEntity contacto) {
		this.contacto = contacto;
	}
	
}
