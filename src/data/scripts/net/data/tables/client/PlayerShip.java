package data.scripts.net.data.tables.client;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.ShipAPI;
import data.scripts.net.data.DataGenManager;
import data.scripts.net.data.packables.metadata.PlayerShipData;
import data.scripts.net.data.records.DataRecord;
import data.scripts.net.data.tables.OutboundEntityManager;
import data.scripts.plugins.MPPlugin;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class PlayerShip implements OutboundEntityManager {

    private final PlayerShipData playerShipData;
    private final short instanceID;

    private String playerShipID;
    private String playerShipIDPrev;
    private ShipAPI playerShip;

    public PlayerShip(short instanceID) {
        this.instanceID = instanceID;

        playerShipData = new PlayerShipData(instanceID, this);
    }

    @Override
    public void update(float amount, MPPlugin plugin) {
        playerShipData.update(amount, this);

        if (playerShipID != null && playerShipIDPrev == null || playerShipID != null && !playerShipIDPrev.equals(playerShipID)) {
            for (ShipAPI ship : Global.getCombatEngine().getShips()) {
                if (ship.getFleetMemberId().equals(playerShipID)) {
                    Global.getCombatEngine().setPlayerShipExternal(ship);
                    playerShip = ship;
                    break;
                }
            }
        }

        playerShipIDPrev = playerShipID;

        if (playerShip != null) {
//            playerShip.blockCommandForOneFrame(ShipCommand.FIRE);
        }
    }

    @Override
    public void register() {
        DataGenManager.registerOutboundEntityManager(PlayerShipData.TYPE_ID, this);
    }

    @Override
    public Map<Short, Map<Byte, DataRecord<?>>> getOutbound(byte typeID) {
        Map<Short, Map<Byte, DataRecord<?>>> out = new HashMap<>();

        Map<Byte, DataRecord<?>> deltas = playerShipData.sourceExecute();
        if (deltas != null && !deltas.isEmpty()) {
            out.put(instanceID, deltas);
        }

        return out;
    }

    @Override
    public Set<Short> getDeleted(byte typeID) {
        return null;
    }

    @Override
    public PacketType getOutboundPacketType() {
        return PacketType.DATAGRAM;
    }

    public String getPlayerShipID() {
        return playerShipID;
    }

    public ShipAPI getPlayerShip() {
        return playerShip;
    }

    public void setPlayerShipID(String playerShipID) {
        this.playerShipID = playerShipID;
    }
}
