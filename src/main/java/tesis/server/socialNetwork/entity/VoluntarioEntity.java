package tesis.server.socialNetwork.entity;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

/**
 * Clase que representa la entidad para manejo de los voluntarios
 * 
 * @author eliana
 *
 */
@Entity
@Table(name="VOLUNTARIO")
public class VoluntarioEntity {

	//el username va a ser el ID y guardara todo en minuscula
	private String userName;
	//guardara con las mayusculas
	private String usernameString;
	//encriptado
	private String password;
	//en lugar del nombre y apellido se tendra un nombre real solamente
	private String nombreReal;
	private Integer ci;
	private String direccion;
	private String telefono;
	private String email;
	//fecha de creacion seteado por el sistema
	private Date fechaIns;
	/*
	 * es para saber si el usuario ha cerrado sesion o no, solo si es TRUE se aceptaran peticiones del usuario.
	 * es en lugar de tener un accessToken
	 */
	private boolean logged;
	private List<ContactoEntity> contactos;
	private Integer reputacion;
	
	private String categoria;
	
	private Boolean activo;
	
	private String msjAlerta;
	
	private String fotoPerfilLink;
	
	
	//constructor
	public VoluntarioEntity(){
		contactos = new ArrayList<ContactoEntity>();
	}
	
	//getters y setters
	@Id
	@Column(name="USERNAME", unique=true, nullable=false)
	public String getUserName() {
		return userName;
	}
	public void setUserName(String userName) {
		this.userName = userName;
	}
	
	@Column(name="USERNAME_STRING", nullable=false)
	public String getUsernameString() {
		return usernameString;
	}

	public void setUsernameString(String usernameString) {
		this.usernameString = usernameString;
	}
	
	@Column(name="PASSWORD", nullable=false)
	public String getPassword() {
		return password;
	}
	public void setPassword(String password) {
		this.password = password;
	}
	
	@Column(name="NOMBRE", nullable=false)
	public String getNombreReal() {
		return nombreReal;
	}
	public void setNombreReal(String nombreReal) {
		this.nombreReal = nombreReal;
	}

	@Column(name="CI_NRO", nullable=true)
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
	
	@OneToMany
	@JoinColumn(name="CONTACTO")
	public List<ContactoEntity> getContactos() {
		return contactos;
	}
	public void setContactos(List<ContactoEntity> contactos) {
		this.contactos = contactos;
	}

	@Column(name="REPUTACION", columnDefinition="int default 1")
	public Integer getReputacion() {
		return reputacion;
	}

	public void setReputacion(Integer reputacion) {
		this.reputacion = reputacion;
	}

	@Column(name="CATEGORIA")
	public String getCategoria() {
		return categoria;
	}

	public void setCategoria(String categoria) {
		this.categoria = categoria;
	}

	@Column(name="ACTIVO")
	public Boolean getActivo() {
		return activo;
	}

	public void setActivo(Boolean activo) {
		this.activo = activo;
	}

	@Column(name="MSJ_ALERTA")
	public String getMsjAlerta() {
		return msjAlerta;
	}

	public void setMsjAlerta(String msjAlerta) {
		this.msjAlerta = msjAlerta;
	}

	@Column(name="FOTO_PERFIL_LINK", nullable=true)
	public String getFotoPerfilLink() {
		return fotoPerfilLink;
	}

	public void setFotoPerfilLink(String fotoPerfilLink) {
		this.fotoPerfilLink = fotoPerfilLink;
	}	
}
