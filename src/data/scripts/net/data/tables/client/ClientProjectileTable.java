package data.scripts.net.data.tables.client;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.DamagingProjectileAPI;
import data.scripts.net.data.DataGenManager;
import data.scripts.net.data.packables.entities.projectiles.BallisticProjectileData;
import data.scripts.net.data.packables.entities.projectiles.MissileData;
import data.scripts.net.data.packables.entities.projectiles.ProjectileData;
import data.scripts.net.data.packables.entities.projectiles.RayProjectileData;
import data.scripts.net.data.tables.InboundEntityManager;
import data.scripts.plugins.MPPlugin;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class ClientProjectileTable implements InboundEntityManager {

    private final Map<Short, RayProjectileData> movingRays = new HashMap<>();
    private final Map<Short, BallisticProjectileData> ballisticProjectiles = new HashMap<>();
    private final Map<Short, MissileData> missiles = new HashMap<>();

    public ClientProjectileTable() {
    }

    @Override
    public void update(float amount, MPPlugin plugin) {
        Set<DamagingProjectileAPI> p = new HashSet<>(Global.getCombatEngine().getProjectiles());

        for (RayProjectileData data : movingRays.values()) {
            if (data != null) {
                data.update(amount, this);
                data.interp(amount);

                p.remove(data.getProjectile());
            }
        }
        for (BallisticProjectileData data : ballisticProjectiles.values()) {
            if (data != null) {
                data.update(amount, this);
                data.interp(amount);

                p.remove(data.getProjectile());
            }
        }
        for (MissileData data : missiles.values()) {
            if (data != null) {
                data.update(amount, this);
                data.interp(amount);

                p.remove(data.getProjectile());
            }
        }

        for (DamagingProjectileAPI proj : p) {
            Global.getCombatEngine().removeEntity(proj);
        }
    }

    @Override
    public void processDelta(byte typeID, short instanceID, Map<Byte, Object> toProcess, MPPlugin plugin, int tick, byte connectionID) {
        if (typeID == RayProjectileData.TYPE_ID) {
            RayProjectileData data = movingRays.get(instanceID);

            if (data == null) {
                data = new RayProjectileData(instanceID, null, (short) -1, null);
                movingRays.put(instanceID, data);

                data.destExecute(toProcess, tick);

                data.init(plugin, this);
            } else {
                data.destExecute(toProcess, tick);
            }
        } else if (typeID == BallisticProjectileData.TYPE_ID) {
            BallisticProjectileData data = ballisticProjectiles.get(instanceID);

            if (data == null) {
                data = new BallisticProjectileData(instanceID, null, (short) -1, null);
                ballisticProjectiles.put(instanceID, data);

                data.destExecute(toProcess, tick);

                data.init(plugin, this);
            } else {
                data.destExecute(toProcess, tick);
            }
        } else if (typeID == MissileData.TYPE_ID) {
            MissileData data = missiles.get(instanceID);

            if (data == null) {
                data = new MissileData(instanceID, null, (short) -1, null);
                missiles.put(instanceID, data);

                data.destExecute(toProcess, tick);

                data.init(plugin, this);
            } else {
                data.destExecute(toProcess, tick);
            }
        }
    }

    @Override
    public void processDeletion(byte typeID, short instanceID, MPPlugin plugin, int tick, byte connectionID) {
        if (typeID == RayProjectileData.TYPE_ID) {
            RayProjectileData data = movingRays.get(instanceID);
            if (data != null) data.delete();
            movingRays.remove(instanceID);
        } else if (typeID == BallisticProjectileData.TYPE_ID) {
            BallisticProjectileData data = ballisticProjectiles.get(instanceID);
            if (data != null) data.delete();
            ballisticProjectiles.remove(instanceID);
        } else if (typeID == MissileData.TYPE_ID) {
            MissileData data = missiles.get(instanceID);
            if (data != null) data.delete();
            missiles.remove(instanceID);
        }
    }


    @Override
    public void register() {
        DataGenManager.registerInboundEntityManager(ProjectileData.TYPE_ID, this);
    }
}
