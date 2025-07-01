package net.dravigen.time_master;

import btw.AddonHandler;
import btw.BTWAddon;
import btw.world.util.data.DataEntry;
import btw.world.util.data.DataProvider;
import net.minecraft.server.MinecraftServer;
import net.minecraft.src.*;
import org.lwjgl.input.Keyboard;
import java.util.List;

public class TimeMasterAddon extends BTWAddon {

    public static KeyBinding reset_time_speed_key;
    public static KeyBinding increase_time_speed_key;
    public static KeyBinding decrease_time_speed_key;
    public static float worldSpeedModifier = 1F;
    public static boolean warned= false;
    public static boolean currentSpeedTest = false;
    public static boolean maxSpeedTest = false;
    public static double tps;

    public static DataEntry<Float> INCREASE_VALUE = DataProvider.getBuilder(float.class)
            .global()
            .name("increase_value")
            .defaultSupplier(() -> (float) 10)
            .readNBT(NBTTagCompound::getFloat)
            .writeNBT(NBTTagCompound::setFloat)
            .build();

    public static DataEntry<Float> DECREASE_VALUE = DataProvider.getBuilder(float.class)
            .global()
            .name("decrease_value")
            .defaultSupplier(() -> (float) 0.25)
            .readNBT(NBTTagCompound::getFloat)
            .writeNBT(NBTTagCompound::setFloat)
            .build();

    public TimeMasterAddon() {
        super();
    }

    public static class TMChannel {
        public static final String CLIENT_TO_SERVER_CHANNEL = "T-M:C2S";
        public static final String SERVER_TO_CLIENT_CHANNEL = "T-M:S2C";
    }


    @Override
    public void initialize() {
        AddonHandler.logMessage(this.getName() + " Version " + this.getVersionString() + " Initializing...");
        createNewCommand();
        if (!MinecraftServer.getIsServer()) {
            initKeybind();
        }
    }

    private void createNewCommand() {
        registerAddonCommand(new CommandBase() {
            @Override
            public String getCommandName() {
                return "timemaster";
            }

            @Override
            public String getCommandUsage(ICommandSender iCommandSender) {
                return "/timemaster <set/<reset>/<speedtest>/<maxspeedtest>/<keybindsvalue, <increasevalue, value>/<decreasevalue, value>>, speedModifier>";
            }
            @SuppressWarnings("rawtypes")
            @Override
            public List addTabCompletionOptions(ICommandSender par1ICommandSender, String[] par2ArrayOfStr) {
                if (par2ArrayOfStr.length==1){
                    return getListOfStringsMatchingLastWord(par2ArrayOfStr, "set","reset","speedtest", "maxspeedtest", "keybindsvalue" );
                } else if (par2ArrayOfStr.length == 2 && par2ArrayOfStr[0].equals("keybindsvalue")) {
                    return getListOfStringsMatchingLastWord(par2ArrayOfStr,"increasevalue","decreasevalue");
                }
                return null;
            }
            @Override
            public void processCommand(ICommandSender iCommandSender, String[] strings) {
                switch (strings[0]) {
                    case "set" -> {
                        try {
                            float speedModifier = Float.parseFloat(strings[1]);
                            if (speedModifier > 250) {
                                speedModifier = 250;
                                worldSpeedModifier = speedModifier;
                                iCommandSender.sendChatToPlayer(ChatMessageComponent.createFromText("Your value was too high ! The world speed got forcefully set to the maximum value available: 250x"));
                            } else if (speedModifier < 0.05F) {
                                speedModifier = 0.05F;
                                worldSpeedModifier = speedModifier;
                                iCommandSender.sendChatToPlayer(ChatMessageComponent.createFromText("Your value was too low ! The world speed got forcefully set to the minimum value available: 0.05x"));
                            } else {
                                worldSpeedModifier = speedModifier;
                                iCommandSender.sendChatToPlayer(ChatMessageComponent.createFromText("The world speed got set to " + speedModifier + "x"));
                            }
                            if (speedModifier < 0.25 && !warned) {
                                iCommandSender.sendChatToPlayer(ChatMessageComponent.createFromText("-----------------------"));
                                iCommandSender.sendChatToPlayer(ChatMessageComponent.createFromText("   WARNING MESSAGE !"));
                                iCommandSender.sendChatToPlayer(ChatMessageComponent.createFromText("Slowing down the world further than 0.25 can have unexpected results, beware !"));
                                iCommandSender.sendChatToPlayer(ChatMessageComponent.createFromText("-----------------------"));

                                warned = true;
                            }

                        } catch (NumberFormatException e) {
                            throw new WrongUsageException("Invalid command.");
                        }
                    }
                    case "reset" -> {
                        try {
                            worldSpeedModifier = 1.0F;
                            iCommandSender.sendChatToPlayer(ChatMessageComponent.createFromText("The world speed got reset"));
                        } catch (NumberFormatException e) {
                            throw new WrongUsageException("Invalid command.");

                        }
                    }
                    case "speedtest" -> {
                        try {
                            currentSpeedTest = true;
                            iCommandSender.sendChatToPlayer(ChatMessageComponent.createFromText(" "));
                            iCommandSender.sendChatToPlayer(ChatMessageComponent.createFromText("Testing the current speed of your game, it will take 10 secs..."));
                            iCommandSender.sendChatToPlayer(ChatMessageComponent.createFromText("--------------------------------------"));
                            iCommandSender.sendChatToPlayer(ChatMessageComponent.createFromText("DON'T PAUSE THE GAME DURING THE PROCESS !"));
                            iCommandSender.sendChatToPlayer(ChatMessageComponent.createFromText("--------------------------------------"));
                        } catch (NumberFormatException e) {
                            throw new WrongUsageException("Invalid command.");

                        }
                    }
                    case "maxspeedtest" -> {
                        try {
                            worldSpeedModifier = 500F;
                            currentSpeedTest = true;
                            maxSpeedTest = true;
                            iCommandSender.sendChatToPlayer(ChatMessageComponent.createFromText(" "));
                            iCommandSender.sendChatToPlayer(ChatMessageComponent.createFromText("Testing the highest speed your pc could handle, it will take 25 secs..."));
                            iCommandSender.sendChatToPlayer(ChatMessageComponent.createFromText("--------------------------------------"));
                            iCommandSender.sendChatToPlayer(ChatMessageComponent.createFromText("DON'T PAUSE THE GAME DURING THE PROCESS !"));
                            iCommandSender.sendChatToPlayer(ChatMessageComponent.createFromText("--------------------------------------"));
                        } catch (NumberFormatException e) {
                            throw new WrongUsageException("Invalid command.");

                        }
                    }
                    case "keybindsvalue" -> {
                        try {
                            WorldServer worldServer = MinecraftServer.getServer().worldServers[0];
                            if (strings[1].equals("increasevalue")) {
                                worldServer.setData(INCREASE_VALUE, Math.min(250, Float.parseFloat(strings[2])));
                                iCommandSender.sendChatToPlayer(ChatMessageComponent.createFromText("By pressing " + Keyboard.getKeyName(increase_time_speed_key.keyCode) + ", the world speed will be set to " + worldServer.getData(INCREASE_VALUE) + "x"));
                            } else if (strings[1].equals("decreasevalue")) {
                                worldServer.setData(DECREASE_VALUE, Math.max(0.05F, Float.parseFloat(strings[2])));
                                iCommandSender.sendChatToPlayer(ChatMessageComponent.createFromText("By pressing " + Keyboard.getKeyName(decrease_time_speed_key.keyCode) + ", the world speed will be set to " + worldServer.getData(DECREASE_VALUE) + "x"));
                            }
                        } catch (NumberFormatException e) {
                            throw new WrongUsageException("Invalid command.");

                        }
                    }
                }
            }
        });
    }

    public void initKeybind(){
        reset_time_speed_key = new KeyBinding(StatCollector.translateToLocal("Reset World Speed"), Keyboard.KEY_R);
        increase_time_speed_key = new KeyBinding(StatCollector.translateToLocal("Increase World Speed"), Keyboard.KEY_F);
        decrease_time_speed_key = new KeyBinding(StatCollector.translateToLocal("Decrease World Speed"), Keyboard.KEY_G);
    }

    @Override
    public void preInitialize() {
        INCREASE_VALUE.register();
        DECREASE_VALUE.register();
    }

}


