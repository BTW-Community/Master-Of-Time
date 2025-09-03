package net.dravigen.time_master.packet;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;

import net.dravigen.time_master.TimeMasterAddon;
import net.minecraft.server.MinecraftServer;
import net.minecraft.src.*;

public class PacketHandlerC2S {

    /**
     * Handles incoming custom payload packets from clients.
     * This method is called from NetServerHandlerMixin and MUST run on the Server Thread.
     *
     * @param packet The received custom payload packet.
     * @param player The player who sent the packet.
     */
    public static void handle(Packet250CustomPayload packet, EntityPlayerMP player) {
        if (packet.channel.equals(TimeMasterAddon.TMChannel.CLIENT_TO_SERVER_CHANNEL)) {
            try {
                ByteArrayInputStream bis = new ByteArrayInputStream(packet.data);
                DataInputStream dis = new DataInputStream(bis);

                String receivedMessage = dis.readUTF();

                // --- STUFF ON SERVER HERE ---

                MinecraftServer server = MinecraftServer.getServer();
                String[] splitText = receivedMessage.split(":");
                String subChannel = splitText[0];
                if (server.getConfigurationManager().isPlayerOpped(player.getEntityName())) {
                    WorldServer worldServer = server.worldServers[0];
                    switch (subChannel) {
                        case "reset" -> {
                            TimeMasterAddon.worldSpeedModifier = 1;
                            server.getConfigurationManager().sendChatMsg(ChatMessageComponent.createFromText("The game is running normally"));
                        }
                        case "increase" -> {
                            float value = TimeMasterAddon.getIncreaseValue(worldServer);
                            if (value >= 0.2) {
                                TimeMasterAddon.worldSpeedModifier = value;
                                server.getConfigurationManager().sendChatMsg(ChatMessageComponent.createFromText("The tick rate 'goal' got set to " + value + "x (" + String.format("%.3f", value*20) + " t/s)"));
                            }else player.sendChatToPlayer(ChatMessageComponent.createFromText("Increase value is too low ! You cannot go below 0.01x (0.2 t/s) !").setColor(EnumChatFormatting.RED));
                        }
                        case "decrease" -> {
                            float value = TimeMasterAddon.getDecreaseValue(worldServer);
                            if (value >= 0.2) {
                                TimeMasterAddon.worldSpeedModifier = value;
                                server.getConfigurationManager().sendChatMsg(ChatMessageComponent.createFromText("The tick rate 'goal' got set to " + value + "x (" + String.format("%.3f", value*20) + " t/s)"));
                            }else player.sendChatToPlayer(ChatMessageComponent.createFromText("Decrease value is too low ! You cannot go below 0.01x (0.2 t/s) !").setColor(EnumChatFormatting.RED));
                        }
                    }
                }
            } catch (IOException e) {
                System.err.println("SERVER: Error handling C2S message packet: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }
}
