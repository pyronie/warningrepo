package io.anuke.mindustry.core;

import io.anuke.mindustry.entities.Player;
import io.anuke.mindustry.net.Net;
import io.anuke.mindustry.net.Net.SendMode;
import io.anuke.mindustry.net.Packets.BlockConfigPacket;
import io.anuke.mindustry.net.Packets.BlockTapPacket;
import io.anuke.mindustry.net.Packets.ChatPacket;
import io.anuke.mindustry.net.Packets.WeaponSwitchPacket;
import io.anuke.mindustry.resource.Upgrade;
import io.anuke.mindustry.world.Tile;
import io.anuke.ucore.modules.Module;

import static io.anuke.mindustry.Vars.*;

public class NetCommon extends Module {

    public NetCommon(){

        Net.handle(ChatPacket.class, (packet) -> {
            ui.chatfrag.addMessage(packet.text, colorizeName(packet.id, packet.name));
        });

        Net.handle(WeaponSwitchPacket.class, (packet) -> {
            Player player = playerGroup.getByID(packet.playerid);

            if (player == null) return;

            player.weaponLeft = Upgrade.getByID(packet.left);
            player.weaponRight = Upgrade.getByID(packet.right);
        });

        Net.handle(BlockTapPacket.class, (packet) -> {
            Tile tile = world.tile(packet.position);
            tile.block().tapped(tile);
        });

        Net.handle(BlockConfigPacket.class, (packet) -> {
            Tile tile = world.tile(packet.position);
            if (tile != null) tile.block().configure(tile, packet.data);
        });
    }

    public void sendMessage(String message){
        ChatPacket packet = new ChatPacket();
        packet.name = null;
        packet.text = message;
        Net.send(packet, SendMode.tcp);
        if(!headless) ui.chatfrag.addMessage(message, null);
    }

    public String colorizeName(int id, String name){
        Player player = playerGroup.getByID(id);
        if(name == null || player == null) return null;
        return "[#" + player.color.toString().toUpperCase() + "]" + name;
    }
}
