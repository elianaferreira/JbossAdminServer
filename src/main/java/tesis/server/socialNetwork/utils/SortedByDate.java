package tesis.server.socialNetwork.utils;

import java.util.Comparator;
import java.util.Date;

import org.json.JSONObject;

public class SortedByDate implements Comparator<JSONObject> {

	@Override
	public int compare(JSONObject o1, JSONObject o2) {
		try{
			Date fecha1 = (Date) o1.get("fecha");
			//SimpleDateFormat dateFormat1 = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss.SSS");
			//Date parsedDate1 = dateFormat1.parse(fecha1);
			//Timestamp timestamp1 = new java.sql.Timestamp(parsedDate1.getTime());
			
			Date fecha2 = (Date) o2.get("fecha");
			//Date parsedDate2 = dateFormat1.parse(fecha2);
			//Timestamp timestamp2 = new Timestamp(parsedDate2.getTime());
			
			/*if(fecha2.after(fecha1)){
				return -1;
			} else if(fecha2.before(fecha1)){
				return 1;
			}*/
			return fecha2.compareTo(fecha1);
		} catch(Exception ex){
			ex.printStackTrace();
		}
		return 0;
	}

}
