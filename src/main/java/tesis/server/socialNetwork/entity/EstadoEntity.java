package tesis.server.socialNetwork.entity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

/**
 * Clase que representa los posibles estados de una campaña
 * 
 * @author eliana
 *
 */

@Entity
@Table(name="ESTADO")
public class EstadoEntity {

	/**
	 * valores:
	 * EST01
	 * EST02
	 */
	private String codEstado;
	/**
	 * valores:
	 * Iniciada
	 * Finalizada
	 */
	private String estado;
	/**
	 * posibles valores:
	 * cuando la campanha es lanzada.
	 * cuando la campaña ha finalizado y no se pueden realizar cambios en la misma.
	 * 
	 */
	private String descripcion;
	
	//getters y setters
	@Id
	@Column(name="COD_ESTADO", nullable=false, unique=true)
	public String getCodEstado() {
		return codEstado;
	}
	public void setCodEstado(String codEstado) {
		this.codEstado = codEstado;
	}
	
	@Column(name="ESTADO", nullable=false, unique=true)
	public String getEstado() {
		return estado;
	}
	public void setEstado(String estado) {
		this.estado = estado;
	}
	
	@Column(name="DESCRIPCION", nullable=false)
	public String getDescripcion() {
		return descripcion;
	}
	public void setDescripcion(String descripcion) {
		this.descripcion = descripcion;
	}
}
