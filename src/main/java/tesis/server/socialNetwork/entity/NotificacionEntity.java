package tesis.server.socialNetwork.entity;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;


/**
 * Clase que se mapeara a una notificacion 
 * @author eferreira
 *
 */
@Entity
@Table(name="NOTIFICACION")
public class NotificacionEntity {
	
	private Integer idNotificacion;
	//en Utiles se listan los tipos de notificaciones
	private String tipoNotificacion;
	//no puede ser nulo
	private VoluntarioEntity voluntarioTarget;
	private VoluntarioEntity voluntarioCreadorNotificacion;
	private CampanhaEntity campanha;
	private String mensaje;
	private Date fechaCreacionNotificacion;
	private Date fechaVisualizacion;
	private Boolean aceptada;
	private Boolean rechazada;
	
	
	//getters y setters
	@Id
	@GeneratedValue(strategy=GenerationType.SEQUENCE, generator="SN_NOTIF_SEQ")
	@SequenceGenerator(name="SN_NOTIF_SEQ",sequenceName="SN_NOTIF_SEQ")
	@Column(name="ID_NOTIF", nullable=false)
	public Integer getIdNotificacion() {
		return idNotificacion;
	}
	public void setIdNotificacion(Integer idNotificacion) {
		this.idNotificacion = idNotificacion;
	}
	
	@Column(name="TIPO", nullable=false)
	public String getTipoNotificacion() {
		return tipoNotificacion;
	}
	public void setTipoNotificacion(String tipoNotificacion) {
		this.tipoNotificacion = tipoNotificacion;
	}
	
	@ManyToOne(fetch=FetchType.EAGER)
	@JoinColumn(name="VOLUNTARIO_TARGET", nullable=false)
	public VoluntarioEntity getVoluntarioTarget() {
		return voluntarioTarget;
	}
	public void setVoluntarioTarget(VoluntarioEntity voluntarioTarget) {
		this.voluntarioTarget = voluntarioTarget;
	}
	
	@ManyToOne(fetch=FetchType.EAGER)
	@JoinColumn(name="VOLUNTARIO_CREATOR", nullable=true)
	public VoluntarioEntity getVoluntarioCreadorNotificacion() {
		return voluntarioCreadorNotificacion;
	}
	public void setVoluntarioCreadorNotificacion(
			VoluntarioEntity voluntarioCreadorNotificacion) {
		this.voluntarioCreadorNotificacion = voluntarioCreadorNotificacion;
	}
	
	@ManyToOne(fetch=FetchType.EAGER)
	@JoinColumn(name="CAMPANHA", nullable=true)
	public CampanhaEntity getCampanha() {
		return campanha;
	}
	public void setCampanha(CampanhaEntity campanha) {
		this.campanha = campanha;
	}
	
	@Column(name="MENSAJE", nullable=false)
	public String getMensaje() {
		return mensaje;
	}
	public void setMensaje(String mensaje) {
		this.mensaje = mensaje;
	}
	
	@Column(name="FECHA_CREACION", nullable=false, columnDefinition="TIMESTAMP WITHOUT TIME ZONE")
	@Temporal(TemporalType.TIMESTAMP)
	public Date getFechaCreacionNotificacion() {
		return fechaCreacionNotificacion;
	}
	public void setFechaCreacionNotificacion(Date fechaCreacionNotificacion) {
		this.fechaCreacionNotificacion = fechaCreacionNotificacion;
	}
	
	@Column(name="FECHA_VISUALIZACION", nullable=true, columnDefinition="TIMESTAMP WITHOUT TIME ZONE")
	@Temporal(TemporalType.TIMESTAMP)
	public Date getFechaVisualizacion() {
		return fechaVisualizacion;
	}
	public void setFechaVisualizacion(Date fechaVisualizacion) {
		this.fechaVisualizacion = fechaVisualizacion;
	}
	
	@Column(name="ACEPTADA", nullable=true)
	public Boolean getAceptada() {
		return aceptada;
	}
	public void setAceptada(Boolean aceptada) {
		this.aceptada = aceptada;
	}
	
	@Column(name="RECHAZADA", nullable=true)
	public Boolean getRechazada() {
		return rechazada;
	}
	public void setRechazada(Boolean rechazada) {
		this.rechazada = rechazada;
	}
	

}
