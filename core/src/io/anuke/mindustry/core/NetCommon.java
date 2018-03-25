package io.anuke.mindustry.core;

import io.anuke.mindustry.Vars;
import io.anuke.mindustry.entities.Player;
import io.anuke.mindustry.net.Net;
import io.anuke.mindustry.net.Net.SendMode;
import io.anuke.mindustry.net.Packets.*;
import io.anuke.mindustry.resource.Upgrade;
import io.anuke.mindustry.resource.Weapon;
import io.anuke.mindustry.world.Tile;
import io.anuke.mindustry.world.blocks.types.LogicAcceptor;
import io.anuke.ucore.modules.Module;

import static io.anuke.mindustry.Vars.*;

public class NetCommon extends Module {

    public NetCommon() {

        Net.handle(ShootPacket.class, (packet) -> {
            Player player = playerGroup.getByID(packet.playerid);

            Weapon weapon = (Weapon) Upgrade.getByID(packet.weaponid);
            weapon.shoot(player, packet.x, packet.y, packet.rotation);
        });

        Net.handle(ChatPacket.class, (packet) -> {
            ui.chatfrag.addMessage(packet.text, colorizeName(packet.id, packet.name));
        });

        Net.handle(WeaponSwitchPacket.class, (packet) -> {
            Player player = playerGroup.getByID(packet.playerid);

            if (player == null) return;

            player.weaponLeft = (Weapon) Upgrade.getByID(packet.left);
            player.weaponRight = (Weapon) Upgrade.getByID(packet.right);
        });

        Net.handle(BlockTapPacket.class, (packet) -> {
            Tile tile = world[player.dimension].tile(packet.position);
            tile.block().tapped(tile);
        });

        Net.handle(BlockConfigPacket.class, (packet) -> {
            Tile tile = world[player.dimension].tile(packet.position);
            if (tile != null) tile.block().configure(tile,packet.intData ,packet.byteData);
        });

        Net.handle(LogicLinkPacket.class, (packet) -> {
            Tile tile = world[player.dimension].tile(packet.tile);
            Tile tile2 = world[player.dimension].tile(packet.tile2);
            if (tile != null && tile2 != null
                && tile instanceof LogicAcceptor && tile2 instanceof LogicAcceptor)
                ((LogicAcceptor)tile.block()).logicLink(tile,tile2);
        });

        Net.handle(PlayerDeathPacket.class, (packet) -> {
            Player player = playerGroup.getByID(packet.id);
            if (player == null) return;

            player.doRespawn();
        });

        Net.handle(ResearchPacket.class, packet -> {
            Vars.global.research(packet.id);
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
