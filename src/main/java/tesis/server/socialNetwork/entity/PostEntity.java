package tesis.server.socialNetwork.entity;

import java.util.Date;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
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
 * Clase que representa la entidad para manejo de un Post 
 * creado por un usuario/voluntario
 * 
 * @author eliana
 *
 */

@Entity
@Table(name="POST")
public class PostEntity {
	
	private Integer idPost;
	private String post;
	private VoluntarioEntity voluntario;
	private Date fechaPost;
	//datos de geolocalizacion
	private Double latitud;
	private Double longitud;
	//fotos guardadas como un array de bytes
	//private byte[] fotoAntes;
	//private byte[] fotoDespues;
	private List<ComentarioEntity> comentarios;
	private List<FavoritoEntity> likeList;
	private List<NoFavoritoEntity> noLikeList;
	//indica si un caso reportado ha sido solucionado.
	private Boolean solucionado;
	private Boolean relevante;
	//la persona que lo soluciona puede no ser el mismo autor
	private VoluntarioEntity voluntarioQueSoluciona;
	private Date fechaSolucion;
	
	private Integer rankingEstado;
	
	private String quienDebeSolucionar;
	
	private Boolean cerradoPorAdministrador;
	private AdminEntity administradorQueCerro;
	private Date fechaCerrado;
	
	private String fotoAntesLink;
	private String fotoDespuesLink;
	
	
	//getters y setters
	@Id
	@GeneratedValue(strategy=GenerationType.SEQUENCE, generator="SN_POST_SEQ")
    @SequenceGenerator(name="SN_POST_SEQ",sequenceName="SN_POST_SEQ")
	@Column(name="ID_POST", nullable=false)
	public Integer getIdPost() {
		return idPost;
	}
	public void setIdPost(Integer idPost) {
		this.idPost = idPost;
	}
	
	@Column(name="POST", nullable=false)
	public String getPost() {
		return post;
	}
	public void setPost(String post) {
		this.post = post;
	}
	
	@ManyToOne(fetch=FetchType.EAGER)
	@JoinColumn(name="VOLUNTARIO", nullable=false)
	public VoluntarioEntity getVoluntario() {
		return voluntario;
	}
	public void setVoluntario(VoluntarioEntity voluntario) {
		this.voluntario = voluntario;
	}
	
	@Column(name="FECHA_POST", nullable=false, columnDefinition="TIMESTAMP WITHOUT TIME ZONE")
	@Temporal(TemporalType.TIMESTAMP)
	public Date getFechaPost() {
		return fechaPost;
	}
	public void setFechaPost(Date fechaPost) {
		this.fechaPost = fechaPost;
	}
	
	@Column(name="SOLUCIONADO", nullable=false)
	public Boolean getSolucionado() {
		return solucionado;
	}
	public void setSolucionado(Boolean solucionado) {
		this.solucionado = solucionado;
	}
	
	@Column(name="LATITUD", nullable=true)
	public Double getLatitud() {
		return latitud;
	}
	public void setLatitud(Double latitud) {
		this.latitud = latitud;
	}
	
	@Column(name="LONGITUD", nullable=true)
	public Double getLongitud() {
		return longitud;
	}
	public void setLongitud(Double longitud) {
		this.longitud = longitud;
	}
	
	/*@Column(name="FOTO_ANTES_BYTES", nullable=true)
	public byte[] getFotoAntes() {
		return fotoAntes;
	}
	public void setFotoAntes(byte[] fotoAntes) {
		this.fotoAntes = fotoAntes;
	}
	
	@Column(name="FOTO_DESPUES_BYTES", nullable=true)
	public byte[] getFotoDespues() {
		return fotoDespues;
	}
	public void setFotoDespues(byte[] fotoDespues) {
		this.fotoDespues = fotoDespues;
	}*/
	
	@OneToMany
	@JoinColumn(name="COMENTARIO")
	public List<ComentarioEntity> getComentarios() {
		return comentarios;
	}
	public void setComentarios(List<ComentarioEntity> comentarios) {
		this.comentarios = comentarios;
	}
	
	@OneToMany
	@JoinColumn(name="FAV")
	public List<FavoritoEntity> getLikeList() {
		return likeList;
	}
	public void setLikeList(List<FavoritoEntity> likeList) {
		this.likeList = likeList;
	}
	
	@OneToMany
	@JoinColumn(name="NO_FAV")
	public List<NoFavoritoEntity> getNoLikeList() {
		return noLikeList;
	}
	public void setNoLikeList(List<NoFavoritoEntity> noLikeList) {
		this.noLikeList = noLikeList;
	}
	
	@Column(name="RELEVANTE", columnDefinition="boolean default false")
	public Boolean getRelevante() {
		return relevante;
	}
	public void setRelevante(Boolean relevante) {
		this.relevante = relevante;
	}
	
	@ManyToOne(fetch=FetchType.EAGER)
	@JoinColumn(name="VOLUNTARIO_SOLUCIONA", nullable=true)
	public VoluntarioEntity getVoluntarioQueSoluciona() {
		return voluntarioQueSoluciona;
	}
	public void setVoluntarioQueSoluciona(VoluntarioEntity voluntarioQueSoluciona) {
		this.voluntarioQueSoluciona = voluntarioQueSoluciona;
	}
	
	@Column(name="FECHA_SOLUCION", nullable=true, columnDefinition="TIMESTAMP WITHOUT TIME ZONE")
	@Temporal(TemporalType.TIMESTAMP)
	public Date getFechaSolucion() {
		return fechaSolucion;
	}
	public void setFechaSolucion(Date fechaSolucion) {
		this.fechaSolucion = fechaSolucion;
	}
	
	
	//solo puede tener valores 1, 2 o 3
	//por defecto el valor sera de 1
	@Column(name="RANKING_ESTADO", nullable=true)
	public Integer getRankingEstado() {
		return rankingEstado;
	}
	public void setRankingEstado(Integer rankingEstado) {
		this.rankingEstado = rankingEstado;
	}
	
	@Column(name="QUIEN_DEBE_SOLUCIONAR", nullable=true)
	public String getQuienDebeSolucionar() {
		return quienDebeSolucionar;
	}
	public void setQuienDebeSolucionar(String quienDebeSolucionar) {
		this.quienDebeSolucionar = quienDebeSolucionar;
	}
	
	
	@Column(name="CERRADO", nullable=true)
	public Boolean getCerradoPorAdministrador() {
		return cerradoPorAdministrador;
	}
	public void setCerradoPorAdministrador(Boolean cerradoPorAdministrador) {
		this.cerradoPorAdministrador = cerradoPorAdministrador;
	}
	
	@ManyToOne(fetch=FetchType.EAGER)
	@JoinColumn(name="ADMINISTRADOR_QUE_CERRO", nullable=true)
	public AdminEntity getAdministradorQueCerro() {
		return administradorQueCerro;
	}
	public void setAdministradorQueCerro(AdminEntity administradorQueCerro) {
		this.administradorQueCerro = administradorQueCerro;
	}
	
	@Column(name="FECHA_CERRADO", nullable=true, columnDefinition="TIMESTAMP WITHOUT TIME ZONE")
	@Temporal(TemporalType.TIMESTAMP)
	public Date getFechaCerrado() {
		return fechaCerrado;
	}
	public void setFechaCerrado(Date fechaCerrado) {
		this.fechaCerrado = fechaCerrado;
	}
	
	
	@Column(name="LINK_FOTO_ANTES", nullable=true)
	public String getFotoAntesLink() {
		return fotoAntesLink;
	}
	public void setFotoAntesLink(String fotoAntesLink) {
		this.fotoAntesLink = fotoAntesLink;
	}
	
	@Column(name="LINK_FOTO_DESPUES", nullable=true)
	public String getFotoDespuesLink() {
		return fotoDespuesLink;
	}
	public void setFotoDespuesLink(String fotoDespuesLink) {
		this.fotoDespuesLink = fotoDespuesLink;
	}
	
	
	
}
