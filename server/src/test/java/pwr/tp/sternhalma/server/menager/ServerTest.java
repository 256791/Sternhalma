package pwr.tp.sternhalma.server.menager;

import static org.junit.Assert.*;
import org.junit.Test;

public class ServerTest{
    @Test
    public void tastExists(){
        Server instance = new Server();
        assertNotNull(instance);
    }
}