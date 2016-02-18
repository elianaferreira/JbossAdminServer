package tesis.server.socialNetwork.entity;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

/**
 * Clase que representa una campanha lanzada por el super usuario
 * 
 * @author eliana
 *
 */

@Entity
@Table(name="CAMPANHA")
public class CampanhaEntity {

	private Integer idCampanha;
	private String nombreCampanha;
	private String mensaje;
	private Boolean activa;
	private Date fechaLanzamiento;
	private Date fechaFinalizacion;
	private List<VoluntarioEntity> voluntariosInvitados;
	private List<VoluntarioEntity> voluntariosAdheridos;

	
	//constructor
	public CampanhaEntity() {
		voluntariosInvitados = new ArrayList<VoluntarioEntity>();
		voluntariosAdheridos = new ArrayList<VoluntarioEntity>();
	}
	
	
	//getters y setters
	@Id
	@GeneratedValue(strategy=GenerationType.SEQUENCE, generator="SN_CAMPANHA_SEQ")
    @SequenceGenerator(name="SN_CAMPANHA_SEQ",sequenceName="SN_CAMPANHA_SEQ")
	@Column(name="ID_CAMPANHA", nullable=false)
	public Integer getIdCampanha() {
		return idCampanha;
	}
	public void setIdCampanha(Integer idCampanha) {
		this.idCampanha = idCampanha;
	}
	
	@Column(name="NOMBRE_CAMPANHA", nullable=false)
	public String getNombreCampanha() {
		return nombreCampanha;
	}
	public void setNombreCampanha(String nombreCampanha) {
		this.nombreCampanha = nombreCampanha;
	}
	
	@Column(name="MENSAJE", nullable=false)
	public String getMensaje() {
		return mensaje;
	}
	public void setMensaje(String mensaje) {
		this.mensaje = mensaje;
	}
	
	@Column(name="ACTIVA", nullable=false)
	public Boolean getActiva() {
		return activa;
	}
	public void setActiva(Boolean activa) {
		this.activa = activa;
	}
	
	
	@Column(name="FECHA_LANZAMIENTO", nullable=false)
	@Temporal(TemporalType.DATE)
	public Date getFechaLanzamiento() {
		return fechaLanzamiento;
	}
	public void setFechaLanzamiento(Date fechaLanzamiento) {
		this.fechaLanzamiento = fechaLanzamiento;
	}
	
	
	@Column(name="FECHA_FINALIZACION", nullable=false)
	@Temporal(TemporalType.DATE)
	public Date getFechaFinalizacion() {
		return fechaFinalizacion;
	}


	public void setFechaFinalizacion(Date fechaFinalizacion) {
		this.fechaFinalizacion = fechaFinalizacion;
	}
	
	
	@OneToMany()
	@JoinColumn(name="VOLUNTARIOS_INVITADOS")
	public List<VoluntarioEntity> getVoluntariosInvitados() {
		return voluntariosInvitados;
	}
	public void setVoluntariosInvitados(List<VoluntarioEntity> voluntariosInvitados) {
		this.voluntariosInvitados = voluntariosInvitados;
	}
	
	@OneToMany
	@JoinColumn(name="VOLUNTARIOS_ADHERIDOS")
	public List<VoluntarioEntity> getVoluntariosAdheridos() {
		return voluntariosAdheridos;
	}
	public void setVoluntariosAdheridos(List<VoluntarioEntity> voluntariosAdheridos) {
		this.voluntariosAdheridos = voluntariosAdheridos;
	}
	
}
