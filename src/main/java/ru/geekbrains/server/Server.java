package ru.geekbrains.server;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import ru.geekbrains.server.AuthService;
import ru.geekbrains.server.ClientHandler;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Server {


    private ServerSocket serverSocket;
    private final List<ClientHandler> loggedUser;
    private final AuthService authService;
    protected ExecutorService executorService;
    private static final Logger log = LogManager.getLogger(Server.class);


    public Server() {
        authService = new AuthService();
        loggedUser = new ArrayList<>();

        try {
            serverSocket = new ServerSocket(8888); // верный вариант: new ServerSocket(8888);
            executorService = Executors.newCachedThreadPool();
            log.info("Сервер запущен");
            while (true) {
                Socket socket = serverSocket.accept();
                new ClientHandler(this, socket);
            }
        } catch (IOException e) {
            log.error("Произошла ошибка: ", e);
        }

    }

    public AuthService getAuthService() {
        return authService;
    }

    public synchronized void subscribe(ClientHandler user) {
        loggedUser.add(user);
        log.info(user.getName() + " подключился");
    }

    public synchronized void unsubscribe(ClientHandler user) {
        loggedUser.remove(user);
        log.info(user.getName() + " отключился");
    }

    public synchronized boolean isNotUserOccupied(String name) {
        return !isUserOccupied(name);
    }

    public synchronized boolean isUserOccupied(String name) {
        /**
         for(ClineHandler user : loggedUser) {
         if (user.getName().equals(user)) {
         return true;
         }
         }
         return false;
         */

        /**
         loggedUser.stream()
         .filter(u -> u.getName().equals(u))
         .findFirst()
         .isPresent();
         */

        return loggedUser.stream()
                .anyMatch(u -> u.getName().equals(name));

    }

    public synchronized void broadcastMessage(String outboundMessage) {
        /**
         for (server.ClientHandler user: loggedUser) {
         user.sendMessage(outboundMessage);
         }
         */

        /**
         loggedUser.forEach(new Consumer<server.ClientHandler>() {
        @Override
        public void accept(server.ClientHandler clientHandler) {
        clientHandler.sendMessage(outboundMessage);
        }
        });
         */

        for (ClientHandler user: loggedUser) {
            String[] messageArray = outboundMessage.split(" ");
            String userName = messageArray[0].substring(0, messageArray[0].length() - 1);
            if(userName.equals(user.getName())) {
                user.sendMessage(user.getName() + " (Вы): " + outboundMessage.substring(userName.length() + 2));
                log.info(user.getName() + " прислал сообщение");
            } else {
                user.sendMessage(outboundMessage);
            }

        }

       // loggedUser.forEach(clientHandler -> clientHandler.sendMessage(clientHandler.getName() + ": " + outboundMessage));
    }

    public synchronized void broadcastPrivateMessage(String privateMessage, String receiverName, ClientHandler sender){
        log.info(sender.getName() + " прислал личное сообщение пользователю " + receiverName);
        for(ClientHandler user : loggedUser){
            if(user.getName().equals(receiverName)){
                user.sendMessage("Личное сообщение от " + sender.getName() + ": " + privateMessage);
                sender.sendMessage("Ваше личное сообщение пользователю " + receiverName + ": " + privateMessage);
                return;
            }
        }
        sender.sendMessage(receiverName + " в настоящий момент отсутствует в чате");
    }




}
