package messengerclient;

import java.awt.*;
import javax.media.*;
import javax.media.protocol.*;
import javax.media.protocol.DataSource;
import javax.media.format.*;
import javax.media.control.TrackControl;
import javax.media.control.QualityControl;
import java.io.*;
//Kjo klase merret me Video transmetimin
public class VideoTransmit {

    // Input MediaLocator mund te jete nje file ose http ose capture source
    private MediaLocator locator;
    private String ipAddress;
    private String port;

    private Processor processor = null;
    private DataSink  rtptransmitter = null;
    private DataSource dataOutput = null;
    
    public VideoTransmit(MediaLocator locator,
			 String ipAddress,
			 String port) {
	
	this.locator = locator;
	this.ipAddress = ipAddress;
	this.port = port;
    }

    /**
     * Fillon transmetimin. Kthen null nese transmetimi ka filluar ne menyre te rregullt.
     * Perndryshe kthen nje string me arysen pse ky setup deshtoi.
     */
    public synchronized String start() {
	String result;

	// Krijon nje procesor per  media locator-in e caktuar 
	result = createProcessor();
	if (result != null)
	    return result;

	// Krijon nje RTP sesion per transmetimin e rezultatit te procesorit 
	// tek IP adresa dhe porti i caktuar 
	result = createTransmitter();
	if (result != null) {
	    processor.close();
	    processor = null;
	    return result;
	}

	//Fillo transmetimin
	processor.start();
	
	return null;
    }

    /**
     * Ndalon transmetimin nese vecse transmetimi ka filluar
     */
    public void stop() {
	synchronized (this) {
	    if (processor != null) {
		processor.stop();
		processor.close();
		processor = null;
		rtptransmitter.close();
		rtptransmitter = null;
	    }
	}
    }

    private String createProcessor() {
	if (locator == null)
	    return "Lokalizuesi eshte bosh";

	DataSource ds;
	DataSource clone;

	try {
	    ds = Manager.createDataSource(locator);
	} catch (Exception e) {
	    return "DataSource nuk mund te krijohet";
	}

	// Krijimi i nje procesori qe do te mirret me inputin e media locator 
	try {
	    processor = Manager.createProcessor(ds);
	} catch (NoProcessorException npe) {
	    return "Processor nuk mund te krijohet";
	} catch (IOException ioe) {
	    return "IOException creating processor";
	} 

	//Presim qe te konfigurohet
	boolean result = waitForState(processor, Processor.Configured);
	if (result == false)
	    return "Processor nuk mund te konfigurohet";

	// Merr track-at nga procesori 
	TrackControl [] tracks = processor.getTrackControls();

	// A kemi te pakten nje track?
	if (tracks == null || tracks.length < 1)
	    return "Track-at nuk mund te gjenden ne processor";

	boolean programmed = false;

	// Kerkon tek track-at per nje video track
	for (int i = 0; i < tracks.length; i++) {
	    Format format = tracks[i].getFormat();
	    if (  tracks[i].isEnabled() &&
		  format instanceof VideoFormat &&
		  !programmed) {
		
		//Gjetja e nje video track
		Dimension size = ((VideoFormat)format).getSize();
		float frameRate = ((VideoFormat)format).getFrameRate();
		int w = (size.width % 8 == 0 ? size.width :
				(int)(size.width / 8) * 8);
		int h = (size.height % 8 == 0 ? size.height :
				(int)(size.height / 8) * 8);
		VideoFormat jpegFormat = new VideoFormat(VideoFormat.JPEG_RTP,
							 new Dimension(w, h),
							 Format.NOT_SPECIFIED,
							 Format.byteArray,
							 frameRate);
		tracks[i].setFormat(jpegFormat);
		System.err.println("Transmetimi i video-s si:");
		System.err.println("  " + jpegFormat);
		programmed = true;
	    } else
		tracks[i].setEnabled(false);
	}

	if (!programmed)
	    return "Video track-i nuk mund te gjendet";

	// Caktimi i pershkruesit te permbajtjes tek RAW_RTP
	ContentDescriptor cd = new ContentDescriptor(ContentDescriptor.RAW_RTP);
	processor.setContentDescriptor(cd);

	result = waitForState(processor, Controller.Realized);
	if (result == false)
	    return "Processor nuk eshte i gatshem";

	// Caktimi i kualiteti te JPEG ne .5.
	setJPEGQuality(processor, 1.0f);

	dataOutput = processor.getDataOutput();
	return null;
    }

    // Krijon nje RTP transmit data sink. Kjo eshte menyra me e lehte per te krijuar
    // nje  RTP transmitter.Menyra tjeter eshte perdorimi i RTPSessionManager API.
    // Perdorimi i nje RTP session manager jep me shume kontroll nese kemi deshire
    // te permisojme transmetimin dhe te caktojme parametrat e tjere.
    private String createTransmitter() {

	String rtpURL = "rtp:/" + ipAddress + ":" + port + "/video";
	MediaLocator outputLocator = new MediaLocator(rtpURL);

	try {
	    rtptransmitter = Manager.createDataSink(dataOutput, outputLocator);
	    rtptransmitter.open();
	    rtptransmitter.start();
	    dataOutput.start();
	} catch (MediaException me) {
	    return "RTP data sink nuk mund te krijohet";
	} catch (IOException ioe) {
	    return "RTP data sink nuk mund te krijohet";
	}
	
	return null;
    }


    void setJPEGQuality(Player p, float val) {

	Control cs[] = p.getControls();
	QualityControl qc = null;
	VideoFormat jpegFmt = new VideoFormat(VideoFormat.JPEG);

	// Kontrollon ndermjet kontrollave per te gjetuar kontrollen e kualitetit per
 	// JPEG encoder-in.
	for (int i = 0; i < cs.length; i++) {

	    if (cs[i] instanceof QualityControl &&
		cs[i] instanceof Owned) {
		Object owner = ((Owned)cs[i]).getOwner();

		// Kontrollon nese owner eshte nje Codec.
		// Pastaj kontrrollon formatin e output-it.
		if (owner instanceof Codec) {
		    Format fmts[] = ((Codec)owner).getSupportedOutputFormats(null);
		    for (int j = 0; j < fmts.length; j++) {
			if (fmts[j].matches(jpegFmt)) {
			    qc = (QualityControl)cs[i];
	    		    qc.setQuality(val);
			    System.err.println("- Caktimi i kualitetit tek " + 
					val + " ne " + qc);
			    break;
			}
		    }
		}
		if (qc != null)
		    break;
	    }
	}
    }

    
    private Integer stateLock = new Integer(0);
    private boolean failed = false;
    
    Integer getStateLock() {
	return stateLock;
    }

    void setFailed() {
	failed = true;
    }
    
    private synchronized boolean waitForState(Processor p, int state) {
	p.addControllerListener(new StateListener());
	failed = false;

	if (state == Processor.Configured) {
	    p.configure();
	} else if (state == Processor.Realized) {
	    p.realize();
	}
	
	//Presim derisa marrim nje event qe konfirmon  
	// suksesin e nje metodeje dhe deshtimin e event-it.

	while (p.getState() < state && !failed) {
	    synchronized (getStateLock()) {
		try {
		    getStateLock().wait();
		} catch (InterruptedException ie) {
		    return false;
		}
	    }
	}

	if (failed)
	    return false;
	else
	    return true;
    }

    class StateListener implements ControllerListener {

	public void controllerUpdate(ControllerEvent ce) {

	    // Nese ka ndonje gabim gjate konfigurimit ose 
	    // procesori do te jete i mbyllur
	    if (ce instanceof ControllerClosedEvent)
		setFailed();

	    //Te gjitha eventet dergojne nje shenim
	    //te  thread-i qe pret ne waitForState metoden.
	    if (ce instanceof ControllerEvent) {
		synchronized (getStateLock()) {
		    getStateLock().notifyAll();
		}
	    }
	}
    }


  
}

