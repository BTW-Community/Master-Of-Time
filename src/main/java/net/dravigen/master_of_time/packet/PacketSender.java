package net.dravigen.master_of_time.packet;

import net.dravigen.master_of_time.MasterOfTimeAddon;
import net.minecraft.src.Minecraft;
import net.minecraft.src.Packet250CustomPayload;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class PacketSender {
	
	public static void sendClientToServerMessage(Object message) throws IOException {
		if (!Minecraft.getMinecraft().theWorld.isRemote) {
			return;
		}
		
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		DataOutputStream dos = new DataOutputStream(bos);
		
		dos.writeUTF(String.valueOf(message));
		
		Packet250CustomPayload packet = new Packet250CustomPayload(MasterOfTimeAddon.MoTChannel, bos.toByteArray());
		
		Minecraft.getMinecraft().getNetHandler().addToSendQueue(packet);
		
	}
}
