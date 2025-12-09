package net.dravigen.master_of_time.packet;

import net.dravigen.master_of_time.MasterOfTimeAddon;
import net.minecraft.server.MinecraftServer;
import net.minecraft.src.*;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;

import static net.dravigen.master_of_time.MasterOfTimeAddon.*;

public class PacketHandlerC2S {
	
	/**
	 * Handles incoming custom payload packets from clients.
	 * This method is called from NetServerHandlerMixin and MUST run on the Server Thread.
	 *
	 * @param packet The received custom payload packet.
	 * @param player The player who sent the packet.
	 */
	public static void handle(Packet250CustomPayload packet, EntityPlayerMP player) {
		if (packet.channel.equals(MasterOfTimeAddon.TMChannel.CLIENT_TO_SERVER_CHANNEL)) {
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
							worldSpeedModifier = 1;
							server.getConfigurationManager()
									.sendChatMsg(ChatMessageComponent.createFromText("The game is running normally"));
						}
						case "increase" -> {
							float value = getUpSpeed(worldServer);
							
							if (value > 1) {
								worldSpeedModifier = value;
								server.getConfigurationManager()
										.sendChatMsg(ChatMessageComponent.createFromText(
												"The tick rate 'goal' got set to " +
														String.format("%.2f", value) +
														"x (" +
														String.format("%.1f", value * 20) +
														" t/s)"));
							}
							else {
								player.sendChatToPlayer(ChatMessageComponent.createFromText(
												"Up key speed is too low ! It should be at least above 1x (20 t/s) !")
																.setColor(EnumChatFormatting.RED));
							}
						}
						case "decrease" -> {
							float value = getDownSpeed(worldServer);
							if (value < 1) {
								worldSpeedModifier = value;
								server.getConfigurationManager()
										.sendChatMsg(ChatMessageComponent.createFromText(
												"The tick rate 'goal' got set to " +
														String.format("%.2f", value) +
														"x (" +
														String.format("%.1f", value * 20) +
														" t/s)"));
							}
							else {
								player.sendChatToPlayer(ChatMessageComponent.createFromText(
												"Down key speed is too high ! It should be at least below 1x (20 t/s) !")
																.setColor(EnumChatFormatting.RED));
							}
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
