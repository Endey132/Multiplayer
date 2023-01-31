package data.scripts.net.data.tables.client;

import data.scripts.net.data.packables.metadata.PlayerShipData;
import data.scripts.net.data.records.BaseRecord;
import data.scripts.net.data.tables.OutboundEntityManager;
import data.scripts.net.data.util.DataGenManager;
import data.scripts.plugins.MPPlugin;

import java.util.HashMap;
import java.util.Map;

public class PlayerShip implements OutboundEntityManager {

    private final PlayerShipData playerShipData;
    private final short instanceID;

    private String playerShipID;

    public PlayerShip(short instanceID) {
        this.instanceID = instanceID;

        playerShipData = new PlayerShipData(instanceID, this);
    }

    @Override
    public void update(float amount, MPPlugin plugin) {
        playerShipData.update(amount, this);
    }

    @Override
    public void register() {
        DataGenManager.registerOutboundEntityManager(PlayerShipData.TYPE_ID, this);
    }

    @Override
    public Map<Short, Map<Byte, BaseRecord<?>>> getOutbound(byte typeID) {
        Map<Short, Map<Byte, BaseRecord<?>>> out = new HashMap<>();

        Map<Byte, BaseRecord<?>> deltas = playerShipData.sourceExecute();
        if (deltas != null && !deltas.isEmpty()) {
            out.put(instanceID, deltas);
        }

        return out;
    }

    @Override
    public PacketType getOutboundPacketType() {
        return PacketType.DATAGRAM;
    }

    public String getPlayerShipID() {
        return playerShipID;
    }

    public void setPlayerShipID(String playerShipID) {
        this.playerShipID = playerShipID;
    }
}