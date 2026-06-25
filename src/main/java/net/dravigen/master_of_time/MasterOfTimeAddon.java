package net.dravigen.master_of_time;

import api.AddonHandler;
import api.BTWAddon;
import api.world.data.DataEntry;
import api.world.data.DataProvider;
import net.dravigen.master_of_time.commands.CommandTick;
import net.minecraft.server.MinecraftServer;
import net.minecraft.src.*;
import org.lwjgl.input.Keyboard;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.util.Locale;

public class MasterOfTimeAddon extends BTWAddon {
	
	public static final String MoTChannel = "MoT|C2S";
	private static final String PLAYER_OP_NAME = "PlayerOP";
	public static final DataEntry.PlayerDataEntry<Boolean> PLAYER_OP = DataProvider.getBuilder(Boolean.class)
			.name(PLAYER_OP_NAME)
			.defaultSupplier(() -> false)
			.readNBT(NBTTagCompound::getBoolean)
			.writeNBT(NBTTagCompound::setBoolean)
			.player()
			.syncPlayer()
			.buildPlayer();
	private static final String TIME_AFFECTED_NAME = "TickRateAffected";
	public static final DataEntry.PlayerDataEntry<Boolean> TIME_AFFECTED = DataProvider.getBuilder(Boolean.class)
			.name(TIME_AFFECTED_NAME)
			.defaultSupplier(() -> true)
			.readNBT(NBTTagCompound::getBoolean)
			.writeNBT(NBTTagCompound::setBoolean)
			.player()
			.syncPlayer()
			.buildPlayer();
	private static final String TICK_SPEED_NAME = "WorldTickSpeed";
	public static final DataEntry.WorldDataEntry<Float> TICK_SPEED = DataProvider.getBuilder(Float.class)
			.name(TICK_SPEED_NAME)
			.defaultSupplier(() -> 1.0F)
			.readNBT(NBTTagCompound::getFloat)
			.writeNBT(NBTTagCompound::setFloat)
			.global()
			.build();
	public static float upSpeedValue = 10.0f;
	public static float downSpeedValue = 0.25f;
	public static float originalTickSpeed = 1.0f;
	public static boolean bCreateWorld = false;
	public static KeyBinding reset_time_speed_key;
	public static KeyBinding upSpeedKey;
	public static KeyBinding downSpeedKey;
	public static KeyBinding freeze_time_speed_key;
	public static KeyBinding step_time_key;
	public static volatile float worldSpeedModifier = 1F;
	public static boolean currentSpeedTest = false;
	public static boolean maxSpeedTest = false;
	public static double tps;
	public static boolean step = false;
	
	public MasterOfTimeAddon() {
		super();
	}
	
	public static void setOptionFloatValue(String name, float value) {
		if (name.equals("downSpeedValue")) {
			downSpeedValue = value;
		}
		else if (name.equals("upSpeedValue")) {
			upSpeedValue = value;
		}
		
		Minecraft.getMinecraft().gameSettings.saveOptions();
	}
	
	public static void sendTickRateMsg(EntityPlayer player, float tickSpeed) {
		String speed = String.format(java.util.Locale.ENGLISH, "%.2f", tickSpeed);
		String tick = String.format(Locale.ENGLISH, "%.1f", tickSpeed * 20);
		
		player.sendChatToPlayer(ChatMessageComponent.createFromTranslationWithSubstitutions("mot.command.rate.success",
																							speed,
																							tick));
	}
	
	public static void setTickSpeed(float speed, ICommandSender player) {
		worldSpeedModifier = speed;
		
		if (speed != 0) {
			if (player.getEntityWorld().isRemote) {
				ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
				DataOutputStream dataOutputStream = new DataOutputStream(byteArrayOutputStream);
				
				try {
					dataOutputStream.writeFloat(speed);
					Minecraft.getMinecraft()
							.getNetHandler()
							.addToSendQueue(new Packet250CustomPayload(MoTChannel,
																	   byteArrayOutputStream.toByteArray()));
				} catch (Exception exception) {
					exception.printStackTrace();
				}
			}
			else {
				player.getEntityWorld().setData(TICK_SPEED, speed);
			}
		}
	}
	
	public void initKeybind() {
		reset_time_speed_key = new KeyBinding(StatCollector.translateToLocal("Reset time speed"), Keyboard.KEY_R);
		upSpeedKey = new KeyBinding(StatCollector.translateToLocal("Speed up key"), Keyboard.KEY_G);
		downSpeedKey = new KeyBinding(StatCollector.translateToLocal("Slow down key"), Keyboard.KEY_V);
		freeze_time_speed_key = new KeyBinding(StatCollector.translateToLocal("Freeze time"), Keyboard.KEY_F);
		step_time_key = new KeyBinding(StatCollector.translateToLocal("Step 1 tick"), Keyboard.KEY_N);
		
	}
	
	public void preInitialize() {
		this.modID = "MoT";
		TIME_AFFECTED.register();
		PLAYER_OP.register();
		TICK_SPEED.register();
	}
	
	@Override
	public void initialize() {
		AddonHandler.logMessage(this.getName() + " Version " + this.getVersionString() + " Initializing...");
		registerAddonCommand(new CommandTick());
		
		if (!MinecraftServer.getIsServer()) {
			initKeybind();
		}
		
		this.registerPacketHandler(MoTChannel, (packet, player) -> {
			ByteArrayInputStream bis = new ByteArrayInputStream(packet.data);
			DataInputStream dis = new DataInputStream(bis);
			
			if (MinecraftServer.getServer().getConfigurationManager().isPlayerOpped(player.getEntityName())) {
				float speed = dis.readFloat();
				player.worldObj.setData(TICK_SPEED, speed);
			}
		});
	}
	
	@Override
	public void serverPlayerConnectionInitialized(NetServerHandler serverHandler, EntityPlayerMP playerMP) {
		if (serverHandler.mcServer.isSinglePlayer()) {
			float speed = bCreateWorld ? originalTickSpeed : playerMP.worldObj.getData(TICK_SPEED);
			
			speed = speed == 0 ? 1 : Math.min(speed, 250);
			
			if (bCreateWorld) {
				playerMP.worldObj.setData(TICK_SPEED, speed);
				bCreateWorld = false;
			}
			
			MasterOfTimeAddon.worldSpeedModifier = speed;
		}
	}
}


