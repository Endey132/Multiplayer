package data.scripts.net.io;

import cmu.CMUtils;
import data.scripts.net.data.BaseRecord;
import data.scripts.net.data.SourcePackable;
import data.scripts.net.data.packables.metadata.connection.ConnectionIDs;
import data.scripts.net.data.packables.metadata.connection.ConnectionSource;
import data.scripts.net.data.tables.InboundEntityManager;
import data.scripts.net.data.tables.OutboundEntityManager;
import data.scripts.net.data.util.DataGenManager;
import data.scripts.net.io.tcp.client.SocketClient;
import data.scripts.net.io.udp.client.DatagramClient;
import data.scripts.plugins.MPPlugin;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Manages switching logic for inputting/sending data
 */
public class ClientConnectionWrapper extends BaseConnectionWrapper implements InboundEntityManager, OutboundEntityManager {
    private final DataDuplex dataDuplex;

    private DatagramClient datagramClient;
    private Thread datagram;

    private final SocketClient socketClient;
    private final Thread socket;
    private final String host;

    private int tick;

    public ClientConnectionWrapper(String host, int port, MPPlugin plugin) {
        super(plugin);

        this.host = host;
        dataDuplex = new DataDuplex();

        socketClient = new SocketClient(host, port, this);
        socket = new Thread(socketClient, "SOCKET_CLIENT_THREAD");
        socket.start();

        tick = -1;
    }

    @Override
    public PacketContainer getSocketMessage() throws IOException {
        if (statusData == null) {
            InetSocketAddress address = socketClient.getLocal();
            if (address == null) return null;

            statusData = new ConnectionSource(ConnectionIDs.getConnectionId(address), this);
        }

        List<SourcePackable> data = new ArrayList<>();
        switch (connectionState) {
            case INITIALISATION_READY:
            //case INITIALISING:
                CMUtils.getGuiDebug().putText(ClientConnectionWrapper.class, "debug", "initialising connection...");

                connectionState = ConnectionState.LOADING_READY;

                break;
            case LOADING_READY:
            //case LOADING:
                CMUtils.getGuiDebug().putText(ClientConnectionWrapper.class, "debug", "Receiving data over socket...");

                connectionState = ConnectionState.SPAWNING_READY;

                break;
            case SPAWNING_READY:
            //case SPAWNING:
                CMUtils.getGuiDebug().putText(ClientConnectionWrapper.class, "debug", "Spawning entities...");

                connectionState = ConnectionState.SIMULATION_READY;

                break;
            case SIMULATION_READY:
            //case SIMULATING:
                CMUtils.getGuiDebug().putText(ClientConnectionWrapper.class, "debug", "Starting simulation...");

                connectionState = ConnectionState.SIMULATING;

                if (datagramClient == null) startDatagramClient();

                break;
            default:
                break;
        }

        data.add(statusData);

        return new PacketContainer(data, tick, true, null, socketBuffer);
    }

    private void startDatagramClient() {
        datagramClient = new DatagramClient(host, ((InetSocketAddress) socketClient.getChannel().localAddress()).getPort(), this);
        datagram = new Thread(datagramClient, "DATAGRAM_CLIENT_THREAD");
        datagram.start();
    }

    @Override
    public PacketContainer getDatagram() throws IOException {
        if (statusData == null) return null;

        List<SourcePackable> data = new ArrayList<>();
        switch (connectionState) {
            case INITIALISATION_READY:
            case INITIALISING:
            case LOADING_READY:
            case LOADING:
            case SIMULATION_READY:
            case SPAWNING_READY:
            case SPAWNING:
                break;
            case SIMULATING:
                for (Map<Integer, SourcePackable> type : dataDuplex.getOutboundDatagram().values()) {
                    data.addAll(type.values());
                }

                break;
            case CLOSED:
            default:
                break;
        }

        return new PacketContainer(data, tick, false, null, datagramBuffer);
    }

    public void updateInbound(Map<Integer, Map<Integer, Map<Integer, BaseRecord<?>>>> entities, int tick) {
        this.tick = tick;
        dataDuplex.updateInbound(entities);
    }

    public DataDuplex getDuplex() {
        return dataDuplex;
    }

    public synchronized ConnectionState getConnectionState() {
        return connectionState;
    }

    public synchronized void setConnectionState(ConnectionState connectionState) {
        this.connectionState = connectionState;
    }

    public void stop() {
        socketClient.stop();
        if (datagramClient != null) datagramClient.stop();
        socket.interrupt();
        if (datagram != null) datagram.interrupt();
    }

    public int getTick() {
        return tick;
    }

    @Override
    public void processDelta(int id, Map<Integer, BaseRecord<?>> toProcess, MPPlugin plugin) {
        int state = (int) toProcess.get(ConnectionIDs.STATE).getValue();
        if (state < connectionState.ordinal()) {
            //return;
        }

        statusData.updateFromDelta(toProcess);
        connectionState = BaseConnectionWrapper.ordinalToConnectionState(state);
    }

    @Override
    public void update(float amount) {

    }

    @Override
    public Map<Integer, SourcePackable> getOutbound() {
        Map<Integer, SourcePackable> out = new HashMap<>();
        out.put(connectionId, statusData);
        return out;
    }

    @Override
    public void register() {
        DataGenManager.registerInboundEntityManager(ConnectionIDs.TYPE_ID, this);
        DataGenManager.registerOutboundEntityManager(ConnectionIDs.TYPE_ID, this);
    }

    @Override
    public PacketType getPacketType() {
        return PacketType.SOCKET;
    }
}
