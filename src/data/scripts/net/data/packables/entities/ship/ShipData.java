package data.scripts.net.data.packables.entities.ship;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.*;
import com.fs.starfarer.api.fleet.FleetMemberAPI;
import com.fs.starfarer.api.fleet.FleetMemberType;
import com.fs.starfarer.combat.entities.Ship;
import data.scripts.net.data.packables.BasePackable;
import data.scripts.net.data.packables.DestExecute;
import data.scripts.net.data.packables.RecordLambda;
import data.scripts.net.data.packables.SourceExecute;
import data.scripts.net.data.records.*;
import data.scripts.plugins.MPClientPlugin;
import data.scripts.plugins.MPPlugin;
import data.scripts.plugins.ai.MPDefaultShipAIPlugin;
import org.lwjgl.util.vector.Vector2f;

import java.util.List;

public class ShipData extends BasePackable {
    public static byte TYPE_ID;

    private ShipAPI ship;
    private java.lang.String hullID;
    private java.lang.String fleetMemberID;
    private int owner;

    public ShipData(short instanceID, final ShipAPI ship) {
        super(instanceID);
        this.ship = ship;

        addRecord(new RecordLambda<>(
                StringRecord.getDefault().setDebugText("fleet member id"),
                new SourceExecute<java.lang.String>() {
                    @Override
                    public java.lang.String get() {
                        return ship.getFleetMemberId();
                    }
                },
                new DestExecute<java.lang.String>() {
                    @Override
                    public void execute(BaseRecord<java.lang.String> record, BasePackable packable) {
                        ((ShipData) packable).setFleetMemberID(record.getValue());
                    }
                }
        ));
        addRecord(new RecordLambda<>(
                StringRecord.getDefault().setDebugText("hullspec id"),
                new SourceExecute<java.lang.String>() {
                    @Override
                    public java.lang.String get() {
                        return ship.getHullSpec().getHullId();
                    }
                },
                new DestExecute<java.lang.String>() {
                    @Override
                    public void execute(BaseRecord<java.lang.String> record, BasePackable packable) {
                        ((ShipData) packable).setHullID(record.getValue());
                    }
                }
        ));
        addRecord(new RecordLambda<>(
                Vector2f32Record.getDefault().setUseDecimalPrecision(false).setDebugText("location"),
                new SourceExecute<Vector2f>() {
                    @Override
                    public Vector2f get() {
                        return ship.getLocation();
                    }
                },
                new DestExecute<Vector2f>() {
                    @Override
                    public void execute(BaseRecord<Vector2f> record, BasePackable packable) {
                        ShipData shipData = (ShipData) packable;
                        ShipAPI ship = shipData.getShip();
                        if (ship != null) ship.getLocation().set(record.getValue());
                    }
                }
        ));
        addRecord(new RecordLambda<>(
                Vector2f16Record.getDefault().setDebugText("velocity"),
                new SourceExecute<Vector2f>() {
                    @Override
                    public Vector2f get() {
                        return ship.getVelocity();
                    }
                },
                new DestExecute<Vector2f>() {
                    @Override
                    public void execute(BaseRecord<Vector2f> record, BasePackable packable) {
                        ShipData shipData = (ShipData) packable;
                        ShipAPI ship = shipData.getShip();
                        if (ship != null) ship.getVelocity().set(record.getValue());
                    }
                }
        ));
        addRecord(new RecordLambda<>(
                ByteRecord.getDefault().setDebugText("facing"),
                new SourceExecute<java.lang.Byte>() {
                    @Override
                    public java.lang.Byte get() {
                        return ConversionUtils.floatToByte(ship.getFacing(), 360f);
                    }
                },
                new DestExecute<java.lang.Byte>() {
                    @Override
                    public void execute(BaseRecord<java.lang.Byte> record, BasePackable packable) {
                        ShipData shipData = (ShipData) packable;
                        ShipAPI ship = shipData.getShip();
                        if (ship != null) ship.setFacing(ConversionUtils.byteToFloat(record.getValue(), 360f));
                    }
                }
        ));
        addRecord(new RecordLambda<>(
                Float16Record.getDefault().setDebugText("angular vel"),
                new SourceExecute<Float>() {
                    @Override
                    public Float get() {
                        return ship.getAngularVelocity();
                    }
                },
                new DestExecute<Float>() {
                    @Override
                    public void execute(BaseRecord<Float> record, BasePackable packable) {
                        ShipData shipData = (ShipData) packable;
                        ShipAPI ship = shipData.getShip();
                        if (ship != null) ship.setAngularVelocity(record.getValue());
                    }
                }
        ));
        addRecord(new RecordLambda<>(
                ByteRecord.getDefault().setDebugText("hitpoints"),
                new SourceExecute<java.lang.Byte>() {
                    @Override
                    public java.lang.Byte get() {
                        return ConversionUtils.floatToByte(ship.getHullLevel(), 1f);
                    }
                },
                new DestExecute<java.lang.Byte>() {
                    @Override
                    public void execute(BaseRecord<java.lang.Byte> record, BasePackable packable) {
                        ShipData shipData = (ShipData) packable;
                        ShipAPI ship = shipData.getShip();
                        if (ship != null) ship.setHitpoints(ship.getMaxHitpoints() * ConversionUtils.byteToFloat(record.getValue(), 1f));
                    }
                }
        ));
        addRecord(new RecordLambda<>(
                ByteRecord.getDefault().setDebugText("flux level"),
                new SourceExecute<java.lang.Byte>() {
                    @Override
                    public java.lang.Byte get() {
                        return ConversionUtils.floatToByte(ship.getFluxLevel(), 1f);
                    }
                },
                new DestExecute<java.lang.Byte>() {
                    @Override
                    public void execute(BaseRecord<java.lang.Byte> record, BasePackable packable) {
                        ShipData shipData = (ShipData) packable;
                        ShipAPI ship = shipData.getShip();
                        if (ship != null) ship.getFluxTracker().setCurrFlux(ship.getMaxFlux() * ConversionUtils.byteToFloat(record.getValue(), 1f));
                    }
                }
        ));
        addRecord(new RecordLambda<>(
                ByteRecord.getDefault().setDebugText("cr level"),
                new SourceExecute<java.lang.Byte>() {
                    @Override
                    public java.lang.Byte get() {
                        return ConversionUtils.floatToByte(ship.getCurrentCR(), 1f);
                    }
                },
                new DestExecute<java.lang.Byte>() {
                    @Override
                    public void execute(BaseRecord<java.lang.Byte> record, BasePackable packable) {
                        ShipData shipData = (ShipData) packable;
                        ShipAPI ship = shipData.getShip();
                        if (ship != null) ship.setCurrentCR(ConversionUtils.byteToFloat(record.getValue(), 1f));
                    }
                }
        ));
        addRecord(new RecordLambda<>(
                Vector2f32Record.getDefault().setUseDecimalPrecision(false).setDebugText("mouse target"),
                new SourceExecute<Vector2f>() {
                    @Override
                    public Vector2f get() {
                        return new Vector2f(ship.getMouseTarget());
                    }
                },
                new DestExecute<Vector2f>() {
                    @Override
                    public void execute(BaseRecord<Vector2f> record, BasePackable packable) {
                        ShipData shipData = (ShipData) packable;
                        ShipAPI ship = shipData.getShip();
                        if (ship != null) ship.getMouseTarget().set(record.getValue());
                    }
                }
        ));
        addRecord(new RecordLambda<>(
                ByteRecord.getDefault().setDebugText("fleet owner"),
                new SourceExecute<java.lang.Byte>() {
                    @Override
                    public java.lang.Byte get() {
                        return (byte) ship.getOwner();
                    }
                },
                new DestExecute<java.lang.Byte>() {
                    @Override
                    public void execute(BaseRecord<java.lang.Byte> record, BasePackable packable) {
                        ShipData shipData = (ShipData) packable;
                        ShipAPI ship = shipData.getShip();
                        if (ship != null) ship.setOwner(record.getValue());
                        shipData.setOwner(record.getValue());
                    }
                }
        ));
    }

    @Override
    public void init(MPPlugin plugin) {
        if (plugin.getType() != MPPlugin.PluginType.CLIENT) return;
        MPClientPlugin clientPlugin = (MPClientPlugin) plugin;

        CombatEngineAPI engine = Global.getCombatEngine();

        VariantData variantData = clientPlugin.getVariantDataMap().find(fleetMemberID);
        if (variantData == null) return;

        // update variant
        ShipHullSpecAPI hullSpec = Global.getSettings().getHullSpec(hullID);

        CombatFleetManagerAPI fleetManager = engine.getFleetManager(owner);

        if (hullSpec.getHullSize() != ShipAPI.HullSize.FIGHTER) {
            java.lang.String hullVariantId = hullID + "_Hull";
            ShipVariantAPI variant = Global.getSettings().createEmptyVariant(
                    hullVariantId,
                    hullSpec
            );

            int numCapacitors = variantData.getNumFluxCapacitors();
            variant.setNumFluxCapacitors(numCapacitors);
            int numVents = variantData.getNumFluxVents();
            variant.setNumFluxVents(numVents);

            List<java.lang.String> weaponSlots = variantData.getWeaponSlots();
            List<java.lang.String> weaponIds = variantData.getWeaponIDs();
            for (int i = 0; i < weaponSlots.size(); i++) {
                java.lang.String slot = weaponSlots.get(i);
                variant.addWeapon(slot, weaponIds.get(i));
            }

            variant.autoGenerateWeaponGroups();

            FleetMemberType fleetMemberType = FleetMemberType.SHIP;
            FleetMemberAPI fleetMember = Global.getFactory().createFleetMember(fleetMemberType, variant);

            fleetManager.addToReserves(fleetMember);

            fleetMember.getCrewComposition().setCrew(fleetMember.getHullSpec().getMaxCrew());

            ship = fleetManager.spawnFleetMember(fleetMember, new Vector2f(0f, 0f), 0f, 0f);
            ship.setCRAtDeployment(0.7f);
            ship.setControlsLocked(false);

            // set fleetmember id to sync with server
            Ship s = (Ship) ship;
            s.setFleetMemberId(fleetMemberID);
        } else {
            throw new NullPointerException("Attempted fighter init in ship data");
        }

        ship.setShipAI(new MPDefaultShipAIPlugin());
    }

    @Override
    public void update(float amount) {

    }

    @Override
    public void delete() {

    }

    @Override
    public byte getTypeID() {
        return TYPE_ID;
    }

    public ShipAPI getShip() {
        return ship;
    }

    public void setHullID(java.lang.String hullID) {
        this.hullID = hullID;
    }

    public void setFleetMemberID(java.lang.String fleetMemberID) {
        this.fleetMemberID = fleetMemberID;
    }

    public void setOwner(int owner) {
        this.owner = owner;
    }
}
