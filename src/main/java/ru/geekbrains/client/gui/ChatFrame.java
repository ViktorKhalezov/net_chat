package ru.geekbrains.client.gui;

import javax.swing.*;
import java.awt.*;
import java.util.function.Consumer;

public class ChatFrame {


    private final JFrame mainFrame;
    private JTextArea chattingArea;
    private JButton submitButton;
    private final Consumer<String> inboundMessageConsumer;
    private final Consumer<String> outboundMessageConsumer;

    public ChatFrame(Consumer<String> outboundMessageConsumer) {
        this.outboundMessageConsumer = outboundMessageConsumer;
        inboundMessageConsumer = createInboundMessageConsumer();

        mainFrame = new JFrame();

        mainFrame.setTitle("Outstanding chat v1.0");
        mainFrame.setBounds(new Rectangle(400, 700));
        mainFrame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        mainFrame.setLayout(new BorderLayout());

        mainFrame.add(createTop(), BorderLayout.CENTER);
        mainFrame.add(createBottom(), BorderLayout.SOUTH);

        mainFrame.setVisible(true);
    }

    public Consumer<String> getInboundMessageConsumer() {
        return inboundMessageConsumer;
    }

    private Consumer<String> createInboundMessageConsumer() {
        return inboundMessage -> {
            chattingArea.append(inboundMessage);
            chattingArea.append("\n");
        };
    }

    private JPanel createTop() {
        JPanel jPanel = new JPanel();
        jPanel.setLayout(new BorderLayout());

        chattingArea = new JTextArea();
        chattingArea.setEditable(false);
        jPanel.add(chattingArea, BorderLayout.CENTER);

        return jPanel;
    }

    private JPanel createBottom() {
        JPanel jPanel = new JPanel();
        jPanel.setLayout(new BorderLayout());

        JTextField inputArea = new JTextField();
        submitButton = new JButton("Submit");

        submitButton.addActionListener(event -> {
            String text = inputArea.getText();
            outboundMessageConsumer.accept(text);
        });

        jPanel.add(inputArea, BorderLayout.CENTER);
        jPanel.add(submitButton, BorderLayout.EAST);

        return jPanel;
    }


}
