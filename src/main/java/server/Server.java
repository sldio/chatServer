package server;

import service.Connection;
import service.ConsoleHelper;
import service.Message;
import service.MessageType;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by А on 10.03.2017.
 */
public class Server {

    private static Map<String, Connection> connectionMap = new ConcurrentHashMap<>();

    private static class Handler extends Thread
    {
        private Socket socket;

        public Handler(Socket socket)
        {
            this.socket = socket;
        }
        private String serverHandshake(Connection connection) throws IOException, ClassNotFoundException
        {
            while (true)
            {
                connection.send(new Message(MessageType.NAME_REQUEST));
                Message receiveMessage = connection.receive();
                if (receiveMessage.getType() == MessageType.USER_NAME)
                {
                    String clientName = receiveMessage.getData();
                    if (!clientName.isEmpty() && clientName != null && !connectionMap.containsKey(clientName))
                    {
                        connectionMap.put(clientName, connection);
                        connection.send(new Message(MessageType.NAME_ACCEPTED));
                        return clientName;
                    }
                }
            }
        }
        private void sendListOfUsers(Connection connection, String userName) throws IOException
        {
            for (Map.Entry<String, Connection> iterator: connectionMap.entrySet())
            {
                if (iterator.getKey().equals(userName))
                    continue;
                connection.send(new Message(MessageType.USER_ADDED, iterator.getKey()));
            }
        }
        private void serverMainLoop(Connection connection, String userName) throws IOException, ClassNotFoundException
        {
            while (true)
            {
                Message messageFromUser = connection.receive();
                if (MessageType.TEXT.equals(messageFromUser.getType()))
                {
                    String str = userName + ": " + messageFromUser.getData();
                    sendBroadcastMessage(new Message(MessageType.TEXT,str));
                }
                else
                {
                    System.out.println("wrong messege");
                }
            }
        }

        public void run()
        {
            String userName = null;
            ConsoleHelper.writeMessage("Соединение установлено с удаленным адресом " + socket.getRemoteSocketAddress());
            try (Connection connection = new Connection(socket))
            {
                userName = serverHandshake(connection);
                sendBroadcastMessage(new Message(MessageType.USER_ADDED,userName));
                sendListOfUsers(connection,userName);
                serverMainLoop(connection,userName);
            }
            catch (IOException | ClassNotFoundException e)
            {
                ConsoleHelper.writeMessage("произошла ошибка при обмене данными с удаленным адресом");
            }
            if (userName != null && connectionMap.containsKey(userName)) {
                connectionMap.remove(userName);
                sendBroadcastMessage(new Message(MessageType.USER_REMOVED, userName));
            }
            ConsoleHelper.writeMessage("service.Connection closed");
        }
    }
    public static void sendBroadcastMessage(Message message)
    {
        for (Map.Entry<String, Connection> iterator: connectionMap.entrySet())
        {
            try
            {
                iterator.getValue().send(message);
            }
            catch (IOException e)
            {
                ConsoleHelper.writeMessage("ошибка отправки сообщения от " + iterator.getKey());
            }
        }
    }

    public static void main (String[] args)

    {
        ServerSocket serverSocket = null;
        try {
            int port = ConsoleHelper.readInt();
            serverSocket = new ServerSocket(port);
            ConsoleHelper.writeMessage("Сервер запущен!");
            while (true) {
                Socket socket = serverSocket.accept();
                Handler handler = new Handler(socket);
                handler.start();
            }
        } catch (Exception e) {
            try {
                serverSocket.close();
            } catch (Exception e1) {
                e1.printStackTrace();
            }
        }
    }
}
