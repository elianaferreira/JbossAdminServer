package tesis.server.socialNetwork.utils;

import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.math.BigInteger;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import javax.imageio.ImageIO;

import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;

import tesis.server.socialNetwork.dao.AdministradorDao;
import tesis.server.socialNetwork.dao.VoluntarioDao;
import tesis.server.socialNetwork.entity.AdminEntity;
import tesis.server.socialNetwork.entity.VoluntarioEntity;


/**
 * Clase que contiene metodos y variables de utilidad
 * 
 * @author eliana
 *
 */
public class Utiles {
	
	public static final String PHOTOS_FOLDER = "C://tesisPhotos/";
	//public static final String PHOTOS_FOLDER = "WebContent\\images\\photos\\";
	//si un post pasa de esta cantidad de dias ya no puede ser relevante
	public static final long DIAS_PASADOS_RELEVANTE = 15;
	//variables de puntajes y reputacion
	public static final Integer PUNTAJE_POR_REPORTAR = 1;
	public static final Integer PUNTAJE_POR_SOLUCIONAR = 5;
	public static final Integer PUNTAJE_POR_RELEVANCIA = 2;
	public static final Integer PUNTAJE_FAVORITO = 3;
	public static final Integer PUNTAJE_NO_FAVORITO = -2;
	//por el momento tendremos que sea un tercio de la poblacion total de voluntarios
	public static final Integer PARTE_POBLACIONAL_PARA_MEDIR_RELEVANTES = 3;
	//tiempo pasado entre un post y su correspondiente repost
	public static final Integer HORAS_ENTRE_POST_Y_REPOST = 4;
	public static final Integer HORAS_ENTRE_MISMO_REPOST = 4;
	
	//tipos de notificacion
	public static final String NOTIF_NUEVA_SOLICITUD_AMISTAD = "NUEVA_SOLICITUD_AMISTAD";
	public static final String NOTIF_INVITADO_CAMPANHA = "INVITADO_CAMPANHA";
	
	public static final String MENSAJE_DE_ALERTA = "Esta es una advertencia, se ha detectado un mal uso de la aplicación por parte tuya.";
	
	public static final String REGEX_ALFANUMERIC = "^[a-zA-Z0-9]*$";
    public static final String REGEX_EMAIL = "^[_A-Za-z0-9-\\\\+]+(\\\\.[_A-Za-z0-9-]+)*@[A-Za-z0-9-]+(\\\\.[A-Za-z0-9]+)*(\\\\.[A-Za-z]{2,})$";
    
    public static final String SOLUCIONADOR_MUNICIPALIDAD = "Municipalidad";
    public static final String SOLUCIONADOR_SENEPA = "SENEPA";
    

	
	//acceso a Base de Datos
	@Autowired
	private static VoluntarioDao voluntarioDao;
	
	@Autowired
	private static AdministradorDao administradorDao;
	
	
	/**
	 * Metodo que crea un JSON a ser retornado al cliente
	 * 
	 * @param errorStatus
	 * @param mensaje
	 * @return
	 */
	public static String retornarSalida(boolean errorStatus, String mensaje){
		JSONObject retorno = new JSONObject();
		retorno.put("error", errorStatus);
		retorno.put("msj", mensaje);
		
		//escribimos en la consola
		System.out.println("MENSAJE RETORNO: " + mensaje);
		return retorno.toString();
	}
	
	/**
	 * Metodo que retorna un JSON con la imagen en String
	 * no se muestra la salida
	 * 
	 * @param errorStatus
	 * @param imagenString
	 * @return
	 */
	public static String retornarImagen(boolean errorStatus, String imagenString){
		JSONObject retorno = new JSONObject();
		retorno.put("error", errorStatus);
		retorno.put("msj", imagenString);
		
		return retorno.toString();
	}
	
	
	/**
	 * Metodo que verifica que un voluntario haya iniciado sesion
	 * 
	 * @param voluntarioEntity
	 * @return
	 */
	public static boolean haIniciadoSesion(VoluntarioEntity voluntarioEntity){
		if(voluntarioEntity.getLogged() == true){
			return true;
		} else{
			return false;
		}
	}
	
	
	/**
	 * Metodo que verifica que el administrador haya iniciado sesion
	 * 
	 * @param adminEntity
	 * @return
	 */
	public static boolean adminLogged(AdminEntity adminEntity){
		if(adminEntity.getLogged() == true){
			return true;
		} else{
			return false;
		}
	}
	
	
	public static void savePhoto(InputStream file, String fileName){
		try {
            OutputStream out = null;
            int read = 0;
            byte[] bytes = new byte[1024];
 
            out = new FileOutputStream(new File(PHOTOS_FOLDER + fileName));
            while ((read = file.read(bytes)) != -1) {
                out.write(bytes, 0, read);
            }
            out.flush();
            out.close();
        } catch (IOException e) {
 
            e.printStackTrace();
        }
	}
	
	
	/**
	 * Metodo que decide si un post es relevante de manera a ser mostrado a lo largo de toda la red de acuerdo a la cantidad
	 * de favoritos o de reposts en relacion a la cantidad de voluntarios de la red.
	 * 
	 * @param cantFavsOrRepost
	 * @param cantidadTotalVoluntarios
	 * @return
	 */
	public static Boolean puedeSerUnPostRelevante(Integer cantFavsOrRepost, Integer cantidadTotalVoluntarios){
		Integer parametro = cantidadTotalVoluntarios/PARTE_POBLACIONAL_PARA_MEDIR_RELEVANTES; //toma la parte entera del resultado
		if(cantFavsOrRepost >= parametro){
			return true;
		}
		return false;
	}
	
	/**
	 * Metodo que retorna la distancia entre dos coordenadas
	 * 
	 * @param lat1
	 * @param lon1
	 * @param lat2
	 * @param lon2
	 * @param unit
	 * @return
	 */
	public static double distance(double lat1, double lon1, double lat2, double lon2) {
		double theta = lon1 - lon2;
		double dist = Math.sin(deg2rad(lat1)) * Math.sin(deg2rad(lat2)) + Math.cos(deg2rad(lat1)) * Math.cos(deg2rad(lat2)) * Math.cos(deg2rad(theta));
		dist = Math.acos(dist);
		dist = rad2deg(dist);
		dist = dist * 60 * 1.1515;
		//distancia en Kilometros
		dist = dist * 1.609344;
		return (dist);
	}

	/*:::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::*/
	/*::  This function converts decimal degrees to radians             :*/
	/*:::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::*/
	private static double deg2rad(double deg) {
		return (deg * Math.PI / 180.0);
	}
	
	/*:::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::*/
	/*::  This function converts radians to decimal degrees             :*/
	/*:::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::*/
	private static double rad2deg(double rad) {
		return (rad * 180 / Math.PI);
	}
	
	
	/**
	 * Metodo que retorna el md5 de una cadena de texto
	 * 
	 * @param input
	 * @return
	 */
	public static String getMD5(String input) {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] messageDigest = md.digest(input.getBytes());
            BigInteger number = new BigInteger(1, messageDigest);
            String hashtext = number.toString(16);
            // Now we need to zero pad it if you actually want the full 32 chars.
            while (hashtext.length() < 32) {
                hashtext = "0" + hashtext;
            }
            return hashtext;
        }
        catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }
	
	
	/**
	 * Metodo que envia la image a Imgur y retorna el link para la descarga
	 * 
	 * @param image
	 * @return
	 * @throws Exception
	 */
	public static String uploadToImgur(BufferedImage image) throws Exception {
	    //String IMGUR_POST_URI = "http://api.imgur.com/2/upload.xml";
		String IMGUR_POST_URI = "https://api.imgur.com/3/upload.xml";	//aparentemente este es para alzar anonimamente
		//String IMGUR_POST_URI = "https://api.imgur.com/3/image";
	    String IMGUR_API_KEY = "c81b8b35ccf6ec5";

	    String linkString =  null;
	    
	    try {
	        ByteArrayOutputStream baos = new ByteArrayOutputStream();
	        System.out.println("Writing image...");
	        ImageIO.write(image, "jpeg", baos);
	        URL url = new URL(IMGUR_POST_URI);
	        System.out.println("Encoding...");

	        //String data = URLEncoder.encode("image", "UTF-8") + "=" + URLEncoder.encode(Base64.encodeBase64String(baos.toByteArray()).toString(), "UTF-8");
	        String data = URLEncoder.encode("image", "UTF-8") + "=" + URLEncoder.encode(Base64.encodeToString(baos.toByteArray(), Base64.DEFAULT), "UTF-8");
	        data += "&" + URLEncoder.encode("key", "UTF-8") + "=" + URLEncoder.encode(IMGUR_API_KEY, "UTF-8");
	        System.out.println("Connecting...");

	        URLConnection conn = url.openConnection();
	        conn.setDoOutput(true);
	        conn.setRequestProperty("Authorization", "Client-ID " + IMGUR_API_KEY);
	        conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
	        OutputStreamWriter wr = new OutputStreamWriter(conn.getOutputStream());
	        System.out.println("Sending data...");
	        /*wr.write(data);
	        wr.flush();
	        System.out.println("Finished.");*/
	        wr.write(data);
	        wr.close();

	        BufferedReader in = new BufferedReader(
	                                    new InputStreamReader(
	                                    conn.getInputStream()));
	        String decodedString;
	        while ((decodedString = in.readLine()) != null) {
	            System.out.println(decodedString);
	            //obtenemos el link
	            //TODO el indexOf lanza error
	            /*
	             * <?xml version="1.0" encoding="utf-8"?> es el primer valor de decodedString 
	             * mejorar esta impl para que solo en el segundo valor que si tiene link tome esto
	             */
	            if(decodedString.indexOf("<link>") > -1){
			        linkString = decodedString.substring(decodedString.indexOf("<link>")+6, decodedString.indexOf("</link>"));
			        System.out.print("LINK para descarga: " + linkString);
	            }
	        }
	        in.close();
	        
	        
	        return linkString;
	    } catch(Exception e){
	        e.printStackTrace();
	        return null;
	    }
	}
	
	
	
	
}
