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
 * Clase que representa una solicitud de amistad enviada
 * 
 * @author eFerreira
 *
 */

@Entity
@Table(name="SOLICITUD_AMISTAD")
public class SolicitudAmistadEntity {

	
	private Integer idSolicitudAmistad;
	private VoluntarioEntity usuarioSolicitante;
	private VoluntarioEntity usuarioSolicitado;
	/**
	 * TRUE: 'usuarioSolicitado' ha aceptado la solicitud de amistad.
	 * FALSE: 'usuarioSolicitado' ha rechazado la solicitud de amista.
	 * Para el caso en el que 'usuarioSolicitado' o 'usuarioSolicitante' elimine la solicitud de amistad,
	 * esta se eliminara de al Base de Datos.
	 */
	private Boolean aceptada;
	
	
	//getters y setters
	
	@Id
	@GeneratedValue(strategy=GenerationType.SEQUENCE, generator="SN_SOLICITUD_SEQ")
    @SequenceGenerator(name="SN_SOLICITUD_SEQ",sequenceName="SN_SOLICITUD_SEQ")
	@Column(name="ID_SOLICITUD", nullable=false)
	public Integer getIdSolicitudAmistad() {
		return idSolicitudAmistad;
	}
	public void setIdSolicitudAmistad(Integer idSolicitudAmistad) {
		this.idSolicitudAmistad = idSolicitudAmistad;
	}
	
	@ManyToOne()
	@JoinColumn(name="USUARIO_SOLICITANTE")
	public VoluntarioEntity getUsuarioSolicitante() {
		return usuarioSolicitante;
	}
	public void setUsuarioSolicitante(VoluntarioEntity usuarioSolicitante) {
		this.usuarioSolicitante = usuarioSolicitante;
	}
	
	@ManyToOne()
	@JoinColumn(name="USUARIO_SOLICITADO")
	public VoluntarioEntity getUsuarioSolicitado() {
		return usuarioSolicitado;
	}
	public void setUsuarioSolicitado(VoluntarioEntity usuarioSolicitado) {
		this.usuarioSolicitado = usuarioSolicitado;
	}
	
	@Column(name="ACEPTADA", nullable=false)
	public Boolean getAceptada() {
		return aceptada;
	}
	public void setAceptada(Boolean aceptada) {
		this.aceptada = aceptada;
	}
	
	
}
