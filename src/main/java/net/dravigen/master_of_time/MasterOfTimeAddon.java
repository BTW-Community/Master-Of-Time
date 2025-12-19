package net.dravigen.master_of_time;

import api.AddonHandler;
import api.BTWAddon;
import api.world.data.DataEntry;
import api.world.data.DataProvider;
import net.minecraft.server.MinecraftServer;
import net.minecraft.src.*;
import org.lwjgl.input.Keyboard;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.util.List;

public class MasterOfTimeAddon extends BTWAddon {
	
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
	private static final String MASTER_OF_TIME_DATA_NAME = "MasterOfTimeData";
	public static final DataEntry.WorldDataEntry<float[]> MASTER_OF_TIME_DATA = DataProvider.getBuilder(float[].class)
			.name(MASTER_OF_TIME_DATA_NAME)
			.defaultSupplier(() -> new float[]{10f, 0.25f})
			.readNBT(tag -> {
				if (!tag.hasKey(MASTER_OF_TIME_DATA_NAME)) {
					NBTTagCompound defaultValue = new NBTTagCompound();
					defaultValue.setFloat("increaseValue", 10f);
					defaultValue.setFloat("decreaseValue", 0.25f);
					tag.setCompoundTag(MASTER_OF_TIME_DATA_NAME, defaultValue);
				}
				NBTTagCompound value = tag.getCompoundTag(MASTER_OF_TIME_DATA_NAME);
				return new float[]{value.getFloat("increaseValue"), value.getFloat("decreaseValue")};
			})
			.writeNBT((tag, value) -> {
				NBTTagCompound newValue = new NBTTagCompound();
				newValue.setFloat("increaseValue", value[0]);
				newValue.setFloat("decreaseValue", value[1]);
				tag.setCompoundTag(MASTER_OF_TIME_DATA_NAME, newValue);
			})
			.global()
			.build();
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
	
	public static float getUpSpeed(WorldServer server) {
		return server.getData(MASTER_OF_TIME_DATA)[0];
	}
	
	public static float getDownSpeed(WorldServer server) {
		return server.getData(MASTER_OF_TIME_DATA)[1];
	}
	
	public static void setIncreaseValue(WorldServer server, float value) {
		server.setData(MASTER_OF_TIME_DATA, new float[]{value, server.getData(MASTER_OF_TIME_DATA)[1]});
	}
	
	public static void setDecreaseValue(WorldServer server, float value) {
		server.setData(MASTER_OF_TIME_DATA, new float[]{server.getData(MASTER_OF_TIME_DATA)[0], value});
	}
	
	private void createNewCommand() {
		registerAddonCommand(new CommandBase() {
			public static boolean warned = false;
			
			@Override
			public String getCommandName() {
				return "tick";
			}
			
			@Override
			public String getCommandUsage(ICommandSender iCommandSender) {
				return "/tick <rate> <speed> OR /tick <reset> OR /tick <freeze> OR /tick <speedTest> OR /tick <maxSpeedTest> OR /tick <keySpeed> <upSpeedKey|downSpeedKey> <speed> OR /tick <playerAffected> <true|false>";
			}
			
			@Override
			public void processCommand(ICommandSender sender, String[] strings) {
				WorldServer worldServer = MinecraftServer.getServer().worldServers[0];
				if (strings.length == 0) throw new WrongUsageException(getCommandUsage(sender));
				
				switch (strings[0].toLowerCase()) {
					case "rate" -> {
						try {
							if (strings.length < 2)
								throw new WrongUsageException("/tick rate <speed> (higher than 0.01)");
							
							float speedModifier = Float.parseFloat(strings[1]);
							
							if (speedModifier < 0.05 && !warned) {
								if (MinecraftServer.getServer().isDedicatedServer()) {
									sender.sendChatToPlayer(ChatMessageComponent.createFromText("""
																								-----------------------\
																								
																								   WARNING MESSAGE !\
																								
																								Slowing down tick rate this low can cause issue to revert back to normal through commands\
																								
																								Re-enter the command to confirm
																								-----------------------""")
																	.setColor(EnumChatFormatting.RED));
									warned = true;
									return;
								}
							}
							if (speedModifier < 0.01F) {
								speedModifier = 0.01F;
								worldSpeedModifier = speedModifier;
								sender.sendChatToPlayer(ChatMessageComponent.createFromText(
										"Your value was too low ! The tick rate got forcefully set to the minimum value available: 0.01x (0.2 t/s)"));
							}
							else {
								worldSpeedModifier = speedModifier;
								sender.sendChatToPlayer(ChatMessageComponent.createFromText(
										"The tick rate 'goal' got set to " +
												String.format("%.2f", speedModifier) +
												"x (" +
												String.format("%.1f", speedModifier * 20) +
												" t/s)"));
							}
							
							
						} catch (NumberFormatException e) {
							throw new WrongUsageException("/tick rate <speed> (higher than 0.01)");
						}
					}
					case "reset" -> {
						worldSpeedModifier = 1.0F;
						
						sender.sendChatToPlayer(ChatMessageComponent.createFromText("The game is running normally"));
					}
					case "speedtest" -> {
						currentSpeedTest = true;
						sender.sendChatToPlayer(ChatMessageComponent.createFromText("""
																					
																					Testing the current speed of your game, it will take 10 secs...
																					--------------------------------------
																					DON'T PAUSE THE GAME DURING THE PROCESS !
																					--------------------------------------"""));
					}
					case "maxspeedtest" -> {
						worldSpeedModifier = 500F;
						currentSpeedTest = true;
						maxSpeedTest = true;
						
						sender.sendChatToPlayer(ChatMessageComponent.createFromText("""
																					
																					Testing the highest speed your pc could handle, it will take 25 secs...
																					--------------------------------------
																					DON'T PAUSE THE GAME DURING THE PROCESS !
																					--------------------------------------"""));
					}
					case "keyspeed" -> {
						try {
							if (strings.length < 3) {
								if (strings.length == 2) {
									if (strings[1].equals("upSpeedKey")) {
										sender.sendChatToPlayer(ChatMessageComponent.createFromText(Keyboard.getKeyName(
												upSpeedKey.keyCode) +
																											" is set to " +
																											String.format(
																													"%.2f",
																													getUpSpeed(
																															worldServer)) +
																											"x (" +
																											String.format(
																													"%.1f",
																													getUpSpeed(
																															worldServer) *
																															20) +
																											" t/s)"));
										
										return;
									}
									else if (strings[1].equals("downSpeedKey")) {
										sender.sendChatToPlayer(ChatMessageComponent.createFromText(Keyboard.getKeyName(
												downSpeedKey.keyCode) +
																											" is set to " +
																											String.format(
																													"%.2f",
																													getDownSpeed(
																															worldServer)) +
																											"x (" +
																											String.format(
																													"%.1f",
																													getDownSpeed(
																															worldServer) *
																															20) +
																											" t/s)"));
										return;
									}
								}
								
								throw new WrongUsageException("/tick keySpeed <upSpeedKey|downSpeedKey> <speed>");
							}
							
							if (strings[1].equals("upSpeedKey")) {
								if (Float.parseFloat(strings[2]) <= 1) {
									sender.sendChatToPlayer(ChatMessageComponent.createFromText(
													"Speed is too low ! It should be at least above 1x (20 t/s) !")
																	.setColor(EnumChatFormatting.RED));
									
									return;
								}
								
								setIncreaseValue(worldServer, Math.min(250, Float.parseFloat(strings[2])));
								sender.sendChatToPlayer(ChatMessageComponent.createFromText("By pressing " +
																									Keyboard.getKeyName(
																											upSpeedKey.keyCode) +
																									", the tick rate 'goal' will be set to " +
																									String.format("%.2f",
																												  getUpSpeed(
																														  worldServer)) +
																									"x (" +
																									String.format("%.1f",
																												  getUpSpeed(
																														  worldServer) *
																														  20) +
																									" t/s)"));
							}
							else if (strings[1].equals("downSpeedKey")) {
								if (Float.parseFloat(strings[2]) >= 1) {
									sender.sendChatToPlayer(ChatMessageComponent.createFromText(
													"Speed is too high ! It should be at least below 1x (20 t/s) !")
																	.setColor(EnumChatFormatting.RED));
									
									return;
								}
								
								setDecreaseValue(worldServer, Math.max(0.01F, Float.parseFloat(strings[2])));
								sender.sendChatToPlayer(ChatMessageComponent.createFromText("By pressing " +
																									Keyboard.getKeyName(
																											downSpeedKey.keyCode) +
																									", the tick rate 'goal' will be set to " +
																									String.format("%.2f",
																												  getDownSpeed(
																														  worldServer)) +
																									"x (" +
																									String.format("%.1f",
																												  getDownSpeed(
																														  worldServer) *
																														  20) +
																									" t/s)"));
							}
						} catch (NumberFormatException e) {
							throw new WrongUsageException("/tick keySpeed upSpeedKey/downSpeedKey <speed>");
						}
					}
					case "freeze" -> {
						if (MinecraftServer.getServer().isDedicatedServer()) {
							sender.sendChatToPlayer(ChatMessageComponent.createFromText(
									"Freezing the server doesn't work properly").setColor(EnumChatFormatting.RED));
							return;
						}
						
						worldSpeedModifier = 0;
					}
					case "playeraffected" -> {
						try {
							if (strings.length < 2) {
								sender.sendChatToPlayer(ChatMessageComponent.createFromText(getPlayer(sender,
																									  sender.getCommandSenderName()).getData(
										TIME_AFFECTED)
																							? "You are currently affected by modified tick rate"
																							: "You aren't currently affected by modified tick rate"));
								
								return;
							}
							
							boolean affected = Boolean.parseBoolean(strings[1]);
							getPlayer(sender, sender.getCommandSenderName()).setData(TIME_AFFECTED, affected);
							
							if (affected) {
								sender.sendChatToPlayer(ChatMessageComponent.createFromText(
										"You are now affected by modified tick rate"));
							}
							else {
								sender.sendChatToPlayer(ChatMessageComponent.createFromText(
										"You are now no longer affected by modified tick rate"));
							}
						} catch (Exception ignored) {
							throw new WrongUsageException("/tick playerAffected <true|false>");
						}
					}
				}
			}
			
			@SuppressWarnings("rawtypes")
			@Override
			public List addTabCompletionOptions(ICommandSender par1ICommandSender, String[] par2ArrayOfStr) {
				if (par2ArrayOfStr.length == 1) {
					return getListOfStringsMatchingLastWord(par2ArrayOfStr,
															"rate",
															"reset",
															"freeze",
															"speedTest",
															"maxSpeedTest",
															"keySpeed",
															"playerAffected");
				}
				else if (par2ArrayOfStr.length == 2 && par2ArrayOfStr[0].equalsIgnoreCase("keySpeed")) {
					return getListOfStringsMatchingLastWord(par2ArrayOfStr, "upSpeedKey", "downSpeedKey");
				}
				else if (par2ArrayOfStr.length == 2 && par2ArrayOfStr[0].equalsIgnoreCase("playerAffected")) {
					return getListOfStringsMatchingLastWord(par2ArrayOfStr, "true", "false");
				}
				return null;
			}
		});
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
		MASTER_OF_TIME_DATA.register();
		TIME_AFFECTED.register();
		PLAYER_OP.register();
	}
	
	@Override
	public void initialize() {
		AddonHandler.logMessage(this.getName() + " Version " + this.getVersionString() + " Initializing...");
		createNewCommand();
		if (!MinecraftServer.getIsServer()) {
			initKeybind();
		}
		
		this.registerPacketHandler(MoTChannel, (packet, player) -> {
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
		});
	}
	
	public static final String MoTChannel = "MoT|C2S";
}


