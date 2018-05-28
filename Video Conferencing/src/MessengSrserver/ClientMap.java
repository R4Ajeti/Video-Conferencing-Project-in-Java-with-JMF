package MessengSrserver;
import java.util.*;

//Klasa per krijimin e listes se klienteve
public class ClientMap {

    static HashMap<String,String> hm = new HashMap<String,String>();
   
    void mapped(String nam,String ip)
    {
      if(hm.get(nam) == null)
      {
      
      	hm.put(nam,ip);
      	System.out.println(nam+" "+ip);
      }	
    }
    
}