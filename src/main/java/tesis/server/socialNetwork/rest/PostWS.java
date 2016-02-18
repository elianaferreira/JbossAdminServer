package tesis.server.socialNetwork.rest;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
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

import tesis.server.socialNetwork.dao.ComentarioDao;
import tesis.server.socialNetwork.dao.FavoritoDao;
import tesis.server.socialNetwork.dao.NoFavoritoDao;
import tesis.server.socialNetwork.dao.PostDao;
import tesis.server.socialNetwork.dao.RepostDao;
import tesis.server.socialNetwork.dao.VoluntarioDao;
import tesis.server.socialNetwork.entity.ComentarioEntity;
import tesis.server.socialNetwork.entity.FavoritoEntity;
import tesis.server.socialNetwork.entity.NoFavoritoEntity;
import tesis.server.socialNetwork.entity.PostEntity;
import tesis.server.socialNetwork.entity.RepostEntity;
import tesis.server.socialNetwork.entity.VoluntarioEntity;
import tesis.server.socialNetwork.utils.Base64;
import tesis.server.socialNetwork.utils.SortedByDate;
import tesis.server.socialNetwork.utils.Utiles;


@Stateless
@Path("/statuses")
public class PostWS {
	
	//acceso a Base de Datos
	@Inject
	private VoluntarioDao voluntarioDao;
	
	@Inject
	private PostDao postDao;
	
	@Inject
	private ComentarioDao comentarioDao;
	
	@Inject
	private FavoritoDao favoritoDao;
	
	@Inject
	private NoFavoritoDao noFavoritoDao;
	
	@Inject
	private RepostDao repostDao;
	
	
	
	/**
	 * Metodo que recibe un nuevo reporte.
	 * 
	 * @param mensaje
	 * @param username
	 * @param latitud
	 * @param longitud
	 * @return
	 */
	@Path("/new")
	@POST
	@Consumes("application/x-www-form-urlencoded")
	@Produces("text/html; charset=UTF-8")
	@ResponseBody
	public String newStatus(@FormParam("mensaje") String mensaje,
							@FormParam("username") String username,
							@FormParam("latitud") String latitud,
							@FormParam("longitud") String longitud,
							@FormParam("fotoAntes") String fotoAntes,
							@FormParam("fotoDespues") String fotoDespues,
							@FormParam("solucionado") Boolean solucionado,
							@FormParam("ranking") Integer rankingEstado,
							@FormParam("quienDebeSolucionar") String quienDebeSolucionar){
		
		//traemos el usuario de la Base de Datos
		VoluntarioEntity voluntarioEntity = voluntarioDao.findByClassAndID(VoluntarioEntity.class, username.toLowerCase());
		//verificamos que el usuario exista
		if(voluntarioEntity == null){
			return Utiles.retornarSalida(true, "El usuario no existe.");
		} else {
			//verificamos que haya iniciado sesion
			if(!Utiles.haIniciadoSesion(voluntarioEntity)){
				return Utiles.retornarSalida(true, "No has iniciado sesi�n.");
			} else {
				//intentamos guardar en la Base de Datos
				try {
					PostEntity postEntity = new PostEntity();
					postEntity.setPost(mensaje);
					postEntity.setVoluntario(voluntarioEntity);
					postEntity.setRankingEstado(rankingEstado);
					if(latitud != null && longitud != null){
						System.out.print("latitud: " + String.valueOf(latitud));
						System.out.print("longitud: " + String.valueOf(longitud));
						postEntity.setLatitud(Double.parseDouble(latitud));
						postEntity.setLongitud(Double.parseDouble(longitud));
					}
					postEntity.setSolucionado(solucionado);
					if(solucionado){
						postEntity.setVoluntarioQueSoluciona(voluntarioEntity);
						if(fotoDespues == null){
							return Utiles.retornarSalida(true, "No puede ser un reporte solucionado sin fotograf�a que lo pruebe.");
						}
					}
					postEntity.setQuienDebeSolucionar(quienDebeSolucionar);
					
					Integer idGen = postDao.guardar(postEntity);
					//una vez que se ha guardado se asocian las fotos con le ID del post en la BD
					if(fotoAntes != null){
						byte[] aByteArray = Base64.decode(fotoAntes, Base64.DEFAULT);
						BufferedImage img = ImageIO.read(new ByteArrayInputStream(aByteArray));

						ImageIO.write(img, "png", new File(Utiles.PHOTOS_FOLDER + String.valueOf(idGen) + "antes_image.png"));
					}
					if(fotoDespues != null){
						//Utiles.savePhoto(fotoDespues, "despues_" + String.valueOf(idGen));
						byte[] aByteArray = Base64.decode(fotoDespues, Base64.DEFAULT);
						BufferedImage img = ImageIO.read(new ByteArrayInputStream(aByteArray));

						ImageIO.write(img, "png", new File(Utiles.PHOTOS_FOLDER + String.valueOf(idGen) + "despues_image.png"));
					}
					
					voluntarioDao.updateReputation(voluntarioEntity, true, solucionado,false, false, false, false, false);
					return Utiles.retornarSalida(false, "Guardado.");
				} catch(Exception ex){
					ex.printStackTrace();
					return Utiles.retornarSalida(true, "Error al guardar el reporte.");
				}
			}
		}
	}
	
	
	/**
	 * Para editar un post este no debe tener imagen de 'despues'
	 * 
	 * @param idPost
	 * @param usernameEditor
	 * @param nuevoMensaje
	 * @param fotoDespues
	 * @return
	 */
	@Path("/updateAndResolve")
	@POST
	@Consumes("application/x-www-form-urlencoded")
	@Produces("text/html; charset=UTF-8")
	@ResponseBody
	public String resolver(@FormParam("id") Integer idPost,
						   @FormParam("username") String usernameEditor,
						   @FormParam("nuevoMensaje") String nuevoMensaje,
						   @FormParam("fotoDespues") String fotoDespues){
		
		//verificamos que el post exista en la Base de Datos
		PostEntity postEntity = postDao.findByClassAndID(PostEntity.class, idPost);
		if(postEntity == null){
			return Utiles.retornarSalida(true, "El reporte no existe.");
		} else {
			//verificamos que el voluntario exista
			VoluntarioEntity voluntarioEditor = voluntarioDao.findByClassAndID(VoluntarioEntity.class, usernameEditor.toLowerCase());
			if(voluntarioEditor == null){
				return Utiles.retornarSalida(true, "No existe el usuario.");
			} else {
				try{
					if(postEntity.getCerradoPorAdministrador()){
						return Utiles.retornarSalida(true, "Reporte cerrado por el Administrador.");
					}
					//actualizamos el post
					postEntity.setPost(nuevoMensaje);
					postEntity.setVoluntarioQueSoluciona(voluntarioEditor);
					postEntity.setSolucionado(true);
					postDao.modificar(postEntity);
					//agregamos la imagen
					byte[] aByteArray = Base64.decode(fotoDespues, Base64.DEFAULT);
					BufferedImage img = ImageIO.read(new ByteArrayInputStream(aByteArray));
					ImageIO.write(img, "png", new File(Utiles.PHOTOS_FOLDER + String.valueOf(idPost) + "despues_image.png"));
					
					voluntarioDao.updateReputation(voluntarioEditor, false, true, false,false, false, false, false);
					return Utiles.retornarSalida(false, "Reporte solucionado.");
				} catch(Exception ex){
					ex.printStackTrace();
					return Utiles.retornarSalida(true, "Error al solucionar el reporte.");
				}
			}
		}
	}
	
	/**
	 * Servicio que retorna una actualizacion del timeline principal del usuario
	 * (posts de sus amigos y de el)
	 * 
	 * @param username
	 * @param timestamp
	 * @param rango: es la cantidad de posts que se quiere recuperar, tendra un valor por defecto en el servidor
	 * @return
	 */
	@GET
	@Path("/timeline/{username}")
	@Produces("text/html; charset=UTF-8")
	@ResponseBody
	public String actualizarTimeline(@PathParam("username") String username,
									@QueryParam("ultimaactualizacion") String ultimaActualizacionString,
									@QueryParam("top") Boolean top){
		
		//verificaciones del usuario
		VoluntarioEntity voluntario = voluntarioDao.findByClassAndID(VoluntarioEntity.class, username.toLowerCase());
		if(voluntario == null){
			return Utiles.retornarSalida(true, "No existe el usuario.");
		} else {
			//verificamos si ha iniciado sesion
			if(voluntario.getLogged() == false){
				//no ha iniciado sesion
				return Utiles.retornarSalida(true, "No has iniciado sesi�n.");
			} else {
				//existe el usuario y ha iniciado sesion
				Timestamp timestamp;
				try{
				    SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss.SSS");
				    Date parsedDate = dateFormat.parse(ultimaActualizacionString);
				    timestamp = new java.sql.Timestamp(parsedDate.getTime());
				    
				    List<JSONObject> retornoArray = new ArrayList<JSONObject>();
					List<PostEntity> posts = postDao.getPosts(username, timestamp, top);
					for(int i=0; i<posts.size(); i++){
						JSONObject postJSON = postDao.getJSONFromPost(username, posts.get(i));
						retornoArray.add(postJSON);
					}
					List<RepostEntity> reposts = repostDao.getReposts(username, timestamp, top);
					List<RepostEntity> repostsFinales = repostDao.getRepostsMasDistantesDelPost(reposts);
					for(int j=0; j<repostsFinales.size(); j++){
						JSONObject repostJSON = repostDao.getJSONFromRepost(repostsFinales.get(j), username);
						retornoArray.add(repostJSON);
					}
					
					Collections.sort(retornoArray, new SortedByDate());
					
					return Utiles.retornarSalida(false, retornoArray.toString());
				}catch(Exception e){//this generic but you can control another types of exception
					//look the origin of exception 
					e.printStackTrace();
				}
				return "";
			}
		}
	}
	
	
	@POST
	@Path("/reply/{idPost}")
	@Consumes("application/x-www-form-urlencoded")
	@Produces("text/html; charset=UTF-8")
	@ResponseBody
	public String responderPost(@PathParam("idPost") Integer idPostToReply,
								@FormParam("respuesta") String respuesta,
								@FormParam("username") String usernameQuienResponde){
		
		//verificamos si el usuario que intenta responder existe y si ha iniciado sesion
		VoluntarioEntity voluntario = voluntarioDao.findByClassAndID(VoluntarioEntity.class, usernameQuienResponde.toLowerCase());
		if(voluntario == null){
			return Utiles.retornarSalida(true, "No existe el usuario.");
		} else {//verificamos si ha iniciado sesion
			if(voluntario.getLogged() == false){
				//no ha iniciado sesion
				return Utiles.retornarSalida(true, "No has iniciado sesi�n.");
			} else {
				//buscamos el post
				PostEntity postARepsonder = postDao.findByClassAndID(PostEntity.class, idPostToReply);
				if(postARepsonder == null){
					return Utiles.retornarSalida(true, "El reporte no existe.");
				} else {
					//verificamos que la respuesta no sea un cadena vacia
					if(!respuesta.isEmpty()){
						//creamos el comentario
						ComentarioEntity comentario = new ComentarioEntity();
						comentario.setAutor(voluntario);
						comentario.setPost(postARepsonder);
						comentario.setCuerpoDelComentario(respuesta);
						comentarioDao.guardar(comentario);
						return Utiles.retornarSalida(false, "Comentario agregado.");
					}
				}
			}
		}
		return "";
	}
	
	@GET
	@Path("/answers/{idPost}")
	@Produces("text/html; charset=UTF-8")
	@ResponseBody
	public String getComentarios(@PathParam("idPost") Integer idPost,
								@QueryParam("username") String usernameSolicitante){
		//verificamos si el usuario que intenta responder existe y si ha iniciado sesion
		VoluntarioEntity voluntario = voluntarioDao.findByClassAndID(VoluntarioEntity.class, usernameSolicitante.toLowerCase());
		if(voluntario == null){
			return Utiles.retornarSalida(true, "No existe el usuario.");
		} else {//verificamos si ha iniciado sesion
			if(voluntario.getLogged() == false){
				//no ha iniciado sesion
				return Utiles.retornarSalida(true, "No has iniciado sesi�n.");
			} else {
				//buscamos el post
				PostEntity postSolicitadp = postDao.findByClassAndID(PostEntity.class, idPost);
				if(postSolicitadp == null){
					return Utiles.retornarSalida(true, "El reporte no existe.");
				} else {
					//retornamos la lista de JSON de los comentarios
					JSONArray listaRetorno = new JSONArray();
					List<ComentarioEntity> listaComentarios = comentarioDao.listarComentariosDePost(idPost);
					//si esta vacia se envia asi mismo
					for(int i=0; i< listaComentarios.size(); i++){
						JSONObject comentarioJSON = comentarioDao.getJSONFromComment(listaComentarios.get(i));
						listaRetorno.put(comentarioJSON);
					}
					return Utiles.retornarSalida(false, listaRetorno.toString());
				}
			}
		}
	}
	
	
	@POST
	@Path("/favorito/{idPost}")
	@Consumes("application/x-www-form-urlencoded")
	@Produces("text/html; charset=UTF-8")
	@ResponseBody
	public String marcarComoFavorito(@PathParam("idPost") Integer idPost,
									 @FormParam("username") String usuarioQueMarca){
		
		//verificamos si el usuario que intenta responder existe y si ha iniciado sesion
		VoluntarioEntity voluntario = voluntarioDao.findByClassAndID(VoluntarioEntity.class, usuarioQueMarca.toLowerCase());
		if(voluntario == null){
			return Utiles.retornarSalida(true, "No existe el usuario.");
		} else {//verificamos si ha iniciado sesion
			if(voluntario.getLogged() == false){
				//no ha iniciado sesion
				return Utiles.retornarSalida(true, "No has iniciado sesi�n.");
			} else {
				//buscamos el post
				PostEntity postSolicitado = postDao.findByClassAndID(PostEntity.class, idPost);
				if(postSolicitado == null){
					return Utiles.retornarSalida(true, "El reporte no existe.");
				} else {
					//verificamos si es de marcado o desmarcado
					//buscamos la entidad FAV perteneciente al usuario y al post
					FavoritoEntity fav = favoritoDao.buscarMarcacion(idPost, usuarioQueMarca);
					if(fav == null){
						Boolean previoMalo = false;
						//creamos la entidad correspondiente al marcado como favorito
						FavoritoEntity favoritoEntity = new FavoritoEntity();
						favoritoEntity.setAutor(voluntario);
						favoritoEntity.setPost(postSolicitado);
						//lo guardamos
						favoritoDao.guardar(favoritoEntity);
						//relevante
						postDao.setPostRelevante(postSolicitado);
						//buscamos el noFav si existe y lo eliminamos
						NoFavoritoEntity noFavEliminar = noFavoritoDao.buscarMarcacion(idPost, usuarioQueMarca);
						if(noFavEliminar == null){
							//no hacer nada
						} else {
							//lo eliminamos
							noFavoritoDao.eliminar(noFavEliminar);
							previoMalo = true;
						}
						//enviamos la cantidad de marcaciones buenas y malas
						Integer cantidadBuenos = favoritoDao.cantidadFavoritosByPost(postSolicitado);
						Integer cantidadMalos = noFavoritoDao.cantidadNoFavoritosByPost(postSolicitado);
						String retorno = postDao.getJSONFromMarcaciones(cantidadBuenos, cantidadMalos, true, false, false, previoMalo);
						
						voluntarioDao.updateReputation(postSolicitado.getVoluntario(), false, false, false, true, false, false, previoMalo);
						return Utiles.retornarSalida(false, retorno);
					} else {
						//lo eliminamos
						favoritoDao.delete(fav);
						//enviamos la cantidad de marcaciones buenas y malas
						Integer cantidadBuenos = favoritoDao.cantidadFavoritosByPost(postSolicitado);
						Integer cantidadMalos = noFavoritoDao.cantidadNoFavoritosByPost(postSolicitado);
						
						String retorno = postDao.getJSONFromMarcaciones(cantidadBuenos, cantidadMalos, false, true, false, false);
						
						voluntarioDao.updateReputation(postSolicitado.getVoluntario(), false, false, false, false, true, false, false);
						return Utiles.retornarSalida(false, retorno);
					
					}
				}
			}
		}
	}
	
	@POST
	@Path("/noFavorito/{idPost}")
	@Consumes("application/x-www-form-urlencoded")
	@Produces("text/html; charset=UTF-8")
	@ResponseBody
	public String marcarComoNoFavorito(@PathParam("idPost") Integer idPost,
									   @FormParam("username") String usuarioQueMarca){
		//verificamos si el usuario que intenta responder existe y si ha iniciado sesion
		VoluntarioEntity voluntario = voluntarioDao.findByClassAndID(VoluntarioEntity.class, usuarioQueMarca.toLowerCase());
		if(voluntario == null){
			return Utiles.retornarSalida(true, "No existe el usuario.");
		} else {//verificamos si ha iniciado sesion
			if(voluntario.getLogged() == false){
				//no ha iniciado sesion
				return Utiles.retornarSalida(true, "No has iniciado sesi�n.");
			} else {
				//buscamos el post
				PostEntity postSolicitado = postDao.findByClassAndID(PostEntity.class, idPost);
				if(postSolicitado == null){
					return Utiles.retornarSalida(true, "El reporte no existe.");
				} else {
					//verificamos si es de marcado o desmarcado
					//buscamos la entidad FAV perteneciente al usuario y al post
					NoFavoritoEntity noFav = noFavoritoDao.buscarMarcacion(idPost, usuarioQueMarca);
					if(noFav == null){
						Boolean previoBueno = false;
						//creamos la entidad correspondiente al marcado como noFavorito
						NoFavoritoEntity noFavoritoEntity = new NoFavoritoEntity();
						noFavoritoEntity.setAutor(voluntario);
						noFavoritoEntity.setPost(postSolicitado);
						//lo guardamos
						noFavoritoDao.guardar(noFavoritoEntity);
						//relevante
						postDao.setPostRelevante(postSolicitado);
						//buscamos el fav si existe y lo eliminamos
						FavoritoEntity favEliminar = favoritoDao.buscarMarcacion(idPost, usuarioQueMarca);
						if(favEliminar == null){
							//no hacer nada
						} else {
							//lo eliminamos
							favoritoDao.eliminar(favEliminar);
							previoBueno = true;
						}
						//enviamos la cantidad de marcaciones buenas y malas
						Integer cantidadBuenos = favoritoDao.cantidadFavoritosByPost(postSolicitado);
						Integer cantidadMalos = noFavoritoDao.cantidadNoFavoritosByPost(postSolicitado);
						String retorno = postDao.getJSONFromMarcaciones(cantidadBuenos, cantidadMalos, false, previoBueno, true, false);
						
						voluntarioDao.updateReputation(postSolicitado.getVoluntario(), false, false, false, false, previoBueno, true, false);
						return Utiles.retornarSalida(false, retorno);
					} else {
						//lo eliminamos
						noFavoritoDao.eliminar(noFav);
						//enviamos la cantidad de marcaciones buenas y malas
						Integer cantidadBuenos = favoritoDao.cantidadFavoritosByPost(postSolicitado);
						Integer cantidadMalos = noFavoritoDao.cantidadNoFavoritosByPost(postSolicitado);
						
						String retorno = postDao.getJSONFromMarcaciones(cantidadBuenos, cantidadMalos, false, false, false, true);
						
						voluntarioDao.updateReputation(postSolicitado.getVoluntario(), false, false, false, false, false, false, true);
						return Utiles.retornarSalida(false, retorno);
					}
				}
			}
		}
	}
	
	
	/**
	 * En este metodo se devolvera el post junto con sus comentarios
	 * @param idPost
	 * @param usernameSolicitante
	 * @return
	 */
	@GET
	@Path("/post/{idPost}")
	@Produces("text/html; charset=UTF-8")
	@ResponseBody
	public String getPost(@PathParam("idPost") Integer idPost,
						  @QueryParam("username") String usernameSolicitante){
		
		//verificamos si el usuario que intenta responder existe y si ha iniciado sesion
		VoluntarioEntity voluntario = voluntarioDao.findByClassAndID(VoluntarioEntity.class, usernameSolicitante.toLowerCase());
		if(voluntario == null){
			return Utiles.retornarSalida(true, "No existe el usuario.");
		} else {//verificamos si ha iniciado sesion
			if(voluntario.getLogged() == false){
				//no ha iniciado sesion
				return Utiles.retornarSalida(true, "No has iniciado sesi�n.");
			} else {
				//buscamos el post
				PostEntity postSolicitado = postDao.findByClassAndID(PostEntity.class, idPost);
				if(postSolicitado == null){
					return Utiles.retornarSalida(true, "El reporte no existe.");
				} else {
					//lo pasamos a JSON
					JSONObject jsonPost = postDao.getJSONFromPost(usernameSolicitante, postSolicitado);
					//ahora obtenemos los comentarios
					//retornamos la lista de JSON de los comentarios
					JSONArray jsonArrayComentarios = new JSONArray();
					List<ComentarioEntity> listaComentarios = comentarioDao.listarComentariosDePost(idPost);
					//si esta vacia se envia asi mismo
					for(int i=0; i< listaComentarios.size(); i++){
						JSONObject comentarioJSON = comentarioDao.getJSONFromComment(listaComentarios.get(i));
						jsonArrayComentarios.put(comentarioJSON);
					}
					//juntamos en un solo JSON
					JSONObject jsonRetorno = new JSONObject();
					jsonRetorno.put("post", jsonPost);
					jsonRetorno.put("comentarios", jsonArrayComentarios);
					//lo enviamos
					return Utiles.retornarSalida(false, jsonRetorno.toString());
				}
			}
		}
	}
	
	
	@POST
	@Path("/repost/{idPost}")
	@Consumes("application/x-www-form-urlencoded")
	@Produces("text/html; charset=UTF-8")
	@ResponseBody
	public String repost(@PathParam("idPost") Integer idPost,
						 @FormParam("username") String usernameRepost){
		//verificamos si el usuario que intenta responder existe y si ha iniciado sesion
		VoluntarioEntity voluntario = voluntarioDao.findByClassAndID(VoluntarioEntity.class, usernameRepost.toLowerCase());
		if(voluntario == null){
			return Utiles.retornarSalida(true, "No existe el usuario.");
		} else {//verificamos si ha iniciado sesion
			if(voluntario.getLogged() == false){
				return Utiles.retornarSalida(true, "No has iniciado sesi�n.");
			} else {
				PostEntity postSolicitado = postDao.findByClassAndID(PostEntity.class, idPost);
				if(postSolicitado == null){
					return Utiles.retornarSalida(true, "El reporte no existe.");
				} else {
					RepostEntity repost = new RepostEntity();
					repost.setPost(postSolicitado);
					repost.setAutorRepost(voluntario);
					repostDao.guardar(repost);
					return Utiles.retornarSalida(false, "Repost realizado.");
				}
			}
		}
	}
	
	
	//TODO @DELETE???
	/*@POST
	@Path("/repost/{idRepost}")
	@Consumes("application/x-www-form-urlencoded")
	@ResponseBody
	public String eliminarRepost(@PathParam("idRepost") Integer idRepost,
						 @FormParam("username") String usernameRepost){
		//verificamos si el usuario que intenta responder existe y si ha iniciado sesion
		VoluntarioEntity voluntario = voluntarioDao.findByClassAndID(VoluntarioEntity.class, usernameRepost);
		if(voluntario == null){
			return Utiles.retornarSalida(true, "No existe el usuario");
		} else {//verificamos si ha iniciado sesion
			if(voluntario.getLogged() == false){
				return Utiles.retornarSalida(true, "No has iniciado sesi�n");
			} else {
				RepostEntity repost = repostDao.findByClassAndID(RepostEntity.class, idRepost);
				if(repost == null){
					return Utiles.retornarSalida(true, "No has hecho un repost de este post o el post ha sido eliminado");
				} else {
					//verificamos que solo el autor lo intente eliminar
					if(repost.getAutorRepost().getUserName() != usernameRepost){
						return Utiles.retornarSalida(true, "No puedes deshacer un repost que no has hecho");
					} else {
						repostDao.eliminar(repost);
						return Utiles.retornarSalida(false, "Repost deshecho");
					}
				}
			}
		}
	}*/

	
	@GET
	@Path("/relevantes/{username}")
	@Consumes("application/x-www-form-urlencoded")
	@Produces("text/html; charset=UTF-8")
	@ResponseBody
	public String postsRelevantes(@PathParam("username") String usernameSolicitante){
		
		VoluntarioEntity voluntario = voluntarioDao.findByClassAndID(VoluntarioEntity.class, usernameSolicitante.toLowerCase());
		if(voluntario == null){
			return Utiles.retornarSalida(true, "No existe el usuario.");
		} else {//verificamos si ha iniciado sesion
			if(voluntario.getLogged() == false){
				return Utiles.retornarSalida(true, "No has iniciado sesi�n.");
			} else {
				List<PostEntity> listaRelevantes = postDao.getRelevantes();
				JSONArray retorno = new JSONArray();
				for(int i=0; i<listaRelevantes.size(); i++){
					JSONObject postJSON = postDao.getJSONFromPost(usernameSolicitante, listaRelevantes.get(i));
					/*if(listaRelevantes.get(i).getVoluntario().getFotoDePerfil() != null){
						postJSON.put("fotoPerfil", Base64.encodeToString(listaRelevantes.get(i).getVoluntario().getFotoDePerfil(), Base64.DEFAULT));
					}*/
					retorno.put(postJSON);
				}
				return Utiles.retornarSalida(false, retorno.toString());
			}
		}
	}
	
	
	@GET
	@Path("/cercanos")
	@Consumes("application/x-www-form-urlencoded")
	@Produces("text/html; charset=UTF-8")
	@ResponseBody
	public String resportesCercanos(@QueryParam("username") String username,
									@QueryParam("distancia") double distancia,
									@QueryParam("latitud") double latitud,
									@QueryParam("longitud") double longitud){
		//hacemos un barrido a la Base de Datos
		List<PostEntity> lista = postDao.getAll();
		JSONArray retorno = new JSONArray();
		for(int i=0; i<lista.size(); i++){
			if(lista.get(i).getLatitud() != null && lista.get(i).getLongitud() != null){
				double distanciaMedida = Utiles.distance(lista.get(i).getLatitud(), lista.get(i).getLongitud(), latitud, longitud);
				if(distanciaMedida <= distancia){
					JSONObject postJSON = postDao.getJSONFromPost(username, lista.get(i));
					retorno.put(postJSON);
				}
			}
		}		
		return Utiles.retornarSalida(false, retorno.toString());
	}
	
	
	
	@GET
	@Path("/photos")
	@Consumes("application/x-www-form-urlencoded")
	@Produces("text/html; charset=UTF-8")
	@ResponseBody
	public String getPhoto(@QueryParam("username") String usernameSolicitante,
							@QueryParam("idPost") Integer idPost,
							@QueryParam("fotoAntes") String fotoAntes,
							@QueryParam("fotoDespues") String fotoDespues){
		
		VoluntarioEntity voluntario = voluntarioDao.findByClassAndID(VoluntarioEntity.class, usernameSolicitante.toLowerCase());
		if(voluntario == null){
			return Utiles.retornarSalida(true, "No existe el usuario.");
		} else {//verificamos si ha iniciado sesion
			if(voluntario.getLogged() == false){
				return Utiles.retornarSalida(true, "No has iniciado sesi�n.");
			} else {
				String flagTipoFoto = "";
				if(fotoAntes != null){
					flagTipoFoto = "antes_image.png";
				} else if(fotoDespues != null){
					flagTipoFoto = "despues_image.png";
				}				
				
				BufferedImage img = null;
				ByteArrayOutputStream baos = new ByteArrayOutputStream();
				byte[] imageInByte = null;
				
				try {
					img = ImageIO.read(new File(Utiles.PHOTOS_FOLDER + String.valueOf(idPost) + flagTipoFoto));
					ImageIO.write(img, "png", baos);
					baos.flush();
					imageInByte = baos.toByteArray();
					baos.close();
				} catch (IOException e) {
					e.printStackTrace();
					img = null;
				}
				
				if(img == null){
					return Utiles.retornarSalida(true, "Sin foto o error al retornar la foto.");
				} else {
					return Utiles.retornarImagen(false, Base64.encodeToString(imageInByte, Base64.DEFAULT));
				}
			}
		}
	}
	
	
	@GET
	@Path("/photo")
	@Consumes("application/x-www-form-urlencoded")
	@Produces("text/html; charset=UTF-8")
	@ResponseBody
	public String getOnePhoto(@QueryParam("usernameProfile") String usernameProfile,
								@QueryParam("idPost") Integer idPost,
								@QueryParam("antes") Boolean fotoAntes){
		
		if(usernameProfile != null){
			//retornamos la foto de perfil
			VoluntarioEntity voluntario = voluntarioDao.findByClassAndID(VoluntarioEntity.class, usernameProfile.toLowerCase());
			if(voluntario != null){
				if(voluntario.getFotoDePerfil() != null){
					System.out.println("FOTO DE PERFIL DE " + usernameProfile);
					return Utiles.retornarImagen(false, Base64.encodeToString(voluntario.getFotoDePerfil(), Base64.DEFAULT));
				}
			}
		} else if(idPost != null){
			String antesDespues = "antes_image.png";
			if(fotoAntes == false){
				antesDespues = "despues_image.png";
			}
			
			BufferedImage img = null;
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			byte[] imageInByte = null;
			
			try {
				img = ImageIO.read(new File(Utiles.PHOTOS_FOLDER + String.valueOf(idPost) + antesDespues));
				ImageIO.write(img, "png", baos);
				baos.flush();
				imageInByte = baos.toByteArray();
				baos.close();
			} catch (IOException e) {
				//e.printStackTrace();
				img = null;
			}
			
			if(img != null) {
				System.out.println("FOTO " + String.valueOf(idPost) + antesDespues);
				return Utiles.retornarImagen(false, Base64.encodeToString(imageInByte, Base64.DEFAULT));
			}
		}
		
		return Utiles.retornarSalida(true, "Nada que retornar.");
	}

}
