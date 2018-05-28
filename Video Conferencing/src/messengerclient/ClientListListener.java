
package messengerclient;

//Interface permban metodat per shtimin dhe largimin e perdoruesve
public interface ClientListListener
{
    void addToList(String userName);
    void removeFromList(String userName);
}
