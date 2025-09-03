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
    public static KeyBinding freeze_time_speed_key;
    public static KeyBinding step_time_key;

    public static volatile float worldSpeedModifier = 1F;
    public static boolean currentSpeedTest = false;
    public static boolean maxSpeedTest = false;
    public static double tps;
    public static boolean step = false;

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
            .defaultSupplier(()-> new float[]{10f,0.25f})
            .readNBT(tag -> {
                if(!tag.hasKey(MASTER_OF_TIME_DATA_NAME)) {
                    NBTTagCompound defaultValue = new NBTTagCompound();
                    defaultValue.setFloat("increaseValue", 10f);
                    defaultValue.setFloat("decreaseValue",0.25f);
                    tag.setCompoundTag(MASTER_OF_TIME_DATA_NAME, defaultValue);
                }
                NBTTagCompound value = tag.getCompoundTag(MASTER_OF_TIME_DATA_NAME);
                return new float[]{value.getFloat("increaseValue"),value.getFloat("decreaseValue")};
            })
            .writeNBT((tag, value) -> {
                NBTTagCompound newValue = new NBTTagCompound();
                newValue.setFloat("increaseValue", value[0]);
                newValue.setFloat("decreaseValue", value[1]);
                tag.setCompoundTag(MASTER_OF_TIME_DATA_NAME, newValue);
            })
            .global()
            .build();

    public static float getIncreaseValue(WorldServer server){
        return server.getData(MASTER_OF_TIME_DATA)[0];
    }
    public static float getDecreaseValue(WorldServer server){
        return server.getData(MASTER_OF_TIME_DATA)[1];
    }
    public static void setIncreaseValue(WorldServer server, float value){
        server.setData(MASTER_OF_TIME_DATA,new float[]{value,server.getData(MASTER_OF_TIME_DATA)[1]});
    }
    public static void setDecreaseValue(WorldServer server, float value){
        server.setData(MASTER_OF_TIME_DATA,new float[]{server.getData(MASTER_OF_TIME_DATA)[0],value});
    }

    public TimeMasterAddon() {
        super();
    }

    public static class TMChannel {
        public static final String CLIENT_TO_SERVER_CHANNEL = "MoT:C2S";
        //public static final String SERVER_TO_CLIENT_CHANNEL = "MoT:S2C";
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
                return "tick";
            }

            @Override
            public String getCommandUsage(ICommandSender iCommandSender) {
                return "/tick <rate> <speed> OR /tick <reset> OR /tick <freeze> OR /tick <speedtest> OR /tick <maxspeedtest> OR /tick <keybindsvalue> <increasevalue/decreasevalue> <speed> OR /tick <playerAffected> <true/false>";
            }
            @SuppressWarnings("rawtypes")
            @Override
            public List addTabCompletionOptions(ICommandSender par1ICommandSender, String[] par2ArrayOfStr) {
                if (par2ArrayOfStr.length==1){
                    return getListOfStringsMatchingLastWord(par2ArrayOfStr, "rate","reset","freeze","speedtest", "maxspeedtest", "keybindsvalue", "playerAffected" );
                } else if (par2ArrayOfStr.length == 2 && par2ArrayOfStr[0].equalsIgnoreCase("keybindsvalue")) {
                    return getListOfStringsMatchingLastWord(par2ArrayOfStr,"increasevalue","decreasevalue");
                } else if (par2ArrayOfStr.length == 2 && par2ArrayOfStr[0].equalsIgnoreCase("playeraffected")) {
                    return getListOfStringsMatchingLastWord(par2ArrayOfStr,"true","false");
                }
                return null;
            }


            public static boolean warned = false;
            public static boolean freezeWarned = false;

            public static long lastWarned;

            @Override
            public void processCommand(ICommandSender sender, String[] strings) {
                WorldServer worldServer = MinecraftServer.getServer().worldServers[0];
                switch (strings[0].toLowerCase()) {
                    case "rate" -> {
                        try {
                            float speedModifier = Float.parseFloat(strings[1]);
                            /*
                            if (speedModifier > 250) {
                                speedModifier = 250;
                                worldSpeedModifier = speedModifier;
                                sender.sendChatToPlayer(ChatMessageComponent.createFromText("Your value was too high ! The world speed got forcefully set to the maximum value available: 250x"));
                            }*/
                            if (speedModifier < 0.05 && !warned) {
                                if (MinecraftServer.getServer().isDedicatedServer()) {
                                    sender.sendChatToPlayer(ChatMessageComponent.createFromText("-----------------------").setColor(EnumChatFormatting.RED));
                                    sender.sendChatToPlayer(ChatMessageComponent.createFromText("   WARNING MESSAGE !").setColor(EnumChatFormatting.RED));
                                    sender.sendChatToPlayer(ChatMessageComponent.createFromText("Slowing down tick rate this low can cause issue to revert back to normal through commands").setColor(EnumChatFormatting.RED));
                                    //sender.sendChatToPlayer(ChatMessageComponent.createFromText("please use the server's cmd to enter: \"reset\"").setColor(EnumChatFormatting.RED));
                                    sender.sendChatToPlayer(ChatMessageComponent.createFromText("Re-enter the command to confirm").setColor(EnumChatFormatting.GREEN));
                                    sender.sendChatToPlayer(ChatMessageComponent.createFromText("-----------------------").setColor(EnumChatFormatting.RED));
                                    warned = true;
                                    return;
                                }
                            }
                            if (speedModifier < 0.01F) {
                                speedModifier = 0.01F;
                                worldSpeedModifier = speedModifier;
                                sender.sendChatToPlayer(ChatMessageComponent.createFromText("Your value was too low ! The tick rate got forcefully set to the minimum value available: 0.01x (0.2 t/s)"));
                            }
                            //else {
                                worldSpeedModifier = speedModifier;
                                sender.sendChatToPlayer(ChatMessageComponent.createFromText("The tick rate 'goal' got set to " + speedModifier + "x (" + String.format("%.3f", speedModifier*20) + " t/s)"));
                            //}


                        } catch (NumberFormatException e) {
                            throw new WrongUsageException("Invalid command.");
                        }
                    }
                    case "reset" -> {
                        try {
                            worldSpeedModifier = 1.0F;
                            sender.sendChatToPlayer(ChatMessageComponent.createFromText("The game is running normally"));
                        } catch (NumberFormatException e) {
                            throw new WrongUsageException("Invalid command.");

                        }
                    }
                    case "speedtest" -> {
                        try {
                            currentSpeedTest = true;
                            sender.sendChatToPlayer(ChatMessageComponent.createFromText(" "));
                            sender.sendChatToPlayer(ChatMessageComponent.createFromText("Testing the current speed of your game, it will take 10 secs..."));
                            sender.sendChatToPlayer(ChatMessageComponent.createFromText("--------------------------------------"));
                            sender.sendChatToPlayer(ChatMessageComponent.createFromText("DON'T PAUSE THE GAME DURING THE PROCESS !"));
                            sender.sendChatToPlayer(ChatMessageComponent.createFromText("--------------------------------------"));
                        } catch (NumberFormatException e) {
                            throw new WrongUsageException("Invalid command.");

                        }
                    }
                    case "maxspeedtest" -> {
                        try {
                            worldSpeedModifier = 500F;
                            currentSpeedTest = true;
                            maxSpeedTest = true;
                            sender.sendChatToPlayer(ChatMessageComponent.createFromText(" "));
                            sender.sendChatToPlayer(ChatMessageComponent.createFromText("Testing the highest speed your pc could handle, it will take 25 secs..."));
                            sender.sendChatToPlayer(ChatMessageComponent.createFromText("--------------------------------------"));
                            sender.sendChatToPlayer(ChatMessageComponent.createFromText("DON'T PAUSE THE GAME DURING THE PROCESS !"));
                            sender.sendChatToPlayer(ChatMessageComponent.createFromText("--------------------------------------"));
                        } catch (NumberFormatException e) {
                            throw new WrongUsageException("Invalid command.");

                        }
                    }
                    case "keybindsvalue" -> {
                        try {
                            //float tickRate = Float.parseFloat(strings[2]);
                            if (strings[1].equals("increasevalue")) {
                                setIncreaseValue(worldServer, Math.min(250, Float.parseFloat(strings[2])));
                                sender.sendChatToPlayer(ChatMessageComponent.createFromText("By pressing " + Keyboard.getKeyName(increase_time_speed_key.keyCode) + ", the tick rate 'goal' will be set to " + getIncreaseValue(worldServer)*20 + "x (" + String.format("%.3f", getIncreaseValue(worldServer)) + " t/s)"));
                            } else if (strings[1].equals("decreasevalue")) {
                                setDecreaseValue(worldServer, Math.max(0.01F, Float.parseFloat(strings[2])));
                                sender.sendChatToPlayer(ChatMessageComponent.createFromText("By pressing " + Keyboard.getKeyName(decrease_time_speed_key.keyCode) + ", the tick rate 'goal' will be set to " + getDecreaseValue(worldServer)*20 + "x (" + String.format("%.3f", getDecreaseValue(worldServer)) + " t/s)"));
                            }
                        } catch (NumberFormatException e) {
                            throw new WrongUsageException("Invalid command.");

                        }
                    }
                    case "freeze" -> {
                        if (MinecraftServer.getServer().isDedicatedServer()){
                            /*
                            if (!freezeWarned||(lastWarned+30000<System.currentTimeMillis()&&freezeWarned)){
                                sender.sendChatToPlayer(ChatMessageComponent.createFromText("-----------------------").setColor(EnumChatFormatting.RED));
                                sender.sendChatToPlayer(ChatMessageComponent.createFromText("   WARNING MESSAGE !").setColor(EnumChatFormatting.RED));
                                sender.sendChatToPlayer(ChatMessageComponent.createFromText("You will freeze the game, the only way to unfreeze is by inputting: \"reset\" in the server's cmd.").setColor(EnumChatFormatting.RED));
                                sender.sendChatToPlayer(ChatMessageComponent.createFromText("-----------------------").setColor(EnumChatFormatting.RED));
                                sender.sendChatToPlayer(ChatMessageComponent.createFromText("Re-enter the command within 30s to confirm...").setColor(EnumChatFormatting.GREEN));
                                freezeWarned=true;
                                lastWarned=System.currentTimeMillis();

                            }*/
                            sender.sendChatToPlayer(ChatMessageComponent.createFromText("Freezing the server doesn't work properly").setColor(EnumChatFormatting.RED));
                            return;
                        }
                        worldSpeedModifier = 0;
                        sender.sendChatToPlayer(ChatMessageComponent.createFromText("The game is frozen"));
                        freezeWarned=false;
                        lastWarned=0;
                    }
                    case "playeraffected"->{
                        try {
                            boolean affected = Boolean.parseBoolean(strings[1]);
                            getPlayer(sender,sender.getCommandSenderName()).setData(TIME_AFFECTED,affected);
                            if (affected) sender.sendChatToPlayer(ChatMessageComponent.createFromText("You are now no longer affected by modified tick rate"));
                            else sender.sendChatToPlayer(ChatMessageComponent.createFromText("You are now affected by modified tick rate"));
                        } catch (Exception ignored) {
                            throw new WrongUsageException("Invalid command.");
                        }
                    }
                }
            }
        });
    }

    public void initKeybind(){
        reset_time_speed_key = new KeyBinding(StatCollector.translateToLocal("Reset time speed"), Keyboard.KEY_R);
        increase_time_speed_key = new KeyBinding(StatCollector.translateToLocal("Speed up time"), Keyboard.KEY_G);
        decrease_time_speed_key = new KeyBinding(StatCollector.translateToLocal("Slow down time"), Keyboard.KEY_V);
        freeze_time_speed_key = new KeyBinding(StatCollector.translateToLocal("Freeze time"), Keyboard.KEY_F);
        step_time_key = new KeyBinding(StatCollector.translateToLocal("Step 1 tick"), Keyboard.KEY_N);

    }

    @Override
    public void preInitialize() {
        //INCREASE_VALUE.register();
        //DECREASE_VALUE.register();
        MASTER_OF_TIME_DATA.register();
        TIME_AFFECTED.register();
    }

}


