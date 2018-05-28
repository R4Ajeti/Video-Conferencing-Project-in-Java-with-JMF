package messengerclient;

import javax.media.*;   
import javax.media.rtp.*;   
import javax.media.rtp.event.*;   
import javax.media.rtp.rtcp.*;   
import javax.media.protocol.*;   
import javax.media.protocol.DataSource;   
import javax.media.format.AudioFormat;   
import javax.media.format.VideoFormat;   
import javax.media.Format;   
import javax.media.format.FormatChangeEvent;   
import javax.media.control.BufferControl;   

import java.util.StringTokenizer;

//Kjo klase permban detajet e video komunikimit
class VideoConference implements Runnable {
	public String message =""; 
	boolean keepListening = true;	
	VideoConference(String message)
	{
		this.message = message;
	}
	public void run()
    {
    	while(keepListening)
    	{
    	
	    	String name = "";
	    	String ips = "";
	    	System.out.println("Video klasa eshte duke pranuar "+ message);
	        StringTokenizer tokens=new StringTokenizer(message);
	
	        String header=tokens.nextToken();
	        if(tokens.hasMoreTokens())
	            name=tokens.nextToken();
	            
	         // Video Receiver -- Call
	        if(name.equalsIgnoreCase("video"))
	        {
		       ips = tokens.nextToken();
		            
			   String st="",pt="";
			   st += ips;
			   System.out.println("#########"+st);
		
				
			   String str[] = new String[2];
			   str[0]  =  st + "/20002";
			   str[1]  =  st + "/20004";
			   AVReceive2 avReceive= new AVReceive2(str);   
			   for(int o=1;o<st.length();o++)pt += st.charAt(o);
			   AVTransmit2 vt = new AVTransmit2(new MediaLocator("vfw://0"),pt,"20006",null);
			   AVTransmit2 at = new AVTransmit2(new MediaLocator("javasound://8000"),pt,"20008",null);
			   at.start();	
		       String result = vt.start();
		    
		 
				if (!avReceive.initialize())    
				 {   
				     System.err.println("Deshtoi inicialzimi i sesioneve.");   
				 }  
				 	
				 try    
				 {   
				     while (!avReceive.isDone())   
				         Thread.sleep(60000);   
				 }    
				 catch (Exception ex)    
				 {}  
		         	
	
		
			if (result != null) {
			    System.out.println("Error : " + result);
			}
			System.out.println("Fillo transmetimin per 60 sekonda...");
			 
			try {
			    Thread.currentThread().sleep(60000);
			} catch (InterruptedException ie) {
			}
		
			// Ndalo transmetimin
			vt.stop();
		   
		                    
	        } 
	        // Caller -- 
	        else if(name.equalsIgnoreCase("video1"))
	        {
	        
		       ips = tokens.nextToken();
		            
			   String st="",pt="";
			   st += ips;
			   System.out.println("*******"+st);
			   for(int o=1;o<st.length();o++)pt += st.charAt(o);
			  
			   AVTransmit2 vt = new AVTransmit2(new MediaLocator("vfw://0"),pt,"20002",null);
			   
			   AVTransmit2 at = new AVTransmit2(new MediaLocator("javasound://8000"),pt,"20004",null);
		       at.start();
		       String result = vt.start(); 
		       	if (result != null) {
		        System.out.println("Error : " + result);

		       }
		
		
		       System.out.println("Fillo transmetimin per 60 sekonda..."); 
		      			
			   String str[] = new String[2];
			   str[0]  =  st + "/20006";
			   str[1]  =  st + "/20008";
			   AVReceive2 avReceive= new AVReceive2(str);
	        
	        	 if (!avReceive.initialize())    
		         {   
		             System.err.println("Deshtoi inicializmi i sesioneve.");   
	 
		         }  
		         	
		         try    
		         {   
		             while (!avReceive.isDone())   
		                 Thread.sleep(60000);   
		         }    
		         catch (Exception ex)    
		         {}  
		         	
		         try {
	              Thread.currentThread().sleep(600000);
	             } catch (InterruptedException ie)
	             {
	             }
	
	         // Ndalo transmetimin
	            vt.stop();		
	        }  
    	}   
    	
    }
}
