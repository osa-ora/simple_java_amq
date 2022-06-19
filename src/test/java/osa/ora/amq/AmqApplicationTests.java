package osa.ora.amq;

import osa.ora.amq.beans.AMQSender;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class AmqApplicationTests {

    @Autowired
    private AMQSender sender;
    
    @Autowired
    private AMQController controller;

    @Test
    void contextLoads() {
    }

    @Test
    void testSendDirect() {
        sender.send("Test Send Msg direct!",AmqApplication.QUEUE_NAME);
    }
    
    @Test
    void testSend() {
        String results=controller.sendMessage("Test Send Msg by Controller!");
        assertNotNull(results);
    }
    @Test
    void testLoad() {
        String results=controller.getMessages();
        assertNotNull(results);
    }
    @Test
    void testReset() {
        String results=controller.reset();
        assertNotNull(results);
    }
}
