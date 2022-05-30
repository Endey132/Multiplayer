package data.scripts.net.connection;

import com.fs.starfarer.api.Global;
import data.scripts.net.connection.tcp.server.SocketServer;
import data.scripts.net.connection.udp.server.DatagramServer;
import data.scripts.net.io.PacketContainer;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Manages a collection of registered user connections
 */
public class ServerConnectionManager {
    private final int maxConnections = Global.getSettings().getInt("mpMaxConnections");

    private final static int PORT = Global.getSettings().getInt("mpLocalPort");

    private final DataDuplex dataDuplex;
    private boolean active;

    private final DatagramServer datagramServer;
    private final Thread datagram;

    private final SocketServer socketServer;
    private final Thread socket;

    private final List<ServerConnectionWrapper> serverConnectionWrappers;

    private int tick;

    public ServerConnectionManager() {
        dataDuplex = new DataDuplex();
        active = true;

        serverConnectionWrappers = new ArrayList<>();

        datagramServer = new DatagramServer(PORT, this);
        datagram = new Thread(datagramServer, "DATAGRAM_SERVER_THREAD");

        socketServer = new SocketServer(PORT, this);
        socket = new Thread(socketServer, "SOCKET_SERVER_THREAD");

        //socket.start();
        datagram.start();

        tick = 0;
    }

    public void update() {
        tick++;

        for (ServerConnectionWrapper connection : serverConnectionWrappers) {
            connection.update();
        }
    }

    public List<PacketContainer> getSocketMessages() throws IOException {
        List<PacketContainer> output = new ArrayList<>();

        for (ServerConnectionWrapper connection : serverConnectionWrappers) {
            PacketContainer message = connection.getSocketMessage();
            if (message != null) output.add(message);
        }

        return output;
    }

    public List<PacketContainer> getDatagrams() throws IOException {
        List<PacketContainer> output = new ArrayList<>();

        for (ServerConnectionWrapper connection : serverConnectionWrappers) {
            PacketContainer message = connection.getDatagram();
            if (message != null) output.add(message);
        }

        return output;
    }

    public ServerConnectionWrapper getNewConnection() {
        synchronized (serverConnectionWrappers) {
            if (serverConnectionWrappers.size() >= maxConnections) return null;
        }
        ServerConnectionWrapper serverConnectionWrapper = new ServerConnectionWrapper(this);

        synchronized (serverConnectionWrappers) {
            serverConnectionWrappers.add(serverConnectionWrapper);
        }

        return serverConnectionWrapper;
    }

    public void removeConnection(ServerConnectionWrapper serverConnectionWrapper) {
        synchronized (serverConnectionWrappers) {
            serverConnectionWrappers.remove(serverConnectionWrapper);
        }
    }

    public void stop() {
        socketServer.stop();
        datagramServer.stop();
        socket.interrupt();
        datagram.interrupt();
    }

    public int getTick() {
        return tick;
    }

    public synchronized List<ServerConnectionWrapper> getConnections() {
        return serverConnectionWrappers;
    }

    public synchronized DataDuplex getDuplex() {
        return dataDuplex;
    }

    public synchronized boolean isActive() {
        return active;
    }

    public synchronized void setActive(boolean active) {
        this.active = active;
    }
}