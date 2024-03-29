package io.anuke.mindustry.core;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.utils.IntMap;
import com.badlogic.gdx.utils.IntSet;
import io.anuke.mindustry.core.GameState.State;
import io.anuke.mindustry.entities.BulletType;
import io.anuke.mindustry.entities.Player;
import io.anuke.mindustry.entities.SyncEntity;
import io.anuke.mindustry.entities.units.BaseUnit;
import io.anuke.mindustry.io.Platform;
import io.anuke.mindustry.net.Net;
import io.anuke.mindustry.net.Net.SendMode;
import io.anuke.mindustry.net.NetworkIO;
import io.anuke.mindustry.net.Packets.*;
import io.anuke.mindustry.world.Block;
import io.anuke.mindustry.world.Placement;
import io.anuke.mindustry.world.Tile;
import io.anuke.ucore.core.Timers;
import io.anuke.ucore.entities.BaseBulletType;
import io.anuke.ucore.entities.Entities;
import io.anuke.ucore.entities.EntityGroup;
import io.anuke.ucore.modules.Module;
import io.anuke.ucore.util.Log;
import io.anuke.ucore.util.Timer;

import java.nio.ByteBuffer;

import static io.anuke.mindustry.Vars.*;

public class NetClient extends Module {
    private final static float dataTimeout = 60*18;
    private final static float playerSyncTime = 2;

    private Timer timer = new Timer(5);
    /**Whether the client is currently conencting.*/
    private boolean connecting = false;
    /**If true, no message will be shown on disconnect.*/
    private boolean quiet = false;
    /**List of all recieved entitity IDs, to prevent duplicates.*/
    private IntSet recieved = new IntSet();
    /**List of recently recieved entities that have not been added to the queue yet.*/
    private IntMap<SyncEntity> recent = new IntMap<>();
    /**Counter for data timeout.*/
    private float timeoutTime = 0f;

    public NetClient(){

        Net.handleClient(Connect.class, packet -> {
            player.isAdmin = false;

            Net.setClientLoaded(false);
            recieved.clear();
            recent.clear();
            timeoutTime = 0f;
            connecting = true;
            quiet = false;

            ui.chatfrag.clearMessages();
            ui.loadfrag.hide();
            ui.loadfrag.show("$text.connecting.data");

            Entities.clear();

            ConnectPacket c = new ConnectPacket();
            c.name = player.name;
            c.android = android;
            c.color = Color.rgba8888(player.color);
            c.uuid = Platform.instance.getUUID();

            if(c.uuid == null){
                ui.showError("$text.invalidid");
                ui.loadfrag.hide();
                disconnectQuietly();
                return;
            }

            Net.send(c, SendMode.tcp);
        });

        Net.handleClient(Disconnect.class, packet -> {
            if (quiet) return;

            Timers.runTask(3f, ui.loadfrag::hide);

            state.set(State.menu);

            ui.showError("$text.disconnect");
            connecting = false;

            Platform.instance.updateRPC();
        });

        Net.handleClient(WorldData.class, data -> {
            Log.info("Recieved world data: {0} bytes.", data.stream.available());
            NetworkIO.loadWorld(data.stream);
            player.set(world.getSpawnX(), world.getSpawnY());

            finishConnecting();
        });

        Net.handleClient(SyncPacket.class, packet -> {
            if (connecting) return;
            int players = 0;
            int enemies = 0;

            ByteBuffer data = ByteBuffer.wrap(packet.data);
            long time = data.getLong();

            byte groupid = data.get();
            byte writesize = data.get();

            EntityGroup<?> group = Entities.getGroup(groupid);

            while (data.position() < data.capacity()) {
                int id = data.getInt();

                SyncEntity entity = (SyncEntity) group.getByID(id);

                if(entity instanceof Player) players ++;
                if(entity instanceof BaseUnit) enemies ++;

                if (entity == null || id == player.id) {
                    if (id != player.id) {
                        EntityRequestPacket req = new EntityRequestPacket();
                        req.id = id;
                        req.group = groupid;
                        Net.send(req, SendMode.udp);
                    }
                    data.position(data.position() + writesize);
                } else {
                    entity.read(data, time);
                }
            }

            if(debugNet){
                clientDebug.setSyncDebug(players, enemies);
            }
        });

        Net.handleClient(StateSyncPacket.class, packet -> {

            System.arraycopy(packet.items, 0, state.inventory.getItems(), 0, packet.items.length);

            state.enemies = packet.enemies;
            state.wavetime = packet.countdown;
            state.wave = packet.wave;

            ui.hudfrag.updateItems();
        });

        Net.handleClient(PlacePacket.class, (packet) -> {
            Player placer = playerGroup.getByID(packet.playerid);

            Placement.placeBlock(placer.team, packet.x, packet.y, Block.getByID(packet.block), packet.rotation, true, Timers.get("placeblocksound", 10));

            if(packet.playerid == player.id){
                Tile tile = world.tile(packet.x, packet.y);
                if(tile != null) Block.getByID(packet.block).placed(tile);
            }
        });

        Net.handleClient(BreakPacket.class, (packet) -> {
            Player placer = playerGroup.getByID(packet.playerid);

            Placement.breakBlock(placer.team, packet.x, packet.y, true, Timers.get("breakblocksound", 10));
        });

        Net.handleClient(EntitySpawnPacket.class, packet -> {
            EntityGroup group = packet.group;

            //duplicates.
            if (group.getByID(packet.entity.id) != null ||
                    recieved.contains(packet.entity.id)) return;

            recieved.add(packet.entity.id);
            recent.put(packet.entity.id, packet.entity);

            packet.entity.add();

            Log.info("Recieved entity {0}", packet.entity.id);
        });

        Net.handleClient(EntityDeathPacket.class, packet -> {
            EntityGroup group = Entities.getGroup(packet.group);
            SyncEntity entity = (SyncEntity) group.getByID(packet.id);

            recieved.add(packet.id);

            if(entity != null) {
                entity.onRemoteDeath();
            }else{
                if(recent.get(packet.id) != null){
                    recent.get(packet.id).onRemoteDeath();
                }else{
                    Log.err("Got remove for null entity! {0} / group type {1}", packet.id, group.getType());
                }
            }
        });

        Net.handleClient(EntityShootPacket.class, packet -> {
            BulletType type = BaseBulletType.getByID(packet.bulletid);
            EntityGroup group = Entities.getGroup(packet.groupid);
            SyncEntity owner = (SyncEntity) group.getByID(packet.entityid);

            owner.onRemoteShoot(type, packet.x, packet.y, packet.rotation, packet.data);
        });

        Net.handleClient(BlockDestroyPacket.class, packet -> {
            Tile tile = world.tile(packet.position % world.width(), packet.position / world.width());
            if (tile != null && tile.entity != null) {
                tile.entity.onDeath(true);
            }
        });

        Net.handleClient(BlockUpdatePacket.class, packet -> {
            Tile tile = world.tile(packet.position % world.width(), packet.position / world.width());
            if (tile != null && tile.entity != null) {
                tile.entity.health = packet.health;
            }
        });

        Net.handleClient(DisconnectPacket.class, packet -> {
            Player player = playerGroup.getByID(packet.playerid);

            if (player != null) {
                player.remove();
            }

            Platform.instance.updateRPC();
        });

        Net.handleClient(KickPacket.class, packet -> {
            quiet = true;
            Net.disconnect();
            state.set(State.menu);
            if(!packet.reason.quiet) ui.showError("$text.server.kicked." + packet.reason.name());
            ui.loadfrag.hide();
        });

        Net.handleClient(GameOverPacket.class, packet -> {
            //TODO core death effects
            quiet = true;
            ui.restart.show();
        });

        Net.handleClient(FriendlyFireChangePacket.class, packet -> state.friendlyFire = packet.enabled);

        Net.handleClient(NetErrorPacket.class, packet -> {
            ui.showError(packet.message);
            disconnectQuietly();
        });

        Net.handleClient(PlayerAdminPacket.class, packet -> {
            Player player = playerGroup.getByID(packet.id);
            player.isAdmin = packet.admin;
            ui.listfrag.rebuild();
        });

        Net.handleClient(TracePacket.class, packet -> {
            Player player = playerGroup.getByID(packet.info.playerid);
            ui.traces.show(player, packet.info);
        });
    }

    @Override
    public void update(){
        if(!Net.client()) return;

        if(!state.is(State.menu)){
            if(!connecting) sync();
        }else if(!connecting){
            Net.disconnect();
        }else{ //...must be connecting
            timeoutTime += Timers.delta();
            if(timeoutTime > dataTimeout){
                Log.err("Failed to load data!");
                ui.loadfrag.hide();
                quiet = true;
                ui.showError("$text.disconnect.data");
                Net.disconnect();
                timeoutTime = 0f;
            }
        }
    }

    public boolean isConnecting(){
        return connecting;
    }

    private void finishConnecting(){
        state.set(State.playing);
        connecting = false;
        ui.loadfrag.hide();
        ui.join.hide();
        Net.setClientLoaded(true);
        Timers.runTask(1f, () -> Net.send(new ConnectConfirmPacket(), SendMode.tcp));
        Timers.runTask(40f, Platform.instance::updateRPC);
    }

    public void beginConnecting(){
        connecting = true;
    }

    public void disconnectQuietly(){
        quiet = true;
        Net.disconnect();
    }

    public void clearRecieved(){
        recieved.clear();
    }

    void sync(){

        if(timer.get(0, playerSyncTime)){
            PositionPacket packet = new PositionPacket();
            packet.player = player;
            Net.send(packet, SendMode.udp);
        }

        if(timer.get(1, 60)){
            Net.updatePing();
        }
    }
}