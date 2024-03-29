package io.anuke.mindustry.core;

import com.badlogic.gdx.utils.*;
import io.anuke.mindustry.content.Mechs;
import io.anuke.mindustry.content.Recipes;
import io.anuke.mindustry.content.UpgradeRecipes;
import io.anuke.mindustry.core.GameState.State;
import io.anuke.mindustry.entities.BulletType;
import io.anuke.mindustry.entities.Player;
import io.anuke.mindustry.entities.SyncEntity;
import io.anuke.mindustry.game.EventType.GameOverEvent;
import io.anuke.mindustry.io.Platform;
import io.anuke.mindustry.io.Version;
import io.anuke.mindustry.net.*;
import io.anuke.mindustry.net.Administration.PlayerInfo;
import io.anuke.mindustry.net.Net.SendMode;
import io.anuke.mindustry.net.Packets.*;
import io.anuke.mindustry.resource.Recipe;
import io.anuke.mindustry.resource.Upgrade;
import io.anuke.mindustry.resource.Weapon;
import io.anuke.mindustry.world.Block;
import io.anuke.mindustry.world.Placement;
import io.anuke.ucore.core.Events;
import io.anuke.ucore.core.Timers;
import io.anuke.ucore.entities.BaseBulletType;
import io.anuke.ucore.entities.Entities;
import io.anuke.ucore.entities.EntityGroup;
import io.anuke.ucore.modules.Module;
import io.anuke.ucore.util.Log;
import io.anuke.ucore.util.Timer;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.nio.ByteBuffer;

import static io.anuke.mindustry.Vars.*;

public class NetServer extends Module{
    private final static float serverSyncTime = 4, itemSyncTime = 10, kickDuration = 30 * 1000;

    private final static int timerEntitySync = 0;
    private final static int timerStateSync = 1;

    public final Administration admins = new Administration();

    /**Maps connection IDs to players.*/
    private IntMap<Player> connections = new IntMap<>();
    private ObjectMap<String, ByteArray> weapons = new ObjectMap<>();
    private boolean closing = false;
    private Timer timer = new Timer(5);
    private ByteBuffer writeBuffer = ByteBuffer.allocate(32);

    public NetServer(){

        Events.on(GameOverEvent.class, () -> weapons.clear());

        Net.handleServer(Connect.class, (id, connect) -> {
            if(admins.isIPBanned(connect.addressTCP)){
                kick(id, KickReason.banned);
            }
        });

        Net.handleServer(ConnectPacket.class, (id, packet) -> {
            String uuid = new String(Base64Coder.encode(packet.uuid));

            if(Net.getConnection(id) == null ||
                    admins.isIPBanned(Net.getConnection(id).address)) return;

            TraceInfo trace = admins.getTrace(Net.getConnection(id).address);
            PlayerInfo info = admins.getInfo(uuid);

            if(admins.isIDBanned(uuid)){
                kick(id, KickReason.banned);
                return;
            }

            if(TimeUtils.millis() - info.lastKicked < kickDuration){
                kick(id, KickReason.recentKick);
                return;
            }

            String ip = Net.getConnection(id).address;

            admins.updatePlayerJoined(uuid, ip, packet.name);
            trace.uuid = uuid;
            trace.android = packet.android;

            if(packet.version != Version.build && Version.build != -1 && packet.version != -1){
                kick(id, packet.version > Version.build ? KickReason.serverOutdated : KickReason.clientOutdated);
                return;
            }

            if(packet.version == -1){
                trace.modclient = true;
            }

            Log.info("Sending data to player '{0}' / {1}", packet.name, id);

            Player player = new Player();
            player.isAdmin = admins.isAdmin(uuid, ip);
            player.clientid = id;
            player.name = packet.name;
            player.mech = packet.android ? Mechs.standardShip : Mechs.standard;
            player.set(world.getSpawnX(), world.getSpawnY());
            player.setNet(player.x, player.y);
            player.setNet(player.x, player.y);
            player.color.set(packet.color);
            connections.put(id, player);

            trace.playerid = player.id;

            //TODO try DeflaterOutputStream
            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            NetworkIO.writeWorld(player, weapons.get(player.name, new ByteArray()), stream);
            WorldData data = new WorldData();
            data.stream = new ByteArrayInputStream(stream.toByteArray());
            Net.sendStream(id, data);

            Log.info("Packed {0} uncompressed bytes of WORLD data.", stream.size());

            Platform.instance.updateRPC();
        });

        Net.handleServer(ConnectConfirmPacket.class, (id, packet) -> {
            Player player = connections.get(id);

            if (player == null) return;

            player.add();
            Log.info("&y{0} has connected.", player.name);
            netCommon.sendMessage("[accent]" + player.name + " has connected.");
        });

        Net.handleServer(Disconnect.class, (id, packet) -> {
            Player player = connections.get(packet.id);

            if (player == null) {
                Log.err("Unknown client has disconnected (ID={0})", id);
                return;
            }

            Log.info("&y{0} has disconnected.", player.name);
            netCommon.sendMessage("[accent]" + player.name + " has disconnected.");
            player.remove();

            DisconnectPacket dc = new DisconnectPacket();
            dc.playerid = player.id;

            Net.send(dc, SendMode.tcp);

            Platform.instance.updateRPC();
            admins.save();
        });

        Net.handleServer(PositionPacket.class, (id, packet) -> {
            //...don't do anything here as it's already handled by the packet itself
        });

        Net.handleServer(EntityShootPacket.class, (id, packet) -> {
            Player player = connections.get(id);

            BulletType type = BaseBulletType.getByID(packet.bulletid);

            player.onRemoteShoot(type, packet.x, packet.y, packet.rotation, packet.data);
            TraceInfo info = admins.getTrace(Net.getConnection(id).address);
            Weapon weapon = Upgrade.getByID((byte)packet.data);

            float wtrc = 40f;

            if(!Timers.get(info.ip + "-weapontrace", wtrc)){
                info.fastShots ++;
            }else{

                if(info.fastShots - 2 > (int)(wtrc / (weapon.getReload() / 2f))){
                    kick(id, KickReason.kick);
                }

                info.fastShots = 0;
            }

            packet.entityid = connections.get(id).id;
            Net.sendExcept(id, packet, SendMode.udp);
        });

        Net.handleServer(PlacePacket.class, (id, packet) -> {
            Player placer = connections.get(id);
            packet.playerid = placer.id;

            Block block = Block.getByID(packet.block);

            if(!Placement.validPlace(placer.team, packet.x, packet.y, block)) return;

            Recipe recipe = Recipes.getByResult(block);

            if(recipe == null) return;

            state.inventory.removeItems(recipe.requirements);

            Placement.placeBlock(placer.team, packet.x, packet.y, block, packet.rotation, true, false);

            admins.getTrace(Net.getConnection(id).address).lastBlockPlaced = block;
            admins.getTrace(Net.getConnection(id).address).totalBlocksPlaced ++;
            admins.getInfo(admins.getTrace(Net.getConnection(id).address).uuid).totalBlockPlaced ++;

            Net.send(packet, SendMode.tcp);
        });

        Net.handleServer(BreakPacket.class, (id, packet) -> {
            Player placer = connections.get(id);
            packet.playerid = placer.id;

            if(!Placement.validBreak(placer.team, packet.x, packet.y)) return;

            Block block = Placement.breakBlock(placer.team, packet.x, packet.y, true, false);

            if(block != null) {
                admins.getTrace(Net.getConnection(id).address).lastBlockBroken = block;
                admins.getTrace(Net.getConnection(id).address).totalBlocksBroken++;
                admins.getInfo(admins.getTrace(Net.getConnection(id).address).uuid).totalBlocksBroken ++;
                if (block.update || block.destructible)
                    admins.getTrace(Net.getConnection(id).address).structureBlocksBroken++;
            }

            Net.send(packet, SendMode.tcp);
        });

        Net.handleServer(ChatPacket.class, (id, packet) -> {
            if(!Timers.get("chatFlood" + id, 30)){
                ChatPacket warn = new ChatPacket();
                warn.text = "[scarlet]You are sending messages too quickly.";
                Net.sendTo(id, warn, SendMode.tcp);
                return;
            }
            Player player = connections.get(id);
            packet.name = player.name;
            packet.id = player.id;
            Net.send(packet, SendMode.tcp);
        });

        Net.handleServer(UpgradePacket.class, (id, packet) -> {
            Player player = connections.get(id);

            Weapon weapon = Upgrade.getByID(packet.id);

            if (!weapons.containsKey(player.name)) weapons.put(player.name, new ByteArray());
            if (!weapons.get(player.name).contains(weapon.id)) weapons.get(player.name).add(weapon.id);

            state.inventory.removeItems(UpgradeRecipes.get(weapon));
        });

        Net.handleServer(WeaponSwitchPacket.class, (id, packet) -> {
            packet.playerid = connections.get(id).id;
            Net.sendExcept(id, packet, SendMode.tcp);
        });

        Net.handleServer(BlockTapPacket.class, (id, packet) -> {
            Net.sendExcept(id, packet, SendMode.tcp);
        });

        Net.handleServer(BlockConfigPacket.class, (id, packet) -> {
            Net.sendExcept(id, packet, SendMode.tcp);
        });

        Net.handleServer(EntityRequestPacket.class, (cid, packet) -> {
            int id = packet.id;
            int dest = cid;
            EntityGroup group = Entities.getGroup(packet.group);
            if(group.getByID(id) != null){
                EntitySpawnPacket p = new EntitySpawnPacket();
                p.entity = (SyncEntity)group.getByID(id);
                p.group = group;
                Net.sendTo(dest, p, SendMode.tcp);
            }
        });

        Net.handleServer(EntityDeathPacket.class, (id, packet) -> {
            packet.id = connections.get(id).id;
            packet.group = (byte)connections.get(id).getGroup().getID();
            Net.sendExcept(id, packet, SendMode.tcp);
        });

        Net.handleServer(AdministerRequestPacket.class, (id, packet) -> {
            Player player = connections.get(id);

            if(!player.isAdmin){
                Log.err("ACCESS DENIED: Player {0} / {1} attempted to perform admin action without proper security access.",
                        player.name, Net.getConnection(player.clientid).address);
                return;
            }

            Player other = playerGroup.getByID(packet.id);

            if(other == null || other.isAdmin){
                Log.err("{0} attempted to perform admin action on nonexistant or admin player.", player.name);
                return;
            }

            String ip = Net.getConnection(other.clientid).address;

            if(packet.action == AdminAction.ban){
                admins.banPlayerIP(ip);
                kick(other.clientid, KickReason.banned);
                Log.info("&lc{0} has banned {1}.", player.name, other.name);
            }else if(packet.action == AdminAction.kick){
                kick(other.clientid, KickReason.kick);
                Log.info("&lc{0} has kicked {1}.", player.name, other.name);
            }else if(packet.action == AdminAction.trace){
                TracePacket trace = new TracePacket();
                trace.info = admins.getTrace(ip);
                Net.sendTo(id, trace, SendMode.tcp);
                Log.info("&lc{0} has requested trace info of {1}.", player.name, other.name);
            }
        });
    }

    public void update(){
        if(!headless && !closing && Net.server() && state.is(State.menu)){
            closing = true;
            reset();
            ui.loadfrag.show("$text.server.closing");
            Timers.runTask(5f, () -> {
                Net.closeServer();
                ui.loadfrag.hide();
                closing = false;
            });
        }

        if(!state.is(State.menu) && Net.server()){
            sync();
        }
    }

    public void reset(){
        weapons.clear();
        admins.clearTraces();
    }

    public void kick(int connection, KickReason reason){
        NetConnection con = Net.getConnection(connection);
        if(con == null){
            Log.err("Cannot kick unknown player!");
            return;
        }else{
            Log.info("Kicking connection #{0} / IP: {1}. Reason: {2}", connection, con.address, reason);
        }

        PlayerInfo info = admins.getInfo(admins.getTrace(con.address).uuid);

        if(reason == KickReason.kick || reason == KickReason.banned){
            info.timesKicked ++;
            info.lastKicked = TimeUtils.millis();
        }

        KickPacket p = new KickPacket();
        p.reason = reason;

        con.send(p, SendMode.tcp);
        Timers.runTask(2f, con::close);

        admins.save();
    }

    void sync(){

        if(timer.get(timerEntitySync, serverSyncTime)){
            //scan through all groups with syncable entities
            for(EntityGroup<?> group : Entities.getAllGroups()) {
                if(group.size() == 0 || !(group.all().iterator().next() instanceof SyncEntity)) continue;

                ((SyncEntity)group.all().get(0)).write(writeBuffer);

                //get write size for one entity (adding 4, as you need to write the ID as well)
                int writesize = writeBuffer.position() + 4;

                writeBuffer.position(0);
                //amount of entities
                int amount = group.size();
                //maximum amount of entities per packet
                int maxsize = 64;

                //current buffer you're writing to
                ByteBuffer current = null;
                //number of entities written to this packet/buffer
                int written = 0;

                //for all the entities...
                for (int i = 0; i < amount; i++) {
                    //if the buffer is null, create a new one
                    if(current == null){
                        //calculate amount of entities to go into this packet
                        int csize = Math.min(amount-i, maxsize);
                        //create a byte array to write to
                        byte[] bytes = new byte[csize*writesize + 1 + 1 + 8];
                        //wrap it for easy writing
                        current = ByteBuffer.wrap(bytes);
                        current.putLong(TimeUtils.millis());
                        //write the group ID so the client knows which group this is
                        current.put((byte)group.getID());
                        //write size of each entity write here
                        current.put((byte)writesize);
                    }

                    SyncEntity entity = (SyncEntity) group.all().get(i);

                    //write ID to the buffer
                    current.putInt(entity.id);

                    int previous = current.position();
                    //write extra data to the buffer
                    entity.write(current);

                    written ++;

                    //if the packet is too big now...
                    if(written >= maxsize){
                        //send the packet.
                        SyncPacket packet = new SyncPacket();
                        packet.data = current.array();
                        Net.send(packet, SendMode.udp);

                        //reset data, send the next packet
                        current = null;
                        written = 0;
                    }
                }

                //make sure to send incomplete packets too
                if(current != null){
                    SyncPacket packet = new SyncPacket();
                    packet.data = current.array();
                    Net.send(packet, SendMode.udp);
                }
            }
        }

        if(timer.get(timerStateSync, itemSyncTime)){
            StateSyncPacket packet = new StateSyncPacket();
            packet.items = state.inventory.getItems();
            packet.countdown = state.wavetime;
            packet.enemies = state.enemies;
            packet.wave = state.wave;
            packet.time = Timers.time();
            packet.timestamp = TimeUtils.millis();

            Net.send(packet, SendMode.udp);
        }
    }
}
