package tesis.server.socialNetwork.rest;

import java.util.HashSet;
import java.util.Set;

import javax.ws.rs.ApplicationPath;
import javax.ws.rs.core.Application;


@ApplicationPath("/api/ws")
public class RestInitializer extends Application{

	/*public Set<Class<?>> getClasses() {
        Set<Class<?>> s = new HashSet<Class<?>>();
        s.add(VoluntarioWS.class);
        s.add(PostWS.class);
        s.add(AdministradorWS.class);
        return s;
    }*/
}
