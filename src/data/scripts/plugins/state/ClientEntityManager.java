package data.scripts.plugins.state;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.BaseEveryFrameCombatPlugin;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.input.InputEventAPI;
import data.scripts.net.data.packables.APackable;
import data.scripts.net.data.packables.ShipData;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Ships, Projectiles, Missiles, Fighters
 */
public class ClientEntityManager extends BaseEveryFrameCombatPlugin {
    private final Map<Integer, APackable> entities;

    public ClientEntityManager() {
        entities = new HashMap<>();
    }

    public void processDeltas(Map<Integer, APackable> toProcess) {
        // probably need to switch to Map<Integer, Map<Integer, ARecord<?>>> for performance/efficiency gain
        // to avoid so much null checking
        for (Integer key : toProcess.keySet()) {
            if (entities.containsKey(key)) {
                entities.get(key).updateFromDelta(toProcess.get(key));
            } else {
                entities.put(key, toProcess.get(key));
            }
        }


    }

    @Override
    public void advance(float amount, List<InputEventAPI> events) {
        for (APackable packable : entities.values()) {
            if (packable instanceof ShipData) {
                ShipData shipData = (ShipData) packable;

                ShipAPI ship = Global.getCombatEngine().getPlayerShip();

                ship.getLocation().set(shipData.getLoc().getRecord());
                ship.getVelocity().set(shipData.getVel().getRecord());
                ship.setFacing(shipData.getAng().getRecord());
                ship.setAngularVelocity(shipData.getAngVel().getRecord());
                ship.getFluxTracker().setCurrFlux(shipData.getFlux().getRecord() * ship.getFluxTracker().getMaxFlux());
                ship.setHitpoints(shipData.getHull().getRecord() * ship.getHullSpec().getHitpoints());
                ship.getMouseTarget().set(shipData.getCursor().getRecord());
            }
        }
    }
}
