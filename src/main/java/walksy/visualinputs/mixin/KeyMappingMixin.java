package walksy.visualinputs.mixin;

import net.minecraft.client.KeyMapping;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import walksy.visualinputs.VisualInputs;

@Mixin(KeyMapping.class)
public class KeyMappingMixin {
    @Inject(method = "consumeClick", at = @At("RETURN"))
    private void visualinputs$onConsumeClick(final CallbackInfoReturnable<Boolean> cir) {
        if (cir.getReturnValueZ()) {
            VisualInputs.TIMELINE.onConsume(KeyMapping.class.cast(this));
        }
    }
}
