package tesis.server.socialNetwork.entity;

import javax.persistence.CascadeType;
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


/**
 * Clase que representa una marcación de no favorito o mal, ya sea de un post
 * o de una campanha
 * 
 * @author eFerreira
 *
 */

@Entity
@Table(name="NO_FAVORITO")
public class NoFavoritoEntity {

	private Integer idNoFavorito;
	private VoluntarioEntity autor;
	
	//no se puede marcar como fav un comentario, al estilo Instagram
	//private ComentarioEntity comentario;
	private PostEntity post;
	private CampanhaEntity campanha;
	
	
	//getters y setters
	@Id
	@GeneratedValue(strategy=GenerationType.SEQUENCE, generator="SN_NO_FAVORITO_SEQ")
    @SequenceGenerator(name="SN_NO_FAVORITO_SEQ",sequenceName="SN_NO_FAVORITO_SEQ")
	@Column(name="ID_NO_FAVORITO", nullable=false)
	public Integer getIdNoFavorito() {
		return idNoFavorito;
	}
	public void setIdNoFavorito(Integer idNoFavorito) {
		this.idNoFavorito = idNoFavorito;
	}
	
	@ManyToOne(optional=false)
	@JoinColumn(name="AUTOR", nullable=false)
	public VoluntarioEntity getAutor() {
		return autor;
	}
	public void setAutor(VoluntarioEntity autor) {
		this.autor = autor;
	}
	
	@ManyToOne()
	@JoinColumn(name="POST", nullable=true)
	public PostEntity getPost() {
		return post;
	}
	public void setPost(PostEntity post) {
		this.post = post;
	}
	
	@ManyToOne()
	@JoinColumn(name="CAMPANHA", nullable=true)
	public CampanhaEntity getCampanha() {
		return campanha;
	}
	public void setCampanha(CampanhaEntity campanha) {
		this.campanha = campanha;
	}
}
