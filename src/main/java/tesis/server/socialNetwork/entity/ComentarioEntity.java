package tesis.server.socialNetwork.entity;

import java.util.Date;
import java.util.List;

import javax.persistence.CascadeType;
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
 * Clase que representa un comentario hecho por un usuario
 * 
 * @author eliana
 *
 */

@Entity
@Table(name="COMENTARIO")
public class ComentarioEntity {

	private Integer idComentario;
	private VoluntarioEntity autor;
	private String cuerpoDelComentario;
	private Date fecha;
	/*
	 * debería estar presente uno u otro
	 */
	private PostEntity post;
	private CampanhaEntity campanha;
	
	//getters y setters
	@Id
	@GeneratedValue(strategy=GenerationType.SEQUENCE, generator="SN_COMENTARIO_SEQ")
    @SequenceGenerator(name="SN_COMENTARIO_SEQ",sequenceName="SN_COMENTARIO_SEQ")
	@Column(name="ID_COMENTARIO", nullable=false)
	public Integer getIdComentario() {
		return idComentario;
	}
	public void setIdComentario(Integer idComentario) {
		this.idComentario = idComentario;
	}
	
	@ManyToOne
	@JoinColumn(name="AUTOR", nullable=false)
	public VoluntarioEntity getAutor() {
		return autor;
	}
	public void setAutor(VoluntarioEntity autor) {
		this.autor = autor;
	}
	
	//no se limita el tamaño del comentario
	@Column(name="CUERPO_MENSAJE", nullable=false)
	public String getCuerpoDelComentario() {
		return cuerpoDelComentario;
	}
	public void setCuerpoDelComentario(String cuerpoDelComentario) {
		this.cuerpoDelComentario = cuerpoDelComentario;
	}
	
	@Column(name="FECHA_COMENTARIO", nullable=false)
	@Temporal(TemporalType.TIMESTAMP)
	public Date getFecha() {
		return fecha;
	}
	public void setFecha(Date fecha) {
		this.fecha = fecha;
	}
	
	@ManyToOne(fetch=FetchType.EAGER, cascade=CascadeType.REMOVE)
	@JoinColumn(name="POST", nullable=true)
	public PostEntity getPost() {
		return post;
	}
	public void setPost(PostEntity post) {
		this.post = post;
	}
	
	@ManyToOne(fetch=FetchType.EAGER, cascade=CascadeType.REMOVE)
	@JoinColumn(name="CAMPANHA", nullable=true)
	public CampanhaEntity getCampanha() {
		return campanha;
	}
	public void setCampanha(CampanhaEntity campanha) {
		this.campanha = campanha;
	}
}
