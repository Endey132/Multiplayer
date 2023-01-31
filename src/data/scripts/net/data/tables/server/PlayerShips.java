package data.scripts.net.data.tables.server;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.ShipAPI;
import data.scripts.net.data.packables.metadata.PlayerShipData;
import data.scripts.net.data.records.BaseRecord;
import data.scripts.net.data.tables.InboundEntityManager;
import data.scripts.net.data.util.DataGenManager;
import data.scripts.plugins.MPPlugin;

import java.util.HashMap;
import java.util.Map;

public class PlayerShips implements InboundEntityManager {

    private final Map<Short, PlayerShipData> playerShips = new HashMap<>();

    private final Map<PlayerShipData, String> IDTrackerMap = new HashMap<>();
    private final Map<String, ShipAPI> activeShips = new HashMap<>();

    public PlayerShips() {
    }

    @Override
    public void update(float amount, MPPlugin plugin) {
        for (PlayerShipData playerShipData : playerShips.values()) {
            playerShipData.update(amount, this);
        }

        for (PlayerShipData playerShipData : playerShips.values()) {
            String id = playerShipData.getPlayerShipID();

            if (id != null) {
                ShipAPI activeShip = activeShips.get(id);

                if (activeShip == null || !activeShip.getFleetMemberId().equals(id)) {
                    for (ShipAPI ship : Global.getCombatEngine().getShips()) {
                        if (ship.getFleetMemberId().equals(id)) {
                            activeShip = ship;

                            activeShips.put(id, activeShip);
                            IDTrackerMap.put(playerShipData, id);

                            break;
                        }
                    }
                }

                if (activeShip != null) {
                    playerShipData.unmask(activeShip, playerShipData.getControlBitmask());
                }
            } else {
                String s = IDTrackerMap.get(playerShipData);

                if (s != null) {
                    activeShips.remove(s);
                    IDTrackerMap.remove(playerShipData);
                }
            }
        }
    }

    @Override
    public void register() {
        DataGenManager.registerInboundEntityManager(PlayerShipData.TYPE_ID, this);
    }

    public Map<Short, PlayerShipData> getPlayerShips() {
        return playerShips;
    }

    @Override
    public void processDelta(byte typeID, short instanceID, Map<Byte, BaseRecord<?>> toProcess, MPPlugin plugin) {
        PlayerShipData data = playerShips.get(instanceID);

        if (data == null) {
            data = new PlayerShipData(instanceID, null);

            playerShips.put(instanceID, data);

            data.destExecute(toProcess);
            data.init(plugin, this);
        } else {
            data.destExecute(toProcess);
        }
    }

    public String getHostShipID() {
        return Global.getCombatEngine().getPlayerShip().getFleetMemberId();
    }
}