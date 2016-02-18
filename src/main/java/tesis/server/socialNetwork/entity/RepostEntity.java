package tesis.server.socialNetwork.entity;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;



@Entity
@Table(name="RE_POST")
public class RepostEntity {
	
	private Integer idRepost;
	private PostEntity post;
	private VoluntarioEntity autorRepost;
	private Date fechaRepost;
	
	
	//getters y setters
	@Id
	@GeneratedValue(strategy=GenerationType.SEQUENCE, generator="SN_REPOST_SEQ")
    @SequenceGenerator(name="SN_REPOST_SEQ",sequenceName="SN_REPOST_SEQ")
	@Column(name="ID_REPOST", nullable=false)
	public Integer getIdRepost() {
		return idRepost;
	}
	public void setIdRepost(Integer idRepost) {
		this.idRepost = idRepost;
	}
	
	@ManyToOne
	@JoinColumn(name="POST", nullable=false)
	public PostEntity getPost() {
		return post;
	}
	public void setPost(PostEntity post) {
		this.post = post;
	}
	
	@ManyToOne
	@JoinColumn(name="AUTOR_REPOST", nullable=false)
	public VoluntarioEntity getAutorRepost() {
		return autorRepost;
	}
	public void setAutorRepost(VoluntarioEntity autorRepost) {
		this.autorRepost = autorRepost;
	}
	
	@Column(name="FECHA_REPOST", nullable=false, columnDefinition="TIMESTAMP WITH TIME ZONE")
	@Temporal(TemporalType.TIMESTAMP)
	public Date getFechaRepost() {
		return fechaRepost;
	}
	public void setFechaRepost(Date fechaRepost) {
		this.fechaRepost = fechaRepost;
	}
	
	

}
