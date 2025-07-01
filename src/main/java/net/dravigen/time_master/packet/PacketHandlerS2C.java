package net.dravigen.time_master.packet;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;

import net.dravigen.time_master.TimeMasterAddon;
import net.minecraft.src.Minecraft;
import net.minecraft.src.Packet250CustomPayload;

public class PacketHandlerS2C {

    /**
     * Handles incoming custom payload packets from the server.
     * This method is called from NetClientHandlerMixin.
     *
     * @param packet The received custom payload packet.
     */
    public static void handle(Packet250CustomPayload packet) {
        if (packet.channel.equals(TimeMasterAddon.TMChannel.SERVER_TO_CLIENT_CHANNEL)) {
            try {
                ByteArrayInputStream bis = new ByteArrayInputStream(packet.data);
                DataInputStream dis = new DataInputStream(bis);

                String receivedMessage = dis.readUTF();

                // --- DO STUFF ON CLIENT HERE ---
                Minecraft mc = Minecraft.getMinecraft();
                String[] splitText = receivedMessage.split(":");
                String subChannel = splitText[0];
                if (splitText.length==2) {
                    String property = splitText[1];
                }
                switch (subChannel){
                    case "reset" -> {}
                    case "increase" -> {}
                    case "decrease" -> {}
                }

            } catch (IOException e) {
                System.err.println("CLIENT: Error handling S2C message packet: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }
}
