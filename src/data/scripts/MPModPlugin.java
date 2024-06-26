package data.scripts;

import com.fs.starfarer.api.BaseModPlugin;
import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.PluginPick;
import com.fs.starfarer.api.campaign.CampaignPlugin;
import com.fs.starfarer.api.combat.*;
import data.scripts.net.data.DataGenManager;
import data.scripts.net.data.packables.entities.projectiles.BallisticProjectileData;
import data.scripts.net.data.packables.entities.projectiles.MissileData;
import data.scripts.net.data.packables.entities.projectiles.MovingRayData;
import data.scripts.net.data.packables.entities.ships.*;
import data.scripts.net.data.packables.metadata.*;
import data.scripts.net.data.records.*;
import data.scripts.net.data.records.collections.ListenArrayRecord;
import data.scripts.net.data.records.collections.SyncingListRecord;
import data.scripts.plugins.MPClientPlugin;
import data.scripts.plugins.MPPlugin;

import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.*;

import data.scripts.loading.TempClassLoader;
import data.scripts.plugins.ai.MPDefaultAutofireAIPlugin;
import data.scripts.plugins.ai.MPDefaultMissileAIPlugin;

public class MPModPlugin extends BaseModPlugin {
    public static String VERSION = "v0.1.1";

    private static MPPlugin PLUGIN;

    /**
     * Disables Starsector's class loader restrictions
     * Credit to Andylizi (Method originates from his Planet Search plugin, found here https://github.com/andylizi/starsector-planet-search)
     * Alex's stance on bypassing class loader https://fractalsoftworks.com/forum/index.php?topic=23229.msg354196
     */
    static{
        try{
            ClassLoader cl = MPModPlugin.class.getClassLoader();
            while (cl != null && !(cl instanceof URLClassLoader)) cl = cl.getParent();
            if(cl == null) throw new RuntimeException("Unable to find URLClassLoader");
            URL[] urls = ((URLClassLoader)cl).getURLs();
            Class<?> cls = new TempClassLoader(urls, ClassLoader.getSystemClassLoader()).loadClass(MPModPlugin.class.getPackage().getName() + ".loading.ClassLoaderEditor");
            // Creates instance of ClassLoaderEditor through the temporary class loader
            MethodHandles.lookup().findConstructor(cls, MethodType.methodType(void.class)).invoke();
            Global.getLogger(MPModPlugin.class).info("Disabled Class Loading Restrictions");
        } catch (RuntimeException | Error ex){
            throw ex;
        } catch (Throwable t){
            throw new ExceptionInInitializerError(t);
        }
    }

    @Override
    public void onApplicationLoad(){
        BallisticProjectileData.TYPE_ID = DataGenManager.registerEntityType(BallisticProjectileData.class);
        MissileData.TYPE_ID = DataGenManager.registerEntityType(MissileData.class);
        MovingRayData.TYPE_ID = DataGenManager.registerEntityType(MovingRayData.class);
        ShieldData.TYPE_ID = DataGenManager.registerEntityType(ShieldData.class);
        WeaponData.TYPE_ID = DataGenManager.registerEntityType(WeaponData.class);
        ShipData.TYPE_ID = DataGenManager.registerEntityType(ShipData.class);
        VariantData.TYPE_ID = DataGenManager.registerEntityType(VariantData.class);
        ChatListenData.TYPE_ID = DataGenManager.registerEntityType(ChatListenData.class);
        ClientConnectionData.TYPE_ID = DataGenManager.registerEntityType(ClientConnectionData.class);
        ClientData.TYPE_ID = DataGenManager.registerEntityType(ClientData.class);
        LobbyData.TYPE_ID = DataGenManager.registerEntityType(LobbyData.class);
        ClientPlayerData.TYPE_ID = DataGenManager.registerEntityType(ClientPlayerData.class);
        ServerPlayerData.TYPE_ID = DataGenManager.registerEntityType(ServerPlayerData.class);
        ServerConnectionData.TYPE_ID = DataGenManager.registerEntityType(ServerConnectionData.class);

        Float32Record.setTypeId(DataGenManager.registerRecordType(Float32Record.class.getSimpleName(), Float32Record.getDefault()));
        IntRecord.setTypeId(DataGenManager.registerRecordType(IntRecord.class.getSimpleName(), IntRecord.getDefault()));
        StringRecord.setTypeId(DataGenManager.registerRecordType(StringRecord.class.getSimpleName(), StringRecord.getDefault()));
        Vector2f32Record.setTypeId(DataGenManager.registerRecordType(Vector2f32Record.class.getSimpleName(), Vector2f32Record.getDefault()));
        Vector3f32Record.setTypeId(DataGenManager.registerRecordType(Vector3f32Record.class.getSimpleName(), Vector3f32Record.getDefault()));
        SyncingListRecord.setTypeId(DataGenManager.registerRecordType(SyncingListRecord.class.getSimpleName(), new SyncingListRecord<>(new ArrayList<>(), (byte) -1)));
        Float16Record.setTypeId(DataGenManager.registerRecordType(Float16Record.class.getSimpleName(), Float16Record.getDefault()));
        ByteRecord.setTypeId(DataGenManager.registerRecordType(ByteRecord.class.getSimpleName(), ByteRecord.getDefault()));
        Vector2f16Record.setTypeId(DataGenManager.registerRecordType(Vector2f16Record.class.getSimpleName(), Vector2f16Record.getDefault()));
        ShortRecord.setTypeId(DataGenManager.registerRecordType(ShortRecord.class.getSimpleName(), ShortRecord.getDefault()));
        ListenArrayRecord.setTypeId(DataGenManager.registerRecordType(ListenArrayRecord.class.getSimpleName(), new ListenArrayRecord<>(new ArrayList<>(), (byte) -1)));
    }

    @Override
    public PluginPick<AutofireAIPlugin> pickWeaponAutofireAI(WeaponAPI weapon) {
        if (getPlugin() != null && getPlugin().getType() == MPPlugin.PluginType.CLIENT) {
            MPDefaultAutofireAIPlugin plugin = new MPDefaultAutofireAIPlugin(weapon);

            ShipAPI ship = weapon.getShip();
            MPClientPlugin clientPlugin = (MPClientPlugin) getPlugin();
            Map<String, MPDefaultAutofireAIPlugin> plugins = clientPlugin.getShipTable().getTempAutofirePlugins().get(ship.getId());
            if (plugins == null) plugins = new HashMap<>();
            plugins.put(weapon.getSlot().getId(), plugin);
            clientPlugin.getShipTable().getTempAutofirePlugins().put(ship.getId(), plugins);

            return new PluginPick<>((AutofireAIPlugin) plugin, CampaignPlugin.PickPriority.HIGHEST);
        }
        return null;
    }

    @Override
    public PluginPick<MissileAIPlugin> pickMissileAI(MissileAPI missile, ShipAPI launchingShip) {
        if (getPlugin() != null && getPlugin().getType() == MPPlugin.PluginType.CLIENT) {
            MPDefaultMissileAIPlugin plugin = new MPDefaultMissileAIPlugin();

            return new PluginPick<>((MissileAIPlugin) plugin, CampaignPlugin.PickPriority.HIGHEST);
        }
        return null;
    }

    public static void setPlugin(MPPlugin plugin) {
        if (PLUGIN != null) {
            Global.getCombatEngine().removePlugin(PLUGIN);
        }
        Global.getCombatEngine().addPlugin(plugin);
        PLUGIN = plugin;
    }

    public static void destroyPlugin() {
        if (PLUGIN == null) return;
        Global.getCombatEngine().removePlugin(PLUGIN);
    }

    public static MPPlugin getPlugin() {
        return PLUGIN;
    }
}