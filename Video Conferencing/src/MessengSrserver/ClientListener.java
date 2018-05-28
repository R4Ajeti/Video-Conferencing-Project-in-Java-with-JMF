package MessengSrserver;

//Interfejs per pranimin e klienteve
public interface ClientListener
{
    void signIn(String userName);
    void signOut(String userNamme);
    void clientStatus(String status);
    void mapped(String nam,String ip);
}
