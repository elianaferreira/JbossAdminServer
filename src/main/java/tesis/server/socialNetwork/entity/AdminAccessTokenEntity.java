package tesis.server.socialNetwork.entity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;


@Entity
@Table(name="ADMIN_ACCESS_TOKEN")
public class AdminAccessTokenEntity {
	
	private String currentAccessToken;

	@Id
	@Column(name="CURRENT_ACCESS_TOKEN", nullable=false, unique=true)
	public String getCurrentAccessToken() {
		return currentAccessToken;
	}

	public void setCurrentAccessToken(String currentAccessToken) {
		this.currentAccessToken = currentAccessToken;
	}
	
	

}
