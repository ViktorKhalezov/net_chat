package ru.geekbrains.client.communication;

import java.io.IOException;

public class ClientCommunicator {
    private final ClientConnector connector;

    public ClientCommunicator() {
        connector = new ClientConnector();
    }

    public void sendMessage(String outboundMessage) {
        try {
            connector.getOut().writeUTF(outboundMessage);
        } catch (IOException e) {
            throw new RuntimeException("Issue occurred while sending a message.", e);
        }
    }

    public String receiveMessage() {
        try {
            return connector.getIn().readUTF();
        } catch (IOException e) {
            throw new RuntimeException("Issue occurred while receiving a message.", e);
        }
    }

}
