package data.scripts.net.data.packables.metadata;

import cmu.drones.ai.DroneAIUtils;
import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.*;
import data.scripts.net.data.packables.DestExecute;
import data.scripts.net.data.packables.EntityData;
import data.scripts.net.data.packables.RecordLambda;
import data.scripts.net.data.packables.SourceExecute;
import data.scripts.net.data.packables.entities.ships.ShipData;
import data.scripts.net.data.records.ByteRecord;
import data.scripts.net.data.records.IntRecord;
import data.scripts.net.data.records.ShortRecord;
import data.scripts.net.data.records.Vector2f32Record;
import data.scripts.net.data.tables.BaseEntityManager;
import data.scripts.net.data.tables.InboundEntityManager;
import data.scripts.net.data.tables.client.combat.player.PlayerShip;
import data.scripts.net.data.tables.server.combat.entities.ShipTable;
import data.scripts.plugins.MPPlugin;
import data.scripts.plugins.ai.MPDefaultShipAIPlugin;
import org.lazywizard.lazylib.VectorUtils;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;
import org.lwjgl.util.vector.Vector2f;

import java.util.List;

/**
 * Sends player ship commands to the server
 */
public class PlayerShipData extends EntityData {

    public static byte TYPE_ID;

    private int controlBitmask;
    private short playerShipID;
    private byte playerShipFlags;
    private short requestedShipID = -1;
    private short requestedShipIDPrev = -1;

    private ShipAPI playerShip;
    private ShipAIPlugin aiPlugin;
    private boolean isAccepted;
    private boolean isRejected;

    private final Vector2f mouseTarget = new Vector2f(0f, 0f);

    private boolean shieldEnable = false;
    private boolean prevShields = false;
    private boolean fighterEnable = false;
    private boolean prevFighters = false;

    // make use of the CMUtils pd controller functionality to imitate the point-at-cursor pilot mode
    private final DroneAIUtils.PDControl control = new DroneAIUtils.PDControl() {
        @Override
        public float getKp() {
            return 0;
        }

        @Override
        public float getKd() {
            return 0;
        }

        @Override
        public float getRp() {
            return 0.1f;
        }

        @Override
        public float getRd() {
            return 0.05f;
        }
    };

    /**
     * Source constructor
     *
     * @param instanceID unique
     */
    public PlayerShipData(short instanceID, final PlayerShip playerShip) {
        super(instanceID);

        addRecord(new RecordLambda<>(
                IntRecord.getDefault().setDebugText("control bitmask"),
                new SourceExecute<Integer>() {
                    @Override
                    public Integer get() {
                        return mask();
                    }
                },
                new DestExecute<Integer>() {
                    @Override
                    public void execute(Integer value, EntityData packable) {
                        PlayerShipData playerShipData = (PlayerShipData) packable;
                        playerShipData.setControlBitmask(value);
                    }
                }
        ));
        addRecord(new RecordLambda<>(
                ShortRecord.getDefault().setDebugText("player ship id"),
                new SourceExecute<Short>() {
                    @Override
                    public Short get() {
                        return playerShip.getActiveShipID();
                    }
                },
                new DestExecute<Short>() {
                    @Override
                    public void execute(Short value, EntityData packable) {
                        setPlayerShipID(value);
                    }
                }
        ));
        addRecord(new RecordLambda<>(
                ShortRecord.getDefault().setDebugText("requested switch ship id"),
                new SourceExecute<Short>() {
                    @Override
                    public Short get() {
                        return playerShip.getRequestedShipID();
                    }
                },
                new DestExecute<Short>() {
                    @Override
                    public void execute(Short value, EntityData packable) {
                        setRequestedShipID(value);
                    }
                }
        ));
        addRecord(new RecordLambda<>(
                ByteRecord.getDefault().setDebugText("ship switch request flags"),
                new SourceExecute<Byte>() {
                    @Override
                    public Byte get() {
                        byte b = 0x00;



                        return b;
                    }
                },
                new DestExecute<Byte>() {
                    @Override
                    public void execute(Byte value, EntityData packable) {
                        setPlayerShipFlags(value);
                    }
                }
        ));
        addRecord(new RecordLambda<>(
                Vector2f32Record.getDefault().setDebugText("player mouse target"),
                new SourceExecute<Vector2f>() {
                    @Override
                    public Vector2f get() {
                        Vector2f m = new Vector2f(Mouse.getX(), Mouse.getY());
                        ViewportAPI v = Global.getCombatEngine().getViewport();
                        m.x = v.convertScreenXToWorldX(m.x);
                        m.y = v.convertScreenYToWorldY(m.y);
                        return m;
                    }
                },
                new DestExecute<Vector2f>() {
                    @Override
                    public void execute(Vector2f value, EntityData packable) {
                        setMouseTarget(value);
                    }
                }
        ));
    }

    @Override
    public void init(MPPlugin plugin, InboundEntityManager manager) {

    }

    @Override
    public void update(float amount, BaseEntityManager manager, MPPlugin plugin) {
        if (playerShip == null && plugin.getType() == MPPlugin.PluginType.SERVER) {
            check((ShipTable) plugin.getEntityManagers().get(ShipTable.class));
        }

        if (playerShip != null) {
            if (plugin.getType() == MPPlugin.PluginType.SERVER) {
                unmask(playerShip, controlBitmask, amount);
            } else {
                playerShip.blockCommandForOneFrame(ShipCommand.TOGGLE_SHIELD_OR_PHASE_CLOAK);
            }
        }

        boolean shieldCheck = Mouse.isButtonDown(1);
        if (shieldCheck && !prevShields) shieldEnable = !shieldEnable;
        prevShields = shieldCheck;

        boolean fighterCheck = Keyboard.isKeyDown(Keyboard.getKeyIndex(Global.getSettings().getControlStringForEnumName("SHIP_PULL_BACK_FIGHTERS")));
        if (fighterCheck && !prevFighters) fighterEnable = !fighterEnable;
        prevFighters = fighterCheck;
    }

    public void transferPlayerShip(ShipAPI dest) {
        if (this.playerShip != null && aiPlugin != null) {
            playerShip.setShipAI(aiPlugin);
            aiPlugin.forceCircumstanceEvaluation();
        }

        playerShip = dest;
        aiPlugin = playerShip.getShipAI();
        playerShip.setShipAI(new MPDefaultShipAIPlugin());
    }

    private void check(ShipTable shipTable) {
        for (ShipData data : shipTable.getTable()) {
            if (data != null && data.getShip() != null) {
                ShipAPI ship = data.getShip();

                if (data.getInstanceID() == playerShipID) {
                    ship.setShipAI(new MPDefaultShipAIPlugin());
                    data.setControlOverride(new ShipControlOverride(this) {
                        @Override
                        public void control(ShipAPI ship) {
                            ship.getMouseTarget().set(mouseTarget);
                        }
                    });
                    playerShip = ship;
                }
            }
        }
    }

    @Override
    public void delete() {

    }

    @Override
    public byte getTypeID() {
        return TYPE_ID;
    }

    public ShipAPI getPlayerShip() {
        return playerShip;
    }

    public int getControlBitmask() {
        return controlBitmask;
    }

    public void setControlBitmask(int controlBitmask) {
        this.controlBitmask = controlBitmask;
    }

    public short getPlayerShipID() {
        return playerShipID;
    }

    public void setPlayerShipID(short playerShipID) {
        this.playerShipID = playerShipID;
    }

    public short getRequestedShipID() {
        return requestedShipID;
    }

    public byte getPlayerShipFlags() {
        return playerShipFlags;
    }

    public void setPlayerShipFlags(byte playerShipFlags) {
        this.playerShipFlags = playerShipFlags;
    }

    public void setRequestedShipID(short requestedShipID) {
        if (requestedShipIDPrev == requestedShipID) {
            this.requestedShipID = -1;
        } else {
            this.requestedShipID = requestedShipID;
        }

        requestedShipIDPrev = requestedShipID;
    }

    public int mask() {
        boolean[] controls = new boolean[Integer.SIZE];

        if (!Keyboard.isCreated()) return 0x00000000;

        controls[0] = Keyboard.isKeyDown(Keyboard.getKeyIndex(Global.getSettings().getControlStringForEnumName("SHIP_ACCELERATE")));
        controls[1] = Keyboard.isKeyDown(Keyboard.getKeyIndex(Global.getSettings().getControlStringForEnumName("SHIP_ACCELERATE_BACKWARDS")));
        controls[2] = Keyboard.isKeyDown(Keyboard.getKeyIndex(Global.getSettings().getControlStringForEnumName("SHIP_TURN_LEFT")));
        controls[3] = Keyboard.isKeyDown(Keyboard.getKeyIndex(Global.getSettings().getControlStringForEnumName("SHIP_TURN_RIGHT")));
        controls[4] = Keyboard.isKeyDown(Keyboard.getKeyIndex(Global.getSettings().getControlStringForEnumName("SHIP_DECELERATE")));

        controls[5] = Keyboard.isKeyDown(Keyboard.KEY_LSHIFT);

        controls[6] = Keyboard.isKeyDown(Keyboard.getKeyIndex(Global.getSettings().getControlStringForEnumName("SHIP_STRAFE_LEFT_NOTURN")));
        controls[7] = Keyboard.isKeyDown(Keyboard.getKeyIndex(Global.getSettings().getControlStringForEnumName("SHIP_STRAFE_RIGHT_NOTURN")));
        controls[8] = Keyboard.isKeyDown(Keyboard.getKeyIndex(Global.getSettings().getControlStringForEnumName("SHIP_USE_SYSTEM")));

//        controls[9] = Keyboard.isKeyDown(Keyboard.getKeyIndex(Global.getSettings().getControlStringForEnumName("SHIP_SHIELDS")));

        controls[9] = shieldEnable;

        controls[10] = Mouse.isButtonDown(0);

        controls[11] = Keyboard.isKeyDown(Keyboard.getKeyIndex(Global.getSettings().getControlStringForEnumName("SHIP_VENT_FLUX")));
        controls[12] = Keyboard.isKeyDown(Keyboard.getKeyIndex(Global.getSettings().getControlStringForEnumName("SHIP_HOLD_FIRE")));

        controls[13] = fighterEnable;

        controls[14] = Keyboard.isKeyDown(Keyboard.getKeyIndex(Global.getSettings().getControlStringForEnumName("SHIP_SELECT_GROUP_1")));
        controls[15] = Keyboard.isKeyDown(Keyboard.getKeyIndex(Global.getSettings().getControlStringForEnumName("SHIP_SELECT_GROUP_2")));
        controls[16] = Keyboard.isKeyDown(Keyboard.getKeyIndex(Global.getSettings().getControlStringForEnumName("SHIP_SELECT_GROUP_3")));
        controls[17] = Keyboard.isKeyDown(Keyboard.getKeyIndex(Global.getSettings().getControlStringForEnumName("SHIP_SELECT_GROUP_4")));
        controls[18] = Keyboard.isKeyDown(Keyboard.getKeyIndex(Global.getSettings().getControlStringForEnumName("SHIP_SELECT_GROUP_5")));
        controls[19] = Keyboard.isKeyDown(Keyboard.getKeyIndex(Global.getSettings().getControlStringForEnumName("SHIP_SELECT_GROUP_6")));
        controls[20] = Keyboard.isKeyDown(Keyboard.getKeyIndex(Global.getSettings().getControlStringForEnumName("SHIP_SELECT_GROUP_7")));

        if (Keyboard.isKeyDown(Keyboard.KEY_LCONTROL)) {
            controls[21] = Keyboard.isKeyDown(Keyboard.getKeyIndex(Global.getSettings().getControlStringForEnumName("SHIP_SELECT_GROUP_1")));
            controls[22] = Keyboard.isKeyDown(Keyboard.getKeyIndex(Global.getSettings().getControlStringForEnumName("SHIP_SELECT_GROUP_2")));
            controls[23] = Keyboard.isKeyDown(Keyboard.getKeyIndex(Global.getSettings().getControlStringForEnumName("SHIP_SELECT_GROUP_3")));
            controls[24] = Keyboard.isKeyDown(Keyboard.getKeyIndex(Global.getSettings().getControlStringForEnumName("SHIP_SELECT_GROUP_4")));
            controls[25] = Keyboard.isKeyDown(Keyboard.getKeyIndex(Global.getSettings().getControlStringForEnumName("SHIP_SELECT_GROUP_5")));
            controls[26] = Keyboard.isKeyDown(Keyboard.getKeyIndex(Global.getSettings().getControlStringForEnumName("SHIP_SELECT_GROUP_6")));
            controls[27] = Keyboard.isKeyDown(Keyboard.getKeyIndex(Global.getSettings().getControlStringForEnumName("SHIP_SELECT_GROUP_7")));
        }

        // max length 32
        int bits = 0;
        for (int i = 0; i < controls.length; i++) {
            if (controls[i]) bits |= 1 << i;
        }

        return bits;
    }

    public void unmask(ShipAPI ship, int bitmask, float amount) {
        boolean[] controls = new boolean[Integer.SIZE];
        for (int i = 0; i < controls.length; i++) {
            if ((bitmask & 1 << i) != 0) controls[i] = true;
        }

        if (controls[0]) ship.giveCommand(ShipCommand.ACCELERATE, null, 0);
        if (controls[1]) ship.giveCommand(ShipCommand.ACCELERATE_BACKWARDS, null, 0);

        if (controls[4]) ship.giveCommand(ShipCommand.DECELERATE, null, 0);

        if (controls[5]) { // strafe mode
            float target = VectorUtils.getAngle(ship.getLocation(), mouseTarget);
            DroneAIUtils.rotate(target, ship, control);

            if (controls[2]) ship.giveCommand(ShipCommand.STRAFE_LEFT, null, 0);
            if (controls[3]) ship.giveCommand(ShipCommand.STRAFE_RIGHT, null, 0);
        } else {
            if (controls[2]) ship.giveCommand(ShipCommand.TURN_LEFT, null, 0);
            if (controls[3]) ship.giveCommand(ShipCommand.TURN_RIGHT, null, 0);
            if (controls[6]) ship.giveCommand(ShipCommand.STRAFE_LEFT, null, 0);
            if (controls[7]) ship.giveCommand(ShipCommand.STRAFE_RIGHT, null, 0);
        }

        if (controls[8]) ship.giveCommand(ShipCommand.USE_SYSTEM, null, 0);
        if (ship.getShield() != null) {
            if (controls[9]) {
                if (ship.getShield().isOff()) {
                    ship.giveCommand(ShipCommand.TOGGLE_SHIELD_OR_PHASE_CLOAK, null, 0);
                }
            } else {
                if (ship.getShield().isOn()) {
                    ship.giveCommand(ShipCommand.TOGGLE_SHIELD_OR_PHASE_CLOAK, null, 0);
                }
            }
        }

        int selected = 0;
        List<WeaponGroupAPI> groups = ship.getWeaponGroupsCopy();
        for (int i = 0; i < groups.size(); i++) {
            if (groups.get(i).equals(ship.getSelectedGroupAPI())) {
                selected = i;
                break;
            }
        }

        if (controls[10]) ship.giveCommand(ShipCommand.FIRE, ship.getMouseTarget(), selected);
        if (controls[11]) ship.giveCommand(ShipCommand.VENT_FLUX, null, 0);
        if (controls[12]) ship.giveCommand(ShipCommand.HOLD_FIRE, null, 0);
        if (controls[13]) {
            if (ship.isPullBackFighters()) {
                ship.giveCommand(ShipCommand.PULL_BACK_FIGHTERS, null, 0);
            }
        } else {
            if (!ship.isPullBackFighters()) {
                ship.giveCommand(ShipCommand.PULL_BACK_FIGHTERS, null, 0);
            }
        }
        if (controls[14]) ship.giveCommand(ShipCommand.SELECT_GROUP, ship.getMouseTarget(), 0);
        if (controls[15]) ship.giveCommand(ShipCommand.SELECT_GROUP, ship.getMouseTarget(), 1);
        if (controls[16]) ship.giveCommand(ShipCommand.SELECT_GROUP, ship.getMouseTarget(), 2);
        if (controls[17]) ship.giveCommand(ShipCommand.SELECT_GROUP, ship.getMouseTarget(), 3);
        if (controls[18]) ship.giveCommand(ShipCommand.SELECT_GROUP, ship.getMouseTarget(), 4);
        if (controls[19]) ship.giveCommand(ShipCommand.SELECT_GROUP, ship.getMouseTarget(), 5);
        if (controls[20]) ship.giveCommand(ShipCommand.SELECT_GROUP, ship.getMouseTarget(), 6);

        if (controls[21]) ship.giveCommand(ShipCommand.TOGGLE_AUTOFIRE, ship.getMouseTarget(), 0);
        if (controls[22]) ship.giveCommand(ShipCommand.TOGGLE_AUTOFIRE, ship.getMouseTarget(), 1);
        if (controls[23]) ship.giveCommand(ShipCommand.TOGGLE_AUTOFIRE, ship.getMouseTarget(), 2);
        if (controls[24]) ship.giveCommand(ShipCommand.TOGGLE_AUTOFIRE, ship.getMouseTarget(), 3);
        if (controls[25]) ship.giveCommand(ShipCommand.TOGGLE_AUTOFIRE, ship.getMouseTarget(), 4);
        if (controls[26]) ship.giveCommand(ShipCommand.TOGGLE_AUTOFIRE, ship.getMouseTarget(), 5);
        if (controls[27]) ship.giveCommand(ShipCommand.TOGGLE_AUTOFIRE, ship.getMouseTarget(), 6);
    }

    public void setMouseTarget(Vector2f mouseTarget) {
        this.mouseTarget.set(mouseTarget);
    }

    public Vector2f getMouseTarget() {
        return mouseTarget;
    }

    public abstract static class ShipControlOverride {
        public final PlayerShipData data;

        public ShipControlOverride(PlayerShipData data) {
            this.data = data;
        }

        public abstract void control(ShipAPI ship);
    }
}
