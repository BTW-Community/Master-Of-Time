package net.dravigen.master_of_time.commands;

import net.minecraft.server.MinecraftServer;
import net.minecraft.src.*;
import org.lwjgl.input.Keyboard;

import java.util.List;
import java.util.Locale;

import static net.dravigen.master_of_time.MasterOfTimeAddon.*;

public class CommandTick extends CommandBase {
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
		if (strings.length == 0) throw new WrongUsageException(getCommandUsage(sender));
		
		switch (strings[0].toLowerCase()) {
			case "rate" -> {
				try {
					if (strings.length < 2)
						throw new WrongUsageException(I18n.getString("mot.command.rate.error.format"));
					
					float speedModifier = Float.parseFloat(strings[1]);
					
					if (speedModifier < 0.05 && !warned) {
						if (MinecraftServer.getServer().isDedicatedServer()) {
							ChatMessageComponent line = ChatMessageComponent.createFromText("-----------------------")
									.setColor(EnumChatFormatting.RED);
							
							sender.sendChatToPlayer(line);
							
							sender.sendChatToPlayer(ChatMessageComponent.createFromTranslationKey(
									"mot.command.rate.warning.slow.1").setColor(EnumChatFormatting.RED));
							
							sender.sendChatToPlayer(ChatMessageComponent.createFromTranslationKey(
									"mot.command.rate.warning.slow.2").setColor(EnumChatFormatting.RED));
							
							sender.sendChatToPlayer(ChatMessageComponent.createFromTranslationKey(
									"mot.command.rate.warning.slow.3").setColor(EnumChatFormatting.RED));
							
							
							sender.sendChatToPlayer(line);
							warned = true;
							return;
						}
					}
					
					if (speedModifier < 0.01F) {
						speedModifier = 0.01F;
						setTickSpeed(speedModifier, sender);
						sender.sendChatToPlayer(ChatMessageComponent.createFromTranslationKey(
								"mot.command.rate.error.tooLow"));
					}
					else {
						speedModifier = Math.min(250, speedModifier);
						
						setTickSpeed(speedModifier, sender);
						String speed = String.format(Locale.ENGLISH, "%.2f", speedModifier);
						String tick = String.format(Locale.ENGLISH, "%.1f", speedModifier * 20);
						
						sender.sendChatToPlayer(ChatMessageComponent.createFromTranslationWithSubstitutions(
								"mot.command.rate.success",
								speed,
								tick));
					}
					
					
				} catch (NumberFormatException e) {
					throw new WrongUsageException(I18n.getString("mot.command.rate.error.format"));
				}
			}
			case "reset" -> {
				setTickSpeed(1.0F, sender);
				
				sender.sendChatToPlayer(ChatMessageComponent.createFromTranslationKey("mot.reset"));
			}
			case "speedtest" -> {
				currentSpeedTest = true;
				ChatMessageComponent line = ChatMessageComponent.createFromText("--------------------------------------");
				
				
				sender.sendChatToPlayer(ChatMessageComponent.createFromText(""));
				
				sender.sendChatToPlayer(ChatMessageComponent.createFromTranslationKey("mot.command.speedtest.msg.1"));
				
				sender.sendChatToPlayer(line);
				
				sender.sendChatToPlayer(ChatMessageComponent.createFromTranslationKey("mot.command.speedtest.msg.2"));
				
				sender.sendChatToPlayer(line);
			}
			case "maxspeedtest" -> {
				worldSpeedModifier = 500F;
				currentSpeedTest = true;
				maxSpeedTest = true;
				
				ChatMessageComponent line = ChatMessageComponent.createFromText("--------------------------------------");
				
				sender.sendChatToPlayer(ChatMessageComponent.createFromText(""));
				
				sender.sendChatToPlayer(ChatMessageComponent.createFromTranslationKey("mot.command.maxspeedtest.msg.1"));
				
				sender.sendChatToPlayer(line);
				
				sender.sendChatToPlayer(ChatMessageComponent.createFromTranslationKey("mot.command.speedtest.msg.2"));
				
				sender.sendChatToPlayer(line);
			}
			case "keyspeed" -> {
				try {
					if (strings.length < 3) {
						if (strings.length == 2) {
							if (strings[1].equals("upSpeedKey")) {
								float upSpeed = upSpeedValue;
								String speed = String.format("%.2f", upSpeed);
								String tick = String.format("%.1f", upSpeed * 20);
								
								sender.sendChatToPlayer(ChatMessageComponent.createFromTranslationWithSubstitutions(
										"mot.key.current",
										Keyboard.getKeyName(upSpeedKey.keyCode),
										speed,
										tick));
								
								return;
							}
							else if (strings[1].equals("downSpeedKey")) {
								float downSpeed = downSpeedValue;
								String speed = String.format("%.2f", downSpeed);
								String tick = String.format("%.1f", downSpeed * 20);
								
								sender.sendChatToPlayer(ChatMessageComponent.createFromTranslationWithSubstitutions(
										"mot.key.current",
										Keyboard.getKeyName(downSpeedKey.keyCode),
										speed,
										tick));
								
								return;
							}
						}
						
						throw new WrongUsageException("/tick keySpeed <upSpeedKey|downSpeedKey> <speed>");
					}
					
					if (strings[1].equals("upSpeedKey")) {
						if (Float.parseFloat(strings[2]) <= 1) {
							sender.sendChatToPlayer(ChatMessageComponent.createFromTranslationKey("mot.upKey.error")
															.setColor(EnumChatFormatting.RED));
							
							return;
						}
						
						float upSpeed = Math.min(250, Float.parseFloat(strings[2]));
						setOptionFloatValue("upSpeedValue", upSpeed);
						
						String speed = String.format("%.2f", upSpeed);
						String tick = String.format("%.1f", upSpeed * 20);
						
						sender.sendChatToPlayer(ChatMessageComponent.createFromTranslationWithSubstitutions(
								"mot.key.set",
								Keyboard.getKeyName(upSpeedKey.keyCode),
								speed,
								tick));
					}
					else if (strings[1].equals("downSpeedKey")) {
						if (Float.parseFloat(strings[2]) >= 1) {
							sender.sendChatToPlayer(ChatMessageComponent.createFromText("mot.downKey.error")
															.setColor(EnumChatFormatting.RED));
							
							return;
						}
						
						float downSpeed = Math.max(0.01F, Float.parseFloat(strings[2]));
						setOptionFloatValue("downSpeedValue", downSpeed);
						
						String speed = String.format("%.2f", downSpeed);
						String tick = String.format("%.1f", downSpeed * 20);
						
						sender.sendChatToPlayer(ChatMessageComponent.createFromTranslationWithSubstitutions(
								"mot.key.set",
								Keyboard.getKeyName(downSpeedKey.keyCode),
								speed,
								tick));
					}
				} catch (NumberFormatException e) {
					throw new WrongUsageException("/tick keySpeed upSpeedKey/downSpeedKey <speed>");
				}
			}
			case "freeze" -> {
				if (MinecraftServer.getServer().isDedicatedServer()) {
					sender.sendChatToPlayer(ChatMessageComponent.createFromTranslationKey("mot.freeze.serverError")
													.setColor(EnumChatFormatting.RED));
					return;
				}
				
				setTickSpeed(0, sender);
			}
			case "playeraffected" -> {
				try {
					if (strings.length < 2) {
						sender.sendChatToPlayer(ChatMessageComponent.createFromTranslationKey(getPlayer(sender,
																										sender.getCommandSenderName()).getData(
								TIME_AFFECTED)
																							  ? "mot.command.playerAffected.affected"
																							  : "mot.command.playerAffected.unaffected"));
						
						return;
					}
					
					boolean affected = Boolean.parseBoolean(strings[1]);
					getPlayer(sender, sender.getCommandSenderName()).setData(TIME_AFFECTED, affected);
					
					if (affected) {
						sender.sendChatToPlayer(ChatMessageComponent.createFromTranslationKey(
								"mot.command.playerAffected.affected"));
					}
					else {
						sender.sendChatToPlayer(ChatMessageComponent.createFromTranslationKey(
								"mot.command.playerAffected.unaffected"));
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
}
