package tesis.server.socialNetwork.entity;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

/**
 * Clase que representa al administrador del servidor web
 * 
 * @author eliana
 *
 */

@Entity
@Table(name="ADMINISTRADOR")
public class AdminEntity {

	private Integer idAdministrador;
	private String adminName; //no es userName
	private String password;
	private String nombre;
	private String apellido;
	private Integer ci;
	private String direccion;
	private String telefono;
	private String email;
	private Date fechaIns;
	private boolean logged;
	private Boolean eliminado;
	
	//getters y setters
	@Id
	@GeneratedValue(strategy=GenerationType.SEQUENCE, generator="SN_ADMIN_SEQ")
    @SequenceGenerator(name="SN_ADMIN_SEQ",sequenceName="SN_ADMIN_SEQ")
	@Column(name="ID_ADMIN", nullable=false)
	public Integer getIdAdministrador() {
		return idAdministrador;
	}
	public void setIdAdministrador(Integer idAdministrador) {
		this.idAdministrador = idAdministrador;
	}
	
	@Column(name="ADMIN_NAME", nullable=false, unique=true)
	public String getAdminName() {
		return adminName;
	}
	public void setAdminName(String adminName) {
		this.adminName = adminName;
	}
	
	@Column(name="PASSWORD", nullable=false)
	public String getPassword() {
		return password;
	}
	public void setPassword(String password) {
		this.password = password;
	}
	@Column(name="NOMBRE", nullable=false)
	public String getNombre() {
		return nombre;
	}
	public void setNombre(String nombre) {
		this.nombre = nombre;
	}
	
	@Column(name="APELLIDO", nullable=false)
	public String getApellido() {
		return apellido;
	}
	public void setApellido(String apellido) {
		this.apellido = apellido;
	}
	
	//se debe tener una identificacion del administrador
	@Column(name="CI_NRO", nullable=false)
	public Integer getCi() {
		return ci;
	}
	public void setCi(Integer ci) {
		this.ci = ci;
	}
	
	@Column(name="DIRECCION", nullable=true)
	public String getDireccion() {
		return direccion;
	}
	public void setDireccion(String direccion) {
		this.direccion = direccion;
	}
	
	@Column(name="TELEFONO", nullable=true)
	public String getTelefono() {
		return telefono;
	}
	public void setTelefono(String telefono) {
		this.telefono = telefono;
	}
	
	@Column(name="EMAIL", nullable=true)
	public String getEmail() {
		return email;
	}
	public void setEmail(String email) {
		this.email = email;
	}
	
	@Column(name="FECHA_INS", nullable=false)
	@Temporal(TemporalType.DATE)
	public Date getFechaIns() {
		return fechaIns;
	}
	public void setFechaIns(Date fechaIns) {
		this.fechaIns = fechaIns;
	}
	
	@Column(name="LOGGED", nullable=false, columnDefinition="boolean default false")
	public boolean getLogged() {
		return logged;
	}
	public void setLogged(boolean logged) {
		this.logged = logged;
	}
	
	@Column(name="ELIMINADO", nullable=false, columnDefinition="boolean default false")
	public Boolean getEliminado() {
		return eliminado;
	}
	public void setEliminado(Boolean eliminado) {
		this.eliminado = eliminado;
	}
}
