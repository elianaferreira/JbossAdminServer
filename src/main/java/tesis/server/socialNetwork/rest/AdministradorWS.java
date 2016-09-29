package tesis.server.socialNetwork.rest;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import javax.ejb.Stateless;
import javax.imageio.ImageIO;
import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;

import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.web.bind.annotation.ResponseBody;

import tesis.server.socialNetwork.dao.AdminAccessTokenDao;
import tesis.server.socialNetwork.dao.AdministradorDao;
import tesis.server.socialNetwork.dao.CampanhaDao;
import tesis.server.socialNetwork.dao.ContactoDao;
import tesis.server.socialNetwork.dao.PostDao;
import tesis.server.socialNetwork.dao.RepostDao;
import tesis.server.socialNetwork.dao.VoluntarioDao;
import tesis.server.socialNetwork.entity.AdminAccessTokenEntity;
import tesis.server.socialNetwork.entity.AdminEntity;
import tesis.server.socialNetwork.entity.CampanhaEntity;
import tesis.server.socialNetwork.entity.ContactoEntity;
import tesis.server.socialNetwork.entity.PostEntity;
import tesis.server.socialNetwork.entity.RepostEntity;
import tesis.server.socialNetwork.entity.VoluntarioEntity;
import tesis.server.socialNetwork.utils.Base64;
import tesis.server.socialNetwork.utils.SortedByDate;
import tesis.server.socialNetwork.utils.Utiles;


@Stateless
@Path("/admin")
public class AdministradorWS {
	
	@Inject
	private AdministradorDao administradorDao;
	
	@Inject
	private AdminAccessTokenDao adminAccessTokenDao;
	
	@Inject
	private VoluntarioDao voluntarioDao;
	
	@Inject
	private PostDao postDao;
	
	@Inject
	private RepostDao repostDao;
	
	@Inject
	private ContactoDao contactoDao;
	
	@Inject
	private CampanhaDao campanhaDao;
	
	
	
	@POST
	@Path("/auth")
	@Consumes("application/x-www-form-urlencoded")
	@ResponseBody
	public String adminAuth(@FormParam("name") String adminName,
							@FormParam("password") String password){
				
		JSONObject retorno = administradorDao.iniciarSesionAdmin(adminName, password);
		if(retorno.has("error")){
			return Utiles.retornarSalida(true, retorno.getString("error"));
		} else {
			return Utiles.retornarSalida(false, retorno.toString());
		}
	}
	
	
	@POST
	@Path("/logout")
	@Consumes("application/x-www-form-urlencoded")
	@ResponseBody
	public String adminLogout(@FormParam("name") String adminName,
							@FormParam("accessToken") String accessToken){
		
		//verificamos si el administrador existe
		AdminEntity administrador = administradorDao.verificarAdministrador(adminName, accessToken);
		if(administrador == null){
			return Utiles.retornarSalida(true, "El nombre o la contrase\u00f1a son inv\u00e1lidos");
		} else {
			//iniciamos sesion para el administrador
			if(administradorDao.cerrarSesionAdmin(administrador)){
				return Utiles.retornarSalida(false, "Sesi\u00f3n cerrada.");
			} else {
				return Utiles.retornarSalida(true, "Error al cerrar la sesi\u00f3n");
			}
		}
	}
	
	
	
	@POST
	@Path("/volunteers")
	@Consumes("application/x-www-form-urlencoded")
	@ResponseBody
	public String addVoluntiersACategory(@FormParam("admin") String adminName,
										 @FormParam("accessToken") String accessToken,
										 @FormParam("voluntarios") String voluntarios){
		
		AdminEntity admin = administradorDao.verificarAdministrador(adminName, accessToken);
		if(admin == null){
			return Utiles.retornarSalida(true, "El nombre o la contrase\u00f1a son inv\u00e1lidos.");
		} else {
			//array de JSON
			JSONArray retornoNoAgregados = new JSONArray();
			
			//voluntarios es un array de JSON con los voluntarios a ser agregado con categoria A
			JSONArray arrayTemp = new JSONArray(voluntarios);
			for(int i=0; i<arrayTemp.length(); i++){
				JSONObject jsonVoluntario = arrayTemp.getJSONObject(i);
				if(voluntarioDao.findByClassAndID(VoluntarioEntity.class, jsonVoluntario.getString("username").toLowerCase()) != null){
					retornoNoAgregados.put(jsonVoluntario);
				} else{
					try{
						VoluntarioEntity voluntario = new VoluntarioEntity();
						voluntario.setUserName(jsonVoluntario.getString("username").toLowerCase());
						voluntario.setPassword(jsonVoluntario.getString("password"));
						voluntario.setUsernameString(jsonVoluntario.getString("username"));
						voluntario.setNombreReal(jsonVoluntario.getString("nombre"));
						voluntario.setCi(jsonVoluntario.getInt("ci"));
						voluntario.setDireccion(jsonVoluntario.getString("direccion"));
						voluntario.setTelefono(jsonVoluntario.getString("telefono"));
						voluntario.setEmail(jsonVoluntario.getString("email"));
						voluntario.setCategoria("A");
						voluntarioDao.guardar(voluntario);
					} catch(Exception ex){
						retornoNoAgregados.put(jsonVoluntario);
					}
				}
			}
			return Utiles.retornarSalida(false, retornoNoAgregados.toString());
		}
	}
	
	
	/**
	 * Metodo que se encarga de buscar los voluntarios de acuerdo a un criterio
	 * @param adminName
	 * @param password
	 * @param criterioBusqueda
	 * @return
	 */
	@GET
	@Path("/search")
	@ResponseBody
	public String searchVoluntiers(@QueryParam("admin") String adminName,
			 @QueryParam("accessToken") String accessToken,
			 @QueryParam("criterio") String criterioBusqueda){
		
		AdminEntity admin = administradorDao.verificarAdministrador(adminName, accessToken);
		if(admin == null){
			return Utiles.retornarSalida(true, "El nombre o la contrase\u00f1a son inv\u00e1lidos");
		} else {
			//llamamos al dao que se encarga de la busqueda
			List<VoluntarioEntity> listaResultado = voluntarioDao.buscarUsuarios(criterioBusqueda);
			if(listaResultado == null){
				return Utiles.retornarSalida(true, "No hay usuario con ese nombre");
			} else {
				JSONArray retorno = new JSONArray();
				//a cada usuario le agregamos la cantidad de amigos que tiene y un boolean de si son amigos
				for(int j=0; j<listaResultado.size(); j++){
					JSONObject jsonFromVoluntario = voluntarioDao.getJSONFromVoluntario(listaResultado.get(j));
					retorno.put(jsonFromVoluntario);
				}
				return Utiles.retornarSalida(false, retorno.toString());
			}
		}
	}
	
	@GET
	@Path("/reports")
	@ResponseBody
	public String getReportsOfVoluntiers(@QueryParam("admin") String adminName,
			 @QueryParam("accessToken") String accessToken,
			 @QueryParam("username") String username,
			 @QueryParam("ultimaActualizacion") String ultimaActualizacionString){
		
		AdminEntity admin = administradorDao.verificarAdministrador(adminName, accessToken);
		if(admin == null){
			return Utiles.retornarSalida(true, "El nombre o la contrase\u00f1a son inv\u00e1lidos");
		} else {
			VoluntarioEntity voluntario = voluntarioDao.findByClassAndID(VoluntarioEntity.class, username);
			if(voluntario == null){
				return Utiles.retornarSalida(true, "El usuario no existe");
			} else {
				List<JSONObject> arrayRetorno = new ArrayList<JSONObject>();
				List<PostEntity> posts = postDao.getHomeTimeline(voluntario);
				for(int i=0; i<posts.size(); i++){
					JSONObject postJSON = postDao.getJSONFromPost(username, posts.get(i));
					arrayRetorno.add(postJSON);
				}
				try{
					/*Date date = new Date();
					SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss.SSS");
					String formattedDate = sdf.format(date);
					Timestamp timestamp;
				    Date parsedDate = sdf.parse(formattedDate);
				    timestamp = new java.sql.Timestamp(parsedDate.getTime());*/
					
					Timestamp timestamp;
					SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss.SSS");
					Date parsedDate = dateFormat.parse(ultimaActualizacionString);
					timestamp = new java.sql.Timestamp(parsedDate.getTime());
					List<RepostEntity> reposts = repostDao.getOwnReposts(username, timestamp, false);
					for(int j=0; j<reposts.size(); j++){
						JSONObject repostJSON = repostDao.getJSONFromRepost(reposts.get(j), username);
						arrayRetorno.add(repostJSON);
					}
				} catch(Exception e){
					e.printStackTrace();
				}
				
				Collections.sort(arrayRetorno, new SortedByDate());
				return Utiles.retornarSalida(false, arrayRetorno.toString());
			}
		}
	}
	
	
	/**
	 * Metodo que retorna la lista completa de posts pero solo los datos relevantes para el mapa
	 * 
	 * @param adminName
	 * @param password
	 * @return
	 */
	@GET
	@Path("/allPosts")
	@ResponseBody
	public String getAllPosts(@QueryParam("admin") String adminName,
								@QueryParam("accessToken") String accessToken){
		
		AdminEntity admin = administradorDao.verificarAdministrador(adminName, accessToken);
		if(admin == null){
			return Utiles.retornarSalida(true, "El nombre o la contrase\u00f1a son inv\u00e1lidos");
		} else {
			List<PostEntity> todosLosPosts = postDao.getAll();
			//vamos a enviar solo el nombre real, el id del post, la ubicacion y si ya fue solucionado
			JSONArray retorno = new JSONArray();
			for(PostEntity p: todosLosPosts){
				JSONObject postJSON = new JSONObject();
				postJSON.put("id", p.getIdPost());
				postJSON.put("solucionado", p.getSolucionado());
				postJSON.put("nombre", p.getVoluntario().getNombreReal());
				postJSON.put("latitud", p.getLatitud());
				postJSON.put("longitud", p.getLongitud());
				postJSON.put("ranking", p.getRankingEstado());
				retorno.put(postJSON);
			}
			return Utiles.retornarSalida(false, retorno.toString());
		}
	}
	
	
	
	/**
	 * Metodo que retorna los datos de un post especifico en base a si ID
	 * 
	 * @param adminName
	 * @param password
	 * @param idPost
	 * @return
	 */
	@GET
	@Path("/post")
	@ResponseBody
	public String getPost(@QueryParam("admin") String adminName,
							@QueryParam("accessToken") String accessToken,
							@QueryParam("idPost") Integer idPost){
		
		AdminEntity admin = administradorDao.verificarAdministrador(adminName, accessToken);
		if(admin == null){
			return Utiles.retornarSalida(true, "El nombre o la contrase\u00f1a son inv\u00e1lidos");
		} else {
			PostEntity postSolicitado = postDao.findByClassAndID(PostEntity.class, idPost);
			if(postSolicitado == null){
				return Utiles.retornarSalida(true, "El reporte no existe");
			} else {
				JSONObject postJSON = postDao.getJSONFromPost("", postSolicitado);
				return Utiles.retornarSalida(false, postJSON.toString());
			}
		}
	}
	
	
	
	@GET
	@Path("/photos")
	@ResponseBody
	public String getPhotos(@QueryParam("admin") String adminName,
							@QueryParam("accessToken") String accessToken,
							@QueryParam("idPost") Integer idPost){
		
		AdminEntity admin = administradorDao.verificarAdministrador(adminName, accessToken);
		if(admin == null){
			return Utiles.retornarSalida(true, "El nombre o la contrase\u00f1a son inv\u00e1lidos");
		} else {
			String retornoAntes = null;
			String retornoDespues = null;
		
			//foto de antes
			BufferedImage img = null;
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			byte[] imageInByte = null;
			
			try {
				img = ImageIO.read(new File(Utiles.PHOTOS_FOLDER + String.valueOf(idPost) + "antes_image.png"));
				ImageIO.write(img, "png", baos);
				baos.flush();
				imageInByte = baos.toByteArray();
				baos.close();
			} catch (IOException e) {
				e.printStackTrace();
				img = null;
			}
			
			if(img != null){
				retornoAntes = Base64.encodeToString(imageInByte, Base64.DEFAULT);
			}
			
			BufferedImage img2 = null;
			ByteArrayOutputStream baos2 = new ByteArrayOutputStream();
			byte[] imageInByte2 = null;
			
			try {
				img2 = ImageIO.read(new File(Utiles.PHOTOS_FOLDER + String.valueOf(idPost) + "despues_image.png"));
				ImageIO.write(img2, "png", baos2);
				baos2.flush();
				imageInByte2 = baos2.toByteArray();
				baos2.close();
			} catch (IOException e) {
				e.printStackTrace();
				img2 = null;
			}
			
			if(img2 != null){
				retornoDespues = Base64.encodeToString(imageInByte2, Base64.DEFAULT);
			}
			
			JSONObject retorno = new JSONObject();
			retorno.put("antes", retornoAntes);
			retorno.put("despues", retornoDespues);
			
			return Utiles.retornarImagen(false, retorno.toString());
		}
	}
	
	
	@POST
	@Path("/invalidateUser")
	@Consumes("application/x-www-form-urlencoded")
	@ResponseBody
	public String desactivarCuentaVoluntario(@FormParam("admin") String adminName,
												@FormParam("accessToken") String accessToken,
												@FormParam("username") String username){
		
		AdminEntity admin = administradorDao.verificarAdministrador(adminName, accessToken);
		if(admin == null){
			return Utiles.retornarSalida(true, "El nombre o la contrase\u00f1a son inv\u00e1lidos");
		} else {
			//buscamos el voluntario
			VoluntarioEntity voluntario = voluntarioDao.findByClassAndID(VoluntarioEntity.class, username);
			if(voluntario == null){
				return Utiles.retornarSalida(true, "No existe un voluntario con use nombre de usuario");
			} else {
				try{
					voluntario.setActivo(false);
					voluntarioDao.modificar(voluntario);
					return Utiles.retornarSalida(false, "El voluntario ha sido dado de baja");
				}catch(Exception e){
					e.printStackTrace();
					return Utiles.retornarSalida(true, "Ha ocurrido un error");
				}
			}
		}
	}
	
	
	@POST
	@Path("/activateUser")
	@Consumes("application/x-www-form-urlencoded")
	@ResponseBody
	public String activarCuentaUsuario(@FormParam("admin") String adminName,
										@FormParam("accessToken") String accessToken,
										@FormParam("username") String username){
		
		AdminEntity admin = administradorDao.verificarAdministrador(adminName, accessToken);
		if(admin == null){
			return Utiles.retornarSalida(true, "El nombre o la contrase\u00f1a son inv\u00e1lidos.");
		} else {
			//buscamos el voluntario
			VoluntarioEntity voluntario = voluntarioDao.findByClassAndID(VoluntarioEntity.class, username);
			if(voluntario == null){
				return Utiles.retornarSalida(true, "No existe un voluntario con use nombre de usuario.");
			} else {
				try{
					voluntario.setActivo(true);
					voluntarioDao.modificar(voluntario);
					return Utiles.retornarSalida(false, "La cuenta del voluntario ha sido activada.");
				}catch(Exception e){
					e.printStackTrace();
					return Utiles.retornarSalida(true, "Ha ocurrido un error.");
				}
			}
		}
	}
	
	
	/**
	 * Metodo que retorna en un array de JSON todas las relaciones de amistad existentes dentro de la red
	 * @param adminName
	 * @param password
	 * @return
	 */
	@GET
	@Path("/allNodeContacts")
	@ResponseBody
	public String getAllNodeContacts(@QueryParam("admin") String adminName, @QueryParam("accessToken") String accessToken){
		
		AdminEntity admin = administradorDao.verificarAdministrador(adminName, accessToken);
		JSONObject retorno = new JSONObject();
		if(admin == null){
			return Utiles.retornarSalida(true, "El nombre o la contrase\u00f1a son inv\u00e1lidos.");
		} else {
			//traemos una lista simplificada
			List<VoluntarioEntity> listaSimpleVoluntarios = voluntarioDao.getListAllUsers();
			if(listaSimpleVoluntarios.size() == 0){
				return Utiles.retornarSalida(true, "No hay voluntarios dentro de la red.");
			} else {
				JSONArray arrayNodos = new JSONArray();
				//agregamos los voluntarios como nodos
				for(VoluntarioEntity v: listaSimpleVoluntarios){
					JSONObject vTemp = new JSONObject();
					vTemp.put("id", v.getUserName());
					vTemp.put("label", v.getNombreReal());
					//del lado del cliente se agregaran las coordenadas
					arrayNodos.put(vTemp);
				}
				//agregamos el array de nodos al json de retorno
				retorno.put("nodes", arrayNodos);
				
				List<ContactoEntity> listaTotalContactos = contactoDao.listarTodosLosContactos();
				if(listaTotalContactos.size() == 0){
					return Utiles.retornarSalida(true, "A\u00fan no hay ninguna relaci\u00f3n de amistad formada dentro de la red.");
				} else {
					JSONArray arrayEdges = new JSONArray();
					for(ContactoEntity c: listaTotalContactos){
						JSONObject cTemp = new JSONObject();
						cTemp.put("id", String.valueOf(c.getIdAmistad()));
						cTemp.put("source", c.getVoluntario().getUserName());
						cTemp.put("target", c.getContacto().getUserName());
						cTemp.put("type", "arrow");
						cTemp.put("size", 1);
						arrayEdges.put(cTemp);
					}
					//agregamos el array de enlaces al json de retorno
					retorno.put("edges", arrayEdges);
					
					return Utiles.retornarSalida(false, retorno.toString());
				}
			}
		}
	}
	
	
	
	@GET
	@Path("/subtotalesReportes")
	@ResponseBody
	public String getSubtotalesReportes(@QueryParam("admin") String adminName, @QueryParam("accessToken") String accessToken){
		
		AdminEntity admin = administradorDao.verificarAdministrador(adminName, accessToken);
		JSONObject retorno = new JSONObject();
		if(admin == null){
			return Utiles.retornarSalida(true, "El nombre o la contrase\u00f1a son inv\u00e1lidos.");
		} else {
			Integer totalSolucionados = postDao.getTotalSolucionados();
			Integer totalNoSolucionados = postDao.getTotalNoSolucionados();
			
			retorno.put("solucionados", totalSolucionados);
			retorno.put("noSolucionados", totalNoSolucionados);
			
			return Utiles.retornarSalida(false, retorno.toString());
		}
	}
	
	
	@GET
	@Path("/reportesRelevantes")
	@ResponseBody
	public String getReportesRelevantes(@QueryParam("admin") String adminName, @QueryParam("accessToken") String accessToken){
		AdminEntity admin = administradorDao.verificarAdministrador(adminName, accessToken);
		JSONArray retorno = new JSONArray();
		if(admin == null){
			return Utiles.retornarSalida(true, "El nombre o la contrase\u00f1a son inv\u00e1lidos.");
		} else {
			List<PostEntity> listaRelevantes = postDao.getRelevantes();
			for(PostEntity p: listaRelevantes){
				JSONObject pJSON = postDao.getJSONFromPost("", p);
				retorno.put(pJSON);
			}
			return Utiles.retornarSalida(false, retorno.toString());		
		}
	}
	
	
	@GET
	@Path("/usuariosPorMes")
	@ResponseBody
	public String getCantUsuariosPorMes(@QueryParam("admin") String adminName, @QueryParam("accessToken") String accessToken){
		AdminEntity admin = administradorDao.verificarAdministrador(adminName, accessToken);
		JSONObject retorno = new JSONObject();
		if(admin == null){
			return Utiles.retornarSalida(true, "El nombre o la contrase\u00f1a son inv\u00e1lidos.");
		} else {
			//traemos la lista completa de voluntarios
			List<VoluntarioEntity> lista = voluntarioDao.getListAllUsers();
			for(VoluntarioEntity v: lista){
				Calendar cal = Calendar.getInstance();
				cal.setTime(v.getFechaIns());
				int mes = cal.get(Calendar.MONTH) + 1;
				int year = cal.get(Calendar.YEAR);
				String posibleKey = String.valueOf(year) + "-" + String.valueOf(mes);
				//si ya tiene le sumanos 1
				if(retorno.has(posibleKey)){
					//seteamos con el mismo valor + 1;
					retorno.put(posibleKey, retorno.getInt(posibleKey)+1);
				} else {
					//sino el valor inicial es 1
					retorno.put(posibleKey, 1);
				}
			}
			return Utiles.retornarSalida(false, retorno.toString());
		}	
	}
	
	
	@GET
	@Path("/usersByRanking")
	@ResponseBody
	public String getUsersByRanking(@QueryParam("admin") String adminName, @QueryParam("accessToken") String accessToken){
		AdminEntity admin = administradorDao.verificarAdministrador(adminName, accessToken);
		JSONArray retorno = new JSONArray();
		if(admin == null){
			return Utiles.retornarSalida(true, "El nombre o la contrase\u00f1a son inv\u00e1lidos.");
		} else {
			//traemos la lista completa de voluntarios
			List<VoluntarioEntity> lista = voluntarioDao.getListUsersByRanking();
			for(VoluntarioEntity v: lista){
				retorno.put(voluntarioDao.getSimpleJSONFromVoluntario(v));
			}
			return Utiles.retornarSalida(false, retorno.toString());
		}
	}
	
	
	@POST
	@Path("/campanha")
	@Consumes("application/x-www-form-urlencoded")
	@ResponseBody
	public String iniciarCampanha(@FormParam("admin") String adminName,
									@FormParam("accessToken") String accessToken,
									@FormParam("nombre") String nombreCampanha,
									@FormParam("mensaje") String mensajeCampanha,
									@FormParam("fechaLanzamiento") String fechaLanzamiento,
									@FormParam("fechaFinalizacion") String fechaFinalizacion,
									@FormParam("voluntariosInvitados") String voluntariosInvitados){
		
		AdminEntity admin = administradorDao.verificarAdministrador(adminName, accessToken);
		if(admin == null){
			return Utiles.retornarSalida(true, "El nombre o la contrase\u00f1a son inv\u00e1lidos.");
		} else {
			//buscamos la `nha por el nombre
			if(nombreCampanha.trim().equals("")){
				return Utiles.retornarSalida(true, "La campa\u00f1a debe contar con un nombre.");
			} else {
				if(campanhaDao.buscarPorNombre(nombreCampanha) != null){
					return Utiles.retornarSalida(true, "Ya existe una campa\u00f1a con ese nombre.");
				} else {
					CampanhaEntity campanha = new CampanhaEntity();
					//verificamos la fecha de lanzamiento
					if(fechaLanzamiento.trim().equals("")){
						return Utiles.retornarSalida(true, "La campa\u00f1a debe contar con fecha de inicio.");
					} else {
						if(fechaFinalizacion.trim().equals("")){
							return Utiles.retornarSalida(true, "La campa\u00f1a debe contar con una fecha de finalizaci\u00f3n.");
						}
						campanha.setNombreCampanha(nombreCampanha);
						if(!mensajeCampanha.trim().equals("")){
							campanha.setMensaje(mensajeCampanha);
						}
						
						//verificamos que la fecha de finalizcion sea superior a la fecha de inicio
						SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy");
						try {
							Date dateLanzamiento = formatter.parse(fechaLanzamiento);
							Date dateFinalizacion = formatter.parse(fechaFinalizacion);
							
							if(dateFinalizacion.before(dateLanzamiento)){
								return Utiles.retornarSalida(true, "La fecha de finalizaci�n no puede ser anterior a la fecha de inicio.");
							}
							
							campanha.setFechaLanzamiento(dateLanzamiento);
							campanha.setFechaFinalizacion(dateFinalizacion);
							
							JSONArray arrayInvitados = new JSONArray(voluntariosInvitados);
							if(arrayInvitados.length() == 0){
								return Utiles.retornarSalida(true, "La campa\u00f1a debe contar con al menos un voluntario como invitado.");
							}
							JSONArray retornoNoInvitados = new JSONArray();
							for(int i=0; i<arrayInvitados.length(); i++){
								String username = arrayInvitados.getString(i);
								//buscamos el username en la Base de Datos
								VoluntarioEntity invitado = voluntarioDao.findByClassAndID(VoluntarioEntity.class, username.toLowerCase());
								if(invitado == null || invitado.getActivo() == false){
									retornoNoInvitados.put(username);
								} else {
									campanha.getVoluntariosInvitados().add(invitado);
								}
							}
							//si la cantidad de retornoNoInvitados es igual al lenght de invitados, ningun voluntario era valido
							if(retornoNoInvitados.length() == arrayInvitados.length()){
								return Utiles.retornarSalida(true, "Ning\u00fan voluntario ten\u00eda un nombre de usuario v\u00e1lido, por favor, verif\u00edquelo y vuelta a intentarlo");
							}
							
							//intentamos guardar la campanha
							try{
								campanhaDao.guardar(campanha);
								//guardar para la notificacion de los voluntarios
								campanhaDao.guardarNotificacionParaVoluntarios(campanha, campanha.getVoluntariosInvitados());
								return Utiles.retornarSalida(false, retornoNoInvitados.toString());
							} catch(Exception ex){
								ex.printStackTrace();
								return Utiles.retornarSalida(true, "Hubo un error al intentar guardar la campa\u00f1a, por favor, intentalo de nuevo m\u00e1s tarde.");
							}
						} catch (ParseException e) {
							e.printStackTrace();
							return Utiles.retornarSalida(true, "La fecha no tiene un formato v\u00e1lido.");
						}
					}
				}
			}
		}
	}
	
	@GET
	@Path("/usersByCatA")
	@ResponseBody
	public String getUsersCatA(@QueryParam("admin") String adminName, @QueryParam("accessToken") String accessToken){
		AdminEntity admin = administradorDao.verificarAdministrador(adminName, accessToken);
		JSONArray retorno = new JSONArray();
		if(admin == null){
			return Utiles.retornarSalida(true, "El nombre o la contrase\u00f1a son inv\u00e1lidos.");
		} else {
			List<VoluntarioEntity> lista = voluntarioDao.getListCategoryA();
			for(VoluntarioEntity v: lista){
				retorno.put(voluntarioDao.getSimpleJSONFromVoluntario(v));
			}
			return Utiles.retornarSalida(false, retorno.toString());
		}
	}
	
	
	@GET
	@Path("/usersByCatB")
	@ResponseBody
	public String getUsersCatB(@QueryParam("admin") String adminName, @QueryParam("accessToken") String accessToken){
		AdminEntity admin = administradorDao.verificarAdministrador(adminName, accessToken);
		JSONArray retorno = new JSONArray();
		if(admin == null){
			return Utiles.retornarSalida(true, "El nombre o la contrase\u00f1a son inv\u00e1lidos.");
		} else {
			//traemos la lista completa de voluntarios
			List<VoluntarioEntity> lista = voluntarioDao.getListCategoryB();
			for(VoluntarioEntity v: lista){
				retorno.put(voluntarioDao.getSimpleJSONFromVoluntario(v));
			}
			return Utiles.retornarSalida(false, retorno.toString());
		}
	}
	
	@GET
	@Path("/allContacts")
	@ResponseBody
	public String getAllContacts(@QueryParam("admin") String adminName, @QueryParam("accessToken") String accessToken){
		AdminEntity admin = administradorDao.verificarAdministrador(adminName, accessToken);
		JSONArray retorno = new JSONArray();
		if(admin == null){
			return Utiles.retornarSalida(true, "El nombre o la contrase\u00f1a son inv\u00e1lidos.");
		} else {
			//traemos la lista completa de voluntarios
			List<VoluntarioEntity> lista = voluntarioDao.getListAllUsers();
			for(VoluntarioEntity v: lista){
				retorno.put(voluntarioDao.getSimpleJSONFromVoluntario(v));
			}
			return Utiles.retornarSalida(false, retorno.toString());
		}
	}
	
	
	@POST
	@Path("/alert")
	@Consumes("application/x-www-form-urlencoded")
	@ResponseBody
	public String lanzarAlertaAVoluntario(@FormParam("admin") String adminName, 
											@FormParam("accessToken") String accessToken,
											@FormParam("username") String username,
											@FormParam("mensaje") String mensajeAlerta){
		
		AdminEntity admin = administradorDao.verificarAdministrador(adminName, accessToken);
		JSONArray retorno = new JSONArray();
		if(admin == null){
			return Utiles.retornarSalida(true, "El nombre o la contrase\u00f1a son inv\u00e1lidos.");
		} else {
			//puede ser el mensaje del Administrador o uno por default
			String mensajeAMostrar;
			if(mensajeAlerta == null){
				mensajeAMostrar = Utiles.MENSAJE_DE_ALERTA;
			} else {
				if(mensajeAlerta.trim().equals("")){
					mensajeAMostrar = Utiles.MENSAJE_DE_ALERTA;
				} else {
					mensajeAMostrar = mensajeAlerta;
				}
			}
			if(username.trim().equals("")){
				Utiles.retornarSalida(true, "El nombre de usuario debe ser v\u00e1lido.");
			} else if(!username.matches(Utiles.REGEX_ALFANUMERIC)){
				Utiles.retornarSalida(true, "El nombre de usuario debe ser v\u00e1lido.");
			} else {
				VoluntarioEntity voluntarioEntity = voluntarioDao.findByClassAndID(VoluntarioEntity.class, username);
				if(voluntarioEntity == null){
					return Utiles.retornarSalida(true, "El voluntario no existe.");
				} else {
					try{
						voluntarioEntity.setMsjAlerta(mensajeAMostrar);
						voluntarioDao.modificar(voluntarioEntity);
						return Utiles.retornarSalida(false, "Alerta enviado.");
					} catch(Exception e){
						e.printStackTrace();
						return Utiles.retornarSalida(true, "Ha ocurrido un error.");
					}
				}
			}
		}
		return "";
	}
	
	
	@GET
	@Path("/timeline")
	@Produces("text/html; charset=UTF-8")
	@ResponseBody
	public String getTimeline(@QueryParam("admin") String adminName,
			 @QueryParam("accessToken") String accessToken,
			 @QueryParam("ultimaActualizacion") String ultimaActualizacionString){
		
		AdminEntity admin = administradorDao.verificarAdministrador(adminName, accessToken);
		if(admin == null){
			return Utiles.retornarSalida(true, "El nombre o la contrase\u00f1a son inv\u00e1lidos.");
		} else {
			Timestamp timestamp;
			try{
			    SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss.SSS");
			    Date parsedDate = dateFormat.parse(ultimaActualizacionString);
			    timestamp = new java.sql.Timestamp(parsedDate.getTime());
			    
			    List<PostEntity> listaRetorno = postDao.getAdminTimeline(timestamp);
			    JSONArray retorno = new JSONArray();
			    for(int j=0; j<listaRetorno.size(); j++){
			    	retorno.put(postDao.getJSONFromPost("", listaRetorno.get(j)));
			    }
			    
			    return Utiles.retornarSalida(false, retorno.toString());
			    
			}catch(Exception e){
				e.printStackTrace();
				return Utiles.retornarSalida(true, "Ha ocurrido un error.");
			}
		}
	}
	
	
	/**
	 * Metodo que retorna las cantidades de reportes que los voluntarios creen que su resolucion no esta en sus manos
	 * 
	 * @param adminName
	 * @param password
	 * @return
	 */
	@GET
	@Path("/quienDebeSolucionar")
	@Produces("text/html; charset=UTF-8")
	@ResponseBody
	public String getEstadisticasNoSolucionados(@QueryParam("admin") String adminName,
			 @QueryParam("accessToken") String accessToken){
		
		AdminEntity admin = administradorDao.verificarAdministrador(adminName, accessToken);
		if(admin == null){
			return Utiles.retornarSalida(true, "El nombre o la contrase\u00f1a son inv\u00e1lidos.");
		} else {
			try{
				List<PostEntity> listaNoSolucionados = postDao.listaQuienDebeSolucionar();
				JSONObject retorno = new JSONObject();
				for(int i=0; i<listaNoSolucionados.size(); i++){
					PostEntity p = listaNoSolucionados.get(i);
					if(retorno.has(p.getQuienDebeSolucionar())){
						Integer actualVal = retorno.getInt(p.getQuienDebeSolucionar());
						retorno.put(p.getQuienDebeSolucionar(), actualVal+1);
					} else {
						retorno.put(p.getQuienDebeSolucionar(), 1);
					}
				}
				return Utiles.retornarSalida(false, retorno.toString());
			} catch(Exception e){
				e.printStackTrace();
				return Utiles.retornarSalida(true, "Ha ocurrido un error.");
			}	
		}
	}
	
	
	@POST
	@Path("/validateAccessToken")
	@Consumes("application/x-www-form-urlencoded")
	@ResponseBody
	public String validateAuthentication(@FormParam("accessToken") String accessToken){
		
		if(accessToken == null){
			return Utiles.retornarSalida(true, "No autenticado.");
		} else {
			AdminAccessTokenEntity entity = adminAccessTokenDao.findByClassAndID(AdminAccessTokenEntity.class, accessToken);
			if(entity == null){
				return Utiles.retornarSalida(true, "No autenticado.");
			} else {
				return Utiles.retornarSalida(false, "Autenticado.");
			}
		}
	}
	
	@GET
	@Path("/activeCampaigns")
	@Produces("text/html; charset=UTF-8")
	@ResponseBody
	public String getCampanhasActivas(@QueryParam("admin") String adminName, @QueryParam("accessToken") String accessToken){
		
		AdminEntity admin = administradorDao.verificarAdministrador(adminName, accessToken);
		if(admin == null){
			return Utiles.retornarSalida(true, "El nombre o la contrase\u00f1a son inv\u00e1lidos.");
			
		} else {
			try{
				//buscamos todas las campanhas
				List<CampanhaEntity> lista = campanhaDao.getAll();
				JSONArray retorno = new JSONArray();
				for(int i=0; i<lista.size(); i++){
					retorno.put(campanhaDao.getJSONFromCampanha(lista.get(i), ""));
				}
				return Utiles.retornarSalida(false, retorno.toString());
			} catch(Exception e){
				e.printStackTrace();
				return Utiles.retornarSalida(true, "Ha ocurrido un error al obtener las campa\u00f1as lanzadas.");
			}
		}
	}
	
	@GET
	@Path("/pendingSolutions/{ente}")
	@Produces("text/html; charset=UTF-8")
	@ResponseBody
	public String getResponsablesPosts(@PathParam("ente") String ente,
										@QueryParam("admin") String adminName, 
										@QueryParam("accessToken") String accessToken,
										@QueryParam("ultimaactualizacion") String ultimaActualizacionString){
		
		AdminEntity admin = administradorDao.verificarAdministrador(adminName, accessToken);
		if(admin == null){
			return Utiles.retornarSalida(true, "El nombre o la contrase\u00f1a son inv\u00e1lidos.");
		} else {
			try{
				Timestamp timestamp;
				SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss.SSS");
			    Date parsedDate = dateFormat.parse(ultimaActualizacionString);
			    timestamp = new java.sql.Timestamp(parsedDate.getTime());
			    
				List<PostEntity> listaNoSolucionados = postDao.listaEnteDebeSolucionar(ente, timestamp);
				JSONArray retorno = new JSONArray();
				for(int j=0; j<listaNoSolucionados.size(); j++){
					retorno.put(postDao.getJSONFromPost("", listaNoSolucionados.get(j)));
				}
				return Utiles.retornarSalida(false, retorno.toString());
			} catch(Exception e){
				e.printStackTrace();
				return Utiles.retornarSalida(true, "Hubo un error al obtener la lista de reportes.");
			}
		}
	}
	
	
	@POST
	@Path("/resolveReport/{report}")
	@Consumes("application/x-www-form-urlencoded")
	@ResponseBody
	public String resolveReport(@PathParam("report") Integer reporteId,
								@FormParam("admin") String adminName, 
								@FormParam("accessToken") String accessToken){
		
		AdminEntity admin = administradorDao.verificarAdministrador(adminName, accessToken);
		if(admin == null){
			return Utiles.retornarSalida(true, "El nombre o la contrase\u00f1a son inv\u00e1lidos.");
		} else {
			//obtenemos el reporte
			PostEntity post = postDao.findByClassAndID(PostEntity.class, reporteId);
			if(post == null){
				return Utiles.retornarSalida(true, "El reporte no existe.");
			} else {
				//verificamos si alguno de los voluntario no lo resolvio ya
				if(post.getSolucionado()){
					return Utiles.retornarSalida(true, "Este reporte ya ha sido solucionado por un voluntario.");
				} else {
					try{
						post.setCerradoPorAdministrador(true);
						post.setAdministradorQueCerro(admin);
						Date date = new Date();
						Timestamp timestamp = new Timestamp(date.getTime());
						post.setFechaCerrado(timestamp);
						postDao.guardar(post);
						return Utiles.retornarSalida(false, "Reporte cerrado.");
					} catch(Exception e){
						e.printStackTrace();
						return Utiles.retornarSalida(true, "Ha ocurrido un error al cerrar el reporte.");
					}
				}
			}
		}
	}
	
	
	@GET
	@Path("/listCampaigns")
	@Produces("text/html; charset=UTF-8")
	@ResponseBody
	public String listaCampanhas(@QueryParam("admin") String adminName, 
								@QueryParam("accessToken") String accessToken,
								@QueryParam("id") Integer idUltimo){
		
		AdminEntity admin = administradorDao.verificarAdministrador(adminName, accessToken);
		if(admin == null){
			return Utiles.retornarSalida(true, "El nombre o la contrase\u00f1a son inv\u00e1lidos.");
		} else {
			try{
				//SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
			    //Date parsedDate = dateFormat.parse(ultimaActualizacionString);
			    
			    //List<CampanhaEntity> lista = campanhaDao.listaCampanhas(parsedDate);
				List<CampanhaEntity> lista = campanhaDao.listaCampanhas(idUltimo);
			    JSONArray retorno = new JSONArray();
			    for(int k=0; k<lista.size(); k++){
			    	retorno.put(campanhaDao.getJSONFromCampanha(lista.get(k), ""));
			    }
			    return Utiles.retornarSalida(false, retorno.toString());
			    
			} catch(Exception e){
				e.printStackTrace();
				return Utiles.retornarSalida(true, "Hubo un error al obtener la lista de reportes.");
			}
		}
	}
	
	
	@GET
	@Path("/campaign/adheridos/{id}")
	@Produces("text/html; charset=UTF-8")
	@ResponseBody
	public String listaAdheridos(@PathParam("id") Integer idCampanha,
								@QueryParam("admin") String adminName, 
								@QueryParam("accessToken") String accessToken){
		
		AdminEntity admin = administradorDao.verificarAdministrador(adminName, accessToken);
		if(admin == null){
			return Utiles.retornarSalida(true, "El nombre o la contrase\u00f1a son inv\u00e1lidos.");
		} else {
			CampanhaEntity campanha = campanhaDao.findByClassAndID(CampanhaEntity.class, idCampanha);
			if(campanha == null){
				return Utiles.retornarSalida(true, "La campa\u00f1a no existe.");
			} else {
				List<VoluntarioEntity> adheridos = campanha.getVoluntariosAdheridos();
				JSONArray retorno = new JSONArray();
				for(int i=0; i<adheridos.size(); i++){
					JSONObject v = voluntarioDao.getJSONFromVoluntario(adheridos.get(i));
					retorno.put(v);
				}
				return Utiles.retornarSalida(false, retorno.toString());
			}
		}
	}
	
	@GET
	@Path("/campaign/invitados/{id}")
	@Produces("text/html; charset=UTF-8")
	@ResponseBody
	public String listaInvitados(@PathParam("id") Integer idCampanha,
								@QueryParam("admin") String adminName, 
								@QueryParam("accessToken") String accessToken){
		
		AdminEntity admin = administradorDao.verificarAdministrador(adminName, accessToken);
		if(admin == null){
			return Utiles.retornarSalida(true, "El nombre o la contrase\u00f1a son inv\u00e1lidos.");
		} else {
			CampanhaEntity campanha = campanhaDao.findByClassAndID(CampanhaEntity.class, idCampanha);
			if(campanha == null){
				return Utiles.retornarSalida(true, "La campa\u00f1a no existe.");
			} else {
				List<VoluntarioEntity> invitados = campanha.getVoluntariosInvitados();
				JSONArray retorno = new JSONArray();
				for(int i=0; i<invitados.size(); i++){
					JSONObject v = voluntarioDao.getJSONFromVoluntario(invitados.get(i));
					retorno.put(v);
				}
				return Utiles.retornarSalida(false, retorno.toString());
			}
		}
	}
	
	
	
	
	@GET
	@Path("/solucionados")
	@Produces("text/html; charset=UTF-8")
	@ResponseBody
	public String getReportesSolucionados(@QueryParam("admin") String adminName,
										 @QueryParam("accessToken") String accessToken,
										 @QueryParam("ultimaActualizacion") String ultimaActualizacionString){
		
		AdminEntity admin = administradorDao.verificarAdministrador(adminName, accessToken);
		if(admin == null){
			return Utiles.retornarSalida(true, "El nombre o la contrase\u00f1a son inv\u00e1lidos.");
		} else {
			Timestamp timestamp;
			try{
			    SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss.SSS");
			    Date parsedDate = dateFormat.parse(ultimaActualizacionString);
			    timestamp = new java.sql.Timestamp(parsedDate.getTime());
			    
			    List<PostEntity> listaRetorno = postDao.listaReportesSolucionados(timestamp);
			    JSONArray retorno = new JSONArray();
			    for(int j=0; j<listaRetorno.size(); j++){
			    	retorno.put(postDao.getJSONFromPost("", listaRetorno.get(j)));
			    }
			    
			    return Utiles.retornarSalida(false, retorno.toString());
			    
			}catch(Exception e){
				e.printStackTrace();
				return Utiles.retornarSalida(true, "Ha ocurrido un error.");
			}
		}
	}
	
	@GET
	@Path("/noSolucionados")
	@Produces("text/html; charset=UTF-8")
	@ResponseBody
	public String getReportesNoSolucionados(@QueryParam("admin") String adminName,
										 @QueryParam("accessToken") String accessToken,
										 @QueryParam("ultimaActualizacion") String ultimaActualizacionString){
		
		AdminEntity admin = administradorDao.verificarAdministrador(adminName, accessToken);
		if(admin == null){
			return Utiles.retornarSalida(true, "El nombre o la contrase\u00f1a son inv\u00e1lidos.");
		} else {
			Timestamp timestamp;
			try{
			    SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss.SSS");
			    Date parsedDate = dateFormat.parse(ultimaActualizacionString);
			    timestamp = new java.sql.Timestamp(parsedDate.getTime());
			    
			    List<PostEntity> listaRetorno = postDao.listaReportesNoSolucionados(timestamp);
			    JSONArray retorno = new JSONArray();
			    for(int j=0; j<listaRetorno.size(); j++){
			    	retorno.put(postDao.getJSONFromPost("", listaRetorno.get(j)));
			    }
			    
			    return Utiles.retornarSalida(false, retorno.toString());
			    
			}catch(Exception e){
				e.printStackTrace();
				return Utiles.retornarSalida(true, "Ha ocurrido un error.");
			}
		}
	}
	
	@GET
	@Path("/cerrados")
	@Produces("text/html; charset=UTF-8")
	@ResponseBody
	public String getReportesCerrados(@QueryParam("admin") String adminName,
										 @QueryParam("accessToken") String accessToken,
										 @QueryParam("ultimaActualizacion") String ultimaActualizacionString){
		
		AdminEntity admin = administradorDao.verificarAdministrador(adminName, accessToken);
		if(admin == null){
			return Utiles.retornarSalida(true, "El nombre o la contrase\u00f1a son inv\u00e1lidos.");
		} else {
			Timestamp timestamp;
			try{
			    SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss.SSS");
			    Date parsedDate = dateFormat.parse(ultimaActualizacionString);
			    timestamp = new java.sql.Timestamp(parsedDate.getTime());
			    
			    List<PostEntity> listaRetorno = postDao.listaReportesCerrados(timestamp);
			    JSONArray retorno = new JSONArray();
			    for(int j=0; j<listaRetorno.size(); j++){
			    	retorno.put(postDao.getJSONFromPost("", listaRetorno.get(j)));
			    }
			    
			    return Utiles.retornarSalida(false, retorno.toString());
			    
			}catch(Exception e){
				e.printStackTrace();
				return Utiles.retornarSalida(true, "Ha ocurrido un error.");
			}
		}
	}
	
	
	//creamos un metodo auxiliar que permitira crear un administrador para el inicio
		@POST
		@Path("/createFirstAdmin")
		@Produces("text/html; charset=UTF-8")
		@ResponseBody
		public String createFirstAdmin(){
			try{
			AdminEntity primerAdmin = new AdminEntity();
			primerAdmin.setAdminName("administrador");
			primerAdmin.setNombre("Eliana");
			primerAdmin.setApellido("Ferreira");
			primerAdmin.setPassword(Utiles.getMD5("administrador"));
			primerAdmin.setCi(4278950);
			primerAdmin.setDireccion("Luque");
			primerAdmin.setEmail("elianaef817@gmail.com");
			primerAdmin.setTelefono("0971877088");
			
			administradorDao.guardar(primerAdmin);
			return Utiles.retornarSalida(false, "Administrador agregado.");
			}catch(Exception e){
				e.printStackTrace();
				return Utiles.retornarSalida(true, "Ha ocurrido un error al agregar el administrador.");
			}
		}
		
		
		
		@POST
		@Path("/newFirstAdmin")
		@Produces("text/html; charset=UTF-8")
		@ResponseBody
		public String addAdminAccount(@FormParam("adminName") String adminNane,
										@FormParam("password") String password,
										@FormParam("nombre") String nombre,
										@FormParam("apellido") String apellido,
										@FormParam("ci") Integer ci,
										@FormParam("direccion") String direccion,
										@FormParam("email") String email,
										@FormParam("telefono") String telefono){
			
			//verificar campos obligatorios
			if(adminNane.trim().equals("")){
				return Utiles.retornarSalida(true, "El nombre de usuario del Administrador no puede estar vac\u00edo.");
			} else {
				Boolean yaExiste = administradorDao.yaExisteAdministrador(adminNane);
				if(yaExiste){
					return Utiles.retornarSalida(true, "Ya existe un Administrador con ese nombre.");
				} else {
					if(password.trim().equals("")){
						return Utiles.retornarSalida(true, "La contrase\u00f1a no puede ser vac\u00eda.");
					} else if(nombre.trim().equals("") || apellido.trim().equals("")){
						return Utiles.retornarSalida(true, "El Administrador debe contar con nombre y apellido.");
					} else {
						//los demas ya no son obligatorios
						try{
							AdminEntity administrador = new AdminEntity();
							administrador.setAdminName(adminNane);
							administrador.setNombre(nombre);
							administrador.setPassword(password);
							administrador.setApellido(apellido);
							administrador.setCi(ci);
							administrador.setDireccion(direccion);
							administrador.setTelefono(telefono);
							administrador.setEmail(email);
							administradorDao.guardar(administrador);
							return Utiles.retornarSalida(false, "Datos del Administrador guardados correctamente.");
						} catch(Exception e){
							e.printStackTrace();
							return Utiles.retornarSalida(true, "Ha ocurrido un error al guardar los datos del Administrador.");
						}
					}
				}
			}
		}
		
		
		@GET
		@Path("/getInfo")
		@Produces("text/html; charset=UTF-8")
		@ResponseBody
		public String getAdminInfo(@QueryParam("admin") String adminName,
									@QueryParam("accessToken") String accessToken){
			AdminEntity admin = administradorDao.verificarAdministrador(adminName, accessToken);
			if(admin == null){
				return Utiles.retornarSalida(true, "El nombre o la contrase\u00f1a son inv\u00e1lidos.");
			} else {
				try {
					JSONObject retorno = administradorDao.getAllDataForEdit(admin);
					return Utiles.retornarSalida(false, retorno.toString());
					
				} catch(Exception e){
					e.printStackTrace();
					return Utiles.retornarSalida(true, "Ha ocurrido un error al obtener los datos del Administrador.");
				}
			}
		}
		
		
		@POST
		@Path("/new")
		@Consumes("application/x-www-form-urlencoded")
		@ResponseBody
		public String addNewAdmin(@FormParam("admin") String admin,
								  @FormParam("accessToken") String accessToken,
								  @FormParam("adminName") String adminName,
								  @FormParam("name") String name,
								  @FormParam("lastname") String lastname,
								  @FormParam("password") String password,
								  @FormParam("email") String email,
								  @FormParam("ci") Integer ci,
								  @FormParam("phone") String phone,
								  @FormParam("address") String address){
			
			
			AdminEntity adminEntity = administradorDao.verificarAdministrador(admin, accessToken);
			if(adminEntity == null){
				return Utiles.retornarSalida(true, "El nombre o la contrase\u00f1a son inv\u00e1lidos.");
			}
			
			if(adminName == null || adminName.equals("")){
				return Utiles.retornarSalida(true, "El Administrador debe tener un nombre de identificaci\u00f3n.");
			} else {
				if(administradorDao.yaExisteAdministrador(adminName)){
					return Utiles.retornarSalida(true, "Ya existe un Administrador con ese nombre de usuario.");
				} else {
					if(name == null || name.equals("") ||  lastname == null || lastname.equals("")){
						return Utiles.retornarSalida(true, "El Administrador debe contar con nombre y apellido.");
					} else if(password == null || password.equals("")){
						return Utiles.retornarSalida(true, "Se necesita una contrase\u00f1a para el Administrador.");
					} else if(ci == null){
						return Utiles.retornarSalida(true, "La C\u00e9dula de Identidad no puede estar vac\u00eda.");
					} else {
						try{
							AdminEntity entity = new AdminEntity();
							entity.setAdminName(adminName.toLowerCase());
							entity.setNombre(name);
							entity.setApellido(lastname);
							entity.setPassword(password);
							entity.setCi(ci);
							entity.setDireccion(address);
							entity.setTelefono(phone);
							entity.setEmail(email);
							
							administradorDao.guardar(entity);
							return Utiles.retornarSalida(false, "Administrador guardado.");						
						} catch(Exception e){
							e.printStackTrace();
							return Utiles.retornarSalida(true, "Ha ocurrido un error al guardar los datos del Administrador.");
						}
					}
				}
			}
		}
		
		
		@POST
		@Path("/update/{admin}")
		@Consumes("application/x-www-form-urlencoded")
		@ResponseBody
		public String updateAdminInfo(@PathParam("admin") String adminName,
								  @FormParam("newAdmin") String newAdminName,
								  @FormParam("accessToken") String accessToken,
								  @FormParam("name") String name,
								  @FormParam("lastname") String lastname,
								  @FormParam("password") String password,
								  @FormParam("passConfirm") String passConfirm,
								  @FormParam("email") String email,
								  @FormParam("ci") Integer ci,
								  @FormParam("phone") String phone,
								  @FormParam("address") String address){
			
			AdminEntity admin = administradorDao.verificarAdministrador(adminName.toLowerCase(), accessToken);
			if(admin == null){
				return Utiles.retornarSalida(true, "El nombre o la contrase\u00f1a son inv\u00e1lidos.");
			} else {
				try{
					//verificacamos si es que cambio su username y si no es repetido
					if(newAdminName != null && !newAdminName.equals("")){
						AdminEntity adminExistente = administradorDao.yaExisteAministrador(newAdminName);
						if(adminExistente != null){
							//verifico si no soy yo mismo
							if(!adminExistente.getIdAdministrador().equals(admin.getIdAdministrador())){
								return Utiles.retornarSalida(true, "Ya existe un Administrador con ese nombre de usuario.");
							}
						} else {
							admin.setAdminName(newAdminName);
						}
					}
						
					if(name != null && !name.equals("")){
						admin.setNombre(name);
					}
					if(lastname != null && !lastname.equals("")){
						admin.setApellido(lastname);
					}
					
					/*if(password != null && !password.equals("") && passConfirm != null && passConfirm.equals(password)){
						admin.setPassword(password);
					} else if((password != null && passConfirm == null) || !passConfirm.equals(password)){
						return Utiles.retornarSalida(true, "Las contrasenhas deben coincidir");
					}*/
					if(password != null && !password.equals("")){
						if(passConfirm == null || passConfirm.equals("")){
							return Utiles.retornarSalida(true, "Las contrase\u00f1as deben coincidir");
						} else {
							if(!password.equals(passConfirm)){
								return Utiles.retornarSalida(true, "Las contrase\u00f1as deben coincidir");
							} else {
								admin.setPassword(password);
							}
						}
					}
					
					if(email != null && !email.equals("")){
						admin.setEmail(email);
					}
					
					if(ci != null && !ci.equals("")){
						admin.setCi(ci);
					}
						
					if(phone != null && !phone.equals("")){
						admin.setTelefono(phone);
					}
					
					if(address != null && !address.equals("")){
						admin.setDireccion(address);
					}
					
					administradorDao.modificar(admin);
					JSONObject retorno = administradorDao.getJsonFromAdmin(admin);
					return Utiles.retornarSalida(false, retorno.toString());
				
				} catch(Exception e){
					e.printStackTrace();
					return Utiles.retornarSalida(true, "Ha ocurrido un error al actualizar los datos.");
				}
			}
		}
		
		
		
		@POST
		@Path("/delete/{adminToDelete}")
		@Consumes("application/x-www-form-urlencoded")
		@ResponseBody
		public String deleteAdmin(@PathParam("adminToDelete") String adminToDelete,
								  @FormParam("admin") String adminName,
								  @FormParam("accessToken") String accessToken){
			
			AdminEntity admin = administradorDao.verificarAdministrador(adminName.toLowerCase(), accessToken);
			if(admin == null){
				return Utiles.retornarSalida(true, "El nombre o la contrase\u00f1a son inv\u00e1lidos.");
			} else {
				try{
					//buscamos el administrador a ser eliminado
					AdminEntity adminBaja = administradorDao.yaExisteAministrador(adminToDelete.toLowerCase());
					if(adminBaja == null){
						return Utiles.retornarSalida(true, "El administrador a ser dado de baja no existe.");
					}
					
					adminBaja.setEliminado(true);
					administradorDao.modificar(adminBaja);
					System.out.println("El administrador " + adminToDelete + " ha sido dado de baja por " + adminName + ".");
					return Utiles.retornarSalida(false, "El administrador ha sido dado de baja.");
					
				} catch(Exception e){
					e.printStackTrace();
					return Utiles.retornarSalida(true, "Ha ocurrido un error al dar de baja al administrador.");
				}
			}
		}
		
		
		
		@GET
		@Path("/getAllActivedAdmin")
		@Produces("text/html; charset=UTF-8")
		@ResponseBody
		public String getAllActivedAdmins(@QueryParam("admin") String adminName,
											@QueryParam("accessToken") String accessToken){
			
			AdminEntity admin = administradorDao.verificarAdministrador(adminName, accessToken);
			if(admin == null){
				return Utiles.retornarSalida(true, "El nombre o la contrase\u00f1a son inv\u00e1lidos.");
			} else {
				try {
					List<AdminEntity> listaActivos = administradorDao.getListaActivos();
					//de esta lista excluimos al que solicito
					JSONArray retorno = new JSONArray();
					//siempre va a haber al menos un administrador activo, SIEMPRE
					for(int k=0; k<listaActivos.size(); k++){
						if(!listaActivos.get(k).getAdminName().equals(adminName)){
							JSONObject obj = administradorDao.getJsonFromAdmin(listaActivos.get(k));
							retorno.put(obj);
						}
					}
					return Utiles.retornarSalida(false, retorno.toString());
				} catch(Exception e){
					e.printStackTrace();
					return Utiles.retornarSalida(true, "Ha ocurrido un error al retornar la lista de administradores activos.");
				}
			}
		}
		
		
		
		@GET
		@Path("/getAllInactivedAdmin")
		@Produces("text/html; charset=UTF-8")
		@ResponseBody
		public String getAllInactivedAdmins(@QueryParam("admin") String adminName,
											@QueryParam("accessToken") String accessToken){
			
			AdminEntity admin = administradorDao.verificarAdministrador(adminName, accessToken);
			if(admin == null){
				return Utiles.retornarSalida(true, "El nombre o la contrase\u00f1a son inv\u00e1lidos.");
			} else {
				try {
					List<AdminEntity> listaActivos = administradorDao.getListaInactivos();
					//de esta lista excluimos al que solicito
					JSONArray retorno = new JSONArray();
					for(int k=0; k<listaActivos.size(); k++){
						if(!listaActivos.get(k).getAdminName().equals(adminName)){
							JSONObject obj = administradorDao.getJsonFromAdmin(listaActivos.get(k));
							retorno.put(obj);
						}
					}
					return Utiles.retornarSalida(false, retorno.toString());
				} catch(Exception e){
					e.printStackTrace();
					return Utiles.retornarSalida(true, "Ha ocurrido un error al retornar la lista de administradores inactivos.");
				}
			}
		}
		
		
		
		@POST
		@Path("/enable/{adminToEnable}")
		@Consumes("application/x-www-form-urlencoded")
		@ResponseBody
		public String enableAdmin(@PathParam("adminToEnable") String adminToEnable,
								  @FormParam("admin") String adminName,
								  @FormParam("accessToken") String accessToken){
			
			AdminEntity admin = administradorDao.verificarAdministrador(adminName.toLowerCase(), accessToken);
			if(admin == null){
				return Utiles.retornarSalida(true, "El nombre o la contrase\u00f1a son inv\u00e1lidos.");
			} else {
				try{
					//buscamos el administrador a ser dado de alta
					AdminEntity adminBaja = administradorDao.yaExisteAministrador(adminToEnable.toLowerCase());
					if(adminBaja == null){
						return Utiles.retornarSalida(true, "El administrador a ser dado de alta no existe.");
					}
					
					adminBaja.setEliminado(false);
					administradorDao.modificar(adminBaja);
					System.out.println("El administrador " + adminToEnable + " ha sido dado de alta por " + adminName + ".");
					return Utiles.retornarSalida(false, "El administrador ha sido dado de alta.");
					
				} catch(Exception e){
					e.printStackTrace();
					return Utiles.retornarSalida(true, "Ha ocurrido un error al dar de alta al administrador.");
				}
			}
		}
		
		
		@POST
		@Path("/deleteAllData")
		@Consumes("application/x-www-form-urlencoded")
		@ResponseBody
		public String deleteAllData(){
			try{
				administradorDao.deleteAllTables();
				return Utiles.retornarSalida(false, "Todos los datos eliminados satisfactoriamente.");			
			} catch(Exception e){
				e.printStackTrace();
				return Utiles.retornarSalida(true, "Ha ocurrido un error al limpiar la Base de Datos.");
			}
		}
	
}
