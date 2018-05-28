package MessengSrserver;

import java.io.ObjectOutputStream;

//Interfejsi per pranimin e mesazheve
public interface MessageListener
{
    void sendInfo(String message);
}
