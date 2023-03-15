package ru.geekbrains.server;

import java.io.*;
import java.net.Socket;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Optional;
import java.util.Timer;
import java.util.TimerTask;

public class ClientHandler {


    private Server server;
    private DataInputStream in;
    private DataOutputStream out;
    private String name;
    private volatile boolean isAuthentificated;
    private BufferedWriter chatHistoryWriter;

    private File chatHistory;


    public ClientHandler(Server server, Socket socket) {
        try {
            this.server = server;
            in = new DataInputStream(socket.getInputStream());
            out = new DataOutputStream(socket.getOutputStream());



            server.executorService.execute (() -> {
                try {
                    server.executorService.execute( () -> {
                        terminatingIfNoAuthentication(socket);
                    });
                    doAuthentication();
                    if(isAuthentificated == true) {
                        chatHistoryWriter = new BufferedWriter(new FileWriter(chatHistory, true));
                    }
                    listenMessages();
                } catch (IOException e) {
                    e.printStackTrace();
                } finally {
                    server.unsubscribe(this);
                    server.broadcastMessage(String.format("User %s is out.", name));
                    closeConnection(socket);
                }
            });

        } catch (IOException e) {
            throw new RuntimeException("Something went wrong during client establishing...", e);
        }
    }

    private void closeConnection(Socket socket) {
        try {
            in.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        try {
            out.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        try {
            chatHistoryWriter.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        try {
            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public String getName() {
        return name;
    }

    private void doAuthentication() throws IOException {
        sendMessage("Система: Greeting you in the Outstanding Chat.");
        sendMessage("Система: Please do authentication. Template is: -auth login password");



        while (true) {
            String maybeCredentials = in.readUTF();
            /** sample: -auth login1 password1 */

            if (maybeCredentials.startsWith("-auth")) {
                String[] credentials = maybeCredentials.split("\\s");

                Optional<AuthService.User> maybeUser = server.getAuthService()
                        .findUserByLoginAndPassword(credentials[1], credentials[2]);

                if (maybeUser.isPresent()) {
                    AuthService.User user = maybeUser.get();
                    if (server.isNotUserOccupied(user.getName())) {
                        name = user.getName();
                        chatHistory = new File(String.format("E:\\Victor\\Программирование\\ДЗ Java разработчик Гик Брейнс\\history_%s.txt", name));
                        loadChatHistory();
                        sendMessage("Система: AUTH OK.");
                        sendMessage("Система: Welcome.");
                        sendMessage("Система: Отправить личное сообщение участнику чата: -w имя участника сообщение");
                        sendMessage("Система: Сменить имя в чате: -cname новое имя");
                        sendMessage("Система: Выйти из чата: -exit");
                        server.broadcastMessage(String.format("Система: User %s entered chat.", name));
                        server.subscribe(this);
                        isAuthentificated = true;
                        return;
                    } else {
                        sendMessage("Система: Current user is already logged in");
                    }
                } else {
                    sendMessage("Система: Invalid credentials.");
                }
            } else {
                sendMessage("Система: Invalid auth operation");
            }
        }


    }

    public void sendMessage(String outboundMessage) {
        try {
            out.writeUTF(outboundMessage);
            if(isAuthentificated == true) {
                chatHistoryWriter.write(outboundMessage + "\n");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void listenMessages() throws IOException {
        while (true) {
            String inboundMessage = in.readUTF();
            if (inboundMessage.equals("-exit")) {
                break;
            }
            if(inboundMessage.startsWith("-w")){
                String[] messageParts = inboundMessage.split("\\s");
                String receiverName = messageParts[1];
                String privateMessage = inboundMessage.substring(receiverName.length() + 4);
                server.broadcastPrivateMessage(privateMessage, receiverName, this);
                continue;
            }
            if(inboundMessage.startsWith("-cname")){
                String oldName = this.getName();
                changeName(inboundMessage);
                String newName = this.getName();
                server.broadcastMessage("Cистема: Пользователь " + oldName + " сменил имя. Его новое имя - " + newName);
                continue;
            }
            server.broadcastMessage(name + ": " + inboundMessage);
        }
    }


        public void terminatingIfNoAuthentication(Socket socket) {
                TimerTask terminateConnection = new TimerTask() {
                    @Override
                    public void run() {
                        if(isAuthentificated == false) {
                            sendMessage("Система: Вы не авторизовались в течение 2 минут.\nВаше соединение прервано");
                            closeConnection(socket);
                        }
                    }
                };

            Timer terminatingTimer = new Timer();

            terminatingTimer.schedule(terminateConnection, 120000);

        }

        public void changeName (String inboundMessage) {
            Connection connection = DatabaseConnection.getConnection();
            String newName = inboundMessage.split("\\s")[1];

            try {
                Statement statement = connection.createStatement();
                ResultSet rs = statement.executeQuery("SELECT * FROM users;");
                while(rs.next()){
                    if(rs.getString("name").equals(newName) || newName.equals("Система")){
                        sendMessage("Система: Желаемое имя занято. Повторите запрос с другим именем.");
                        DatabaseConnection.closeConnection(connection);
                        return;
                    }
                }
                String updateQuery = String.format("UPDATE users SET name = '%s' WHERE name = '%s';", newName, name);
                statement.executeUpdate(updateQuery);
                name = newName;
            } catch (SQLException e) {
                e.printStackTrace();
            } finally {
                DatabaseConnection.closeConnection(connection);
            }
        }

        public void loadChatHistory() {
            ArrayList<String> chatHistoryList = new ArrayList<>();

            if(chatHistory.exists()) {
                try(BufferedReader chatHistoryReader = new BufferedReader(new FileReader(chatHistory))){
                         String str = "";
                         while((str = chatHistoryReader.readLine()) != null){
                             chatHistoryList.add(str);
                         }

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            if(chatHistoryList != null || chatHistoryList.size() > 0){
                if(chatHistoryList.size() > 100){
                    for(int i = chatHistoryList.size() - 101; i < chatHistoryList.size() - 1; i++) {
                        sendMessage(chatHistoryList.get(i));
                    }
                } else {
                    for(int i = 0; i < chatHistoryList.size(); i++) {
                        sendMessage(chatHistoryList.get(i));
                    }
                }
            }
        }


}
