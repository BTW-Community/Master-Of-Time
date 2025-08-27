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
                            server.getConfigurationManager().sendChatMsg(ChatMessageComponent.createFromText("The world speed got reset"));
                        }
                        case "increase" -> {
                            float value = TimeMasterAddon.getIncreaseValue(worldServer);
                            if (value >= 0.05) {
                                TimeMasterAddon.worldSpeedModifier = value;
                                server.getConfigurationManager().sendChatMsg(ChatMessageComponent.createFromText("The world speed got increased to " + value));
                            }else server.getConfigurationManager().sendChatMsg(ChatMessageComponent.createFromText("Increase value is too low ! You cannot go beyond 0.05 !").setColor(EnumChatFormatting.RED));
                        }
                        case "decrease" -> {
                            float value = TimeMasterAddon.getDecreaseValue(worldServer);
                            if (value >= 0.05) {
                                TimeMasterAddon.worldSpeedModifier = value;
                                server.getConfigurationManager().sendChatMsg(ChatMessageComponent.createFromText("The world speed got decreased to " + value));
                            }else server.getConfigurationManager().sendChatMsg(ChatMessageComponent.createFromText("Decrease value is too low ! You cannot go beyond 0.05 !").setColor(EnumChatFormatting.RED));
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
