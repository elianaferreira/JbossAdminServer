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
 * Clase que representa una fotografia a ser guardada en la base de Datos
 * 
 * @author eliana
 *
 */
@Entity
@Table(name="FOTOGRAFIA")
public class FotografiaEntity {

	private Integer idFotografia;
	private CampanhaEntity campanha;
	private PostEntity post;
	private byte[] bytes;

	//getters y setters
	@Id
	@GeneratedValue(strategy=GenerationType.SEQUENCE, generator="SN_FOTO_SEQ")
    @SequenceGenerator(name="SN_FOTO_SEQ",sequenceName="SN_FOTO_SEQ")
	@Column(name="ID_FOTO", nullable=false)
	public Integer getIdFotografia() {
		return idFotografia;
	}

	public void setIdFotografia(Integer idFotografia) {
		this.idFotografia = idFotografia;
	}
	
	
	/*
	 * puede estar o no en una lista de fotografias para una campanha dada
	 */
	@ManyToOne(fetch=FetchType.EAGER, cascade=CascadeType.REMOVE)
	@JoinColumn(name="CAMPANHA", nullable=true)
	public CampanhaEntity getCampanha() {
		return campanha;
	}

	public void setCampanha(CampanhaEntity campanha) {
		this.campanha = campanha;
	}

	
	@ManyToOne(fetch=FetchType.EAGER, cascade=CascadeType.REMOVE)
	@JoinColumn(name="POST", nullable=true)
	public PostEntity getPost() {
		return post;
	}

	public void setPost(PostEntity post) {
		this.post = post;
	}
	
	
	@Column(name="BYTES", nullable=false)
	public byte[] getBytes() {
		return bytes;
	}

	public void setBytes(byte[] bytes) {
		this.bytes = bytes;
	}
}
