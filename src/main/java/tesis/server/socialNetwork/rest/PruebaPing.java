package tesis.server.socialNetwork.rest;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;


@Path("/ping")
public class PruebaPing {

	@GET
	@Produces(value = "application/json")
	public String ping(){
		return "{'ping': 'pong'}";
	}
}
