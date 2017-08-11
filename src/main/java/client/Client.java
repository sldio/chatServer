package client;

import service.Connection;
import service.ConsoleHelper;
import service.Message;
import service.MessageType;

import java.io.IOException;
import java.net.Socket;

/**
 * Created by А on 13.03.2017.
 */
    public class Client
{
    protected Connection connection;
    private volatile boolean clientConnected = false;

    public static void main(String[] args) {
        Client client = new Client();
        client.run();
    }

    protected String getServerAddress() {
        ConsoleHelper.writeMessage("Please write IP");
        return ConsoleHelper.readString();
    }

    protected int getServerPort() {
        ConsoleHelper.writeMessage("Please write port");
        return ConsoleHelper.readInt();
    }

    protected String getUserName() {
        ConsoleHelper.writeMessage("Please write your name");
        return ConsoleHelper.readString();
    }

    protected boolean shouldSendTextFromConsole() {
        return true;
    }

    protected SocketThread getSocketThread() {
        return new SocketThread();
    }

    protected void sendTextMessage(String text) {
        try
        {
            connection.send(new Message(MessageType.TEXT, text));
        }
        catch (IOException e)
        {
            ConsoleHelper.writeMessage("service.Connection error");
            clientConnected = false;
        }
    }

    public void run() {
        SocketThread socketThread = getSocketThread();
        socketThread.setDaemon(true);
        socketThread.start();
        try {
            synchronized (this) {
                this.wait();
            }
        } catch (InterruptedException e) {
            ConsoleHelper.writeMessage("Wait error");
            return;
        }
        if (clientConnected) {
            ConsoleHelper.writeMessage("Соединение установлено. Для выхода наберите команду 'exit'.");
        } else {
            ConsoleHelper.writeMessage("Произошла ошибка во время работы клиента.");
        }
        String message;
        while (clientConnected) {
            if (!(message = ConsoleHelper.readString()).equalsIgnoreCase("exit")) {
                if (shouldSendTextFromConsole()) sendTextMessage(message);
            } else
                break;
        }
    }

    public class SocketThread extends Thread {

        protected void processIncomingMessage(String message) //должен выводить текст message в консоль.
        {
            ConsoleHelper.writeMessage(message);
        }

        protected void informAboutAddingNewUser(String userName) //должен выводить в консоль информацию о том, что участник с именем userName присоединился к чату.
        {
            ConsoleHelper.writeMessage(userName + " added too chat");
        }

        protected void informAboutDeletingNewUser(String userName) //должен выводить в консоль, что участник с именем userName покинул чат.
        {
            ConsoleHelper.writeMessage(userName + " lived this chat");
        }

        protected void notifyConnectionStatusChanged(boolean clientConnected) {
            Client.this.clientConnected = clientConnected;

            synchronized (Client.this) {
                Client.this.notify();
            }
        }

        protected void clientHandshake() throws IOException, ClassNotFoundException {
            while (true)
            {
                Message message = connection.receive();

                try {
                    switch (message.getType()) {
                        case NAME_REQUEST:
                            Message m = new Message(MessageType.USER_NAME, getUserName());
                            connection.send(m);
                            break;
                        case NAME_ACCEPTED:
                            notifyConnectionStatusChanged(true);
                            return;
                        default: throw new IOException("Unexpected service.MessageType");
                    }
                }
                catch (Exception e)
                {
                    throw new IOException("Unexpected service.MessageType");
                }
            }
        }
        protected void clientMainLoop() throws IOException, ClassNotFoundException
        {

            while (true) {
                Message message = connection.receive();
                if (message.getType() == MessageType.TEXT) {
                    processIncomingMessage(message.getData());
                }
                else
                    if (message.getType() == MessageType.USER_ADDED)
                    {
                    informAboutAddingNewUser(message.getData());
                }
                else
                    if (message.getType() == MessageType.USER_REMOVED)
                    {
                    informAboutDeletingNewUser(message.getData());
                } else
                    {
                    throw new IOException("Unexpected service.MessageType");
                }
            }
        }

        @Override
        public void run()
        {
            String serverAdress = getServerAddress();
            int serverPort = getServerPort();
            try (Socket socket = new Socket(serverAdress,serverPort))
            {
                connection = new Connection(socket);
                clientHandshake();
                clientMainLoop();

            }
            catch (IOException | ClassNotFoundException e)
            {
                notifyConnectionStatusChanged(false);
            }

        }
    }
}

