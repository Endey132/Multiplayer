package data.scripts.plugins;

import com.fs.starfarer.api.Script;
import data.scripts.net.data.DataGenManager;
import data.scripts.net.data.packables.entities.projectiles.BallisticProjectileData;
import data.scripts.net.data.packables.entities.projectiles.MissileData;
import data.scripts.net.data.packables.entities.projectiles.MovingRayData;
import data.scripts.net.data.packables.entities.ships.*;
import data.scripts.net.data.packables.metadata.*;
import data.scripts.net.data.records.*;
import data.scripts.net.data.records.collections.ListenArrayRecord;
import data.scripts.net.data.records.collections.SyncingListRecord;

import java.util.ArrayList;

/**
 * Persistent class generated through custom class loader
 */
public class MPBasePlugin implements Script {
    public static MPBasePlugin instance;
    public static boolean initialized = false;
    @Override
    public void run(){
        if(initialized)
            return;
        instance = this;

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

        initialized = true;
    }
}
