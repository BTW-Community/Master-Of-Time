package net.dravigen.master_of_time.mixin;

import net.minecraft.src.*;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import static net.dravigen.master_of_time.MasterOfTimeAddon.bCreateWorld;
import static net.dravigen.master_of_time.MasterOfTimeAddon.originalTickSpeed;

@Mixin(GuiCreateWorld.class)
public abstract class GuiCreateWorldMx extends GuiScreen {
	@Unique
	boolean bCanCreate = true;
	@Unique
	private GuiTextField textboxTickSpeed;
	@Unique
	private String warningText = "";
	@Unique
	private String warningText1 = "";
	@Unique
	private int color = 0x880808;
	
	@Inject(method = "initGui", at = @At(value = "INVOKE", target = "Lnet/minecraft/src/GuiTextField;setText(Ljava/lang/String;)V"))
	public void initGui(CallbackInfo ci) {
		originalTickSpeed = 1;
		this.textboxTickSpeed = new GuiTextField(this.fontRenderer, 5, 60, 50, 20);
		this.textboxTickSpeed.setText(String.valueOf(originalTickSpeed));
	}
	
	@Inject(method = "updateScreen", at = @At("TAIL"))
	public void updateScreen(CallbackInfo ci) {
		this.textboxTickSpeed.updateCursorCounter();
	}
	
	@Inject(method = "drawScreen", at = @At(value = "INVOKE", target = "Lnet/minecraft/src/GuiScreen;drawScreen(IIF)V"))
	private void drawScreen(int mouseX, int mouseY, float partialTicks, CallbackInfo ci) {
		this.drawString(this.fontRenderer, I18n.getString("mot.worldCreation.originalSpeed"), 4, 47, 0xA0A0A0);
		
		if (!warningText.isEmpty()) {
			this.drawString(this.fontRenderer, I18n.getString(warningText), 4, 85, color);
		}
		if (!warningText1.isEmpty()) {
			this.drawString(this.fontRenderer,
							I18n.getString(warningText1),
							4,
							85 + this.fontRenderer.FONT_HEIGHT,
							color);
		}
		
		this.textboxTickSpeed.drawTextBox();
	}
	
	@Inject(method = "mouseClicked", at = @At(value = "INVOKE", target = "Lnet/minecraft/src/GuiTextField;mouseClicked(III)V"))
	private void mouseClicked(int mouseX, int mouseY, int mouseButton, CallbackInfo ci) {
		this.textboxTickSpeed.mouseClicked(mouseX, mouseY, mouseButton);
	}
	
	@Inject(method = "keyTyped", at = @At(value = "TAIL"))
	private void keyTyped(char par1, int par2, CallbackInfo ci) {
		if (this.textboxTickSpeed.isFocused()) {
			this.textboxTickSpeed.textboxKeyTyped(par1, par2);
			
			try {
				originalTickSpeed = Float.parseFloat(this.textboxTickSpeed.getText());
				
				if (originalTickSpeed < 0.01F) {
					warningText = I18n.getString("mot.worldCreation.warning.tooLow");
					warningText1 = I18n.getString("mot.worldCreation.warning.tooLow1");
					color = 0x880808;
					bCanCreate = false;
				}
				else if (originalTickSpeed < 1) {
					warningText = I18n.getString("mot.worldCreation.warning.lowNumber");
					warningText1 = I18n.getString("mot.worldCreation.warning.lowNumber1");
					color = 0xFFFF00;
					bCanCreate = true;
				}
				else {
					warningText = "";
					warningText1 = "";
					bCanCreate = true;
				}
			} catch (NumberFormatException e) {
				originalTickSpeed = 1;
				warningText = I18n.getString("mot.worldCreation.warning.notNumber");
				color = 0x880808;
				bCanCreate = false;
			}
		}
		
		((GuiButton) this.buttonList.get(0)).enabled = bCanCreate;
	}
	
	@Inject(method = "actionPerformed", at = @At(value = "INVOKE", target = "Lnet/minecraft/src/WorldSettings;func_82750_a(Ljava/lang/String;)Lnet/minecraft/src/WorldSettings;"), locals = LocalCapture.CAPTURE_FAILHARD)
	private void actionPerformed(GuiButton par1GuiButton, CallbackInfo ci, long seed, String var4,
			EnumGameType gameType, WorldSettings settings) {
		originalTickSpeed = Float.parseFloat(this.textboxTickSpeed.getText());
		bCreateWorld = true;
	}
}
