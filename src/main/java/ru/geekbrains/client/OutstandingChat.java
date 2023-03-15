package ru.geekbrains.client;

import ru.geekbrains.client.communication.ClientCommunicator;
import ru.geekbrains.client.gui.ChatFrame;

import java.util.function.Consumer;

public class OutstandingChat {


    private final ChatFrame frame;
    private final ClientCommunicator communicator;

    public OutstandingChat() {
        communicator = new ClientCommunicator();

        Consumer<String> outboundMessageConsumer = communicator::sendMessage;

        frame = new ChatFrame(outboundMessageConsumer);

        new Thread(() -> {
            while (true) {
                String inboundMessage = communicator.receiveMessage();
                frame.getInboundMessageConsumer().accept(inboundMessage);
            }
        })
                .start();
    }


}
