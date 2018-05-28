package messengerclient;

//Interface qe permban metodat per menaxhimit e dritares se klientit
public interface ClientWindowListener
{
    public void openWindow(String message);
    public void closeWindow(String message);
    public void fileStatus(String filesStatus);
}
