package service;

import java.io.Closeable;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.net.SocketAddress;

/**
 * Created by А on 10.03.2017.
 */
public class Connection implements Closeable
{
    private final Socket socket;
    private final ObjectOutputStream out;
    private final ObjectInputStream in;

    public Connection (Socket soket) throws IOException
    {
        this.socket = soket;
        try
        {
            this.out = new ObjectOutputStream(soket.getOutputStream());
            this.in = new ObjectInputStream(soket.getInputStream());
        }
        catch (IOException e)
        {
            throw new IOException();
        }
    }

    public void send(Message message) throws IOException
    {
        synchronized (out) {
            out.writeObject(message);
        }
    }

    public Message receive() throws IOException, ClassNotFoundException
    {
        synchronized (in) {
            Message message;
            message = (Message) in.readObject();
            return message;
        }
    }

    public SocketAddress getRemoteSocketAddress()
    {
        return socket.getRemoteSocketAddress();
    }
    public void close() throws IOException
    {
        in.close();
        out.close();
        socket.close();
    }
}
