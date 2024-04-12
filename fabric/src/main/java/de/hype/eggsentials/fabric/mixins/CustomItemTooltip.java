package de.hype.eggsentials.fabric.mixins;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.gui.screen.ingame.ScreenHandlerProvider;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.slot.Slot;
import net.minecraft.text.Text;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

@Mixin(HandledScreen.class)
public abstract class CustomItemTooltip<T extends ScreenHandler> extends Screen implements ScreenHandlerProvider<T> {

    @Shadow
    @Final
    protected T handler;
    @Shadow
    @Nullable
    protected Slot focusedSlot;
    @Shadow
    @Final
    protected Text playerInventoryTitle;

    protected CustomItemTooltip(Text title) {
        super(title);
    }

    @Shadow
    protected abstract List<Text> getTooltipFromItem(ItemStack stack);

    @ModifyExpressionValue(method = "drawMouseoverTooltip", at = @At(value = "INVOKE", target = "Lnet/minecraft/screen/slot/Slot;getStack()Lnet/minecraft/item/ItemStack;"))
    private ItemStack modfiedItemStack(ItemStack original) {
        return original;
    }

    @Inject(method = "<init>", at = @At("RETURN"))
    private void onConstructor(ScreenHandler handler, PlayerInventory inventory, Text title, CallbackInfo ci) {

    }

//    @Inject(method = "render", at = @At("RETURN"))
//    public void render(DrawContext context, int mouseX, int mouseY, float delta, CallbackInfo ci) {
//        if (Eggsentials.splashConfig.smallestHubName == null) return;
//        context.drawText(textRenderer, getSmallestHubText(), 10, 50, 16777215, false);
//    }


    @Inject(method = "close", at = @At("HEAD"))
    private void onClose(CallbackInfo ci) {
    }
}
