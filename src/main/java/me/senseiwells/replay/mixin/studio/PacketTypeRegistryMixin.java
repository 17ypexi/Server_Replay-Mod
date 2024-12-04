package me.senseiwells.replay.mixin.studio;

import com.replaymod.replaystudio.lib.viaversion.api.protocol.packet.State;
import com.replaymod.replaystudio.lib.viaversion.api.protocol.version.ProtocolVersion;
import com.replaymod.replaystudio.protocol.PacketTypeRegistry;
import me.senseiwells.replay.compat.via.TempProtocolVersion;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.EnumMap;
import java.util.Map;

// TODO: Remove this when updating ReplayStudio
@Deprecated
@Mixin(value = PacketTypeRegistry.class, remap = false)
public class PacketTypeRegistryMixin {
    @Shadow private static Map<ProtocolVersion, EnumMap<State, PacketTypeRegistry>> forVersionAndState;

    @Inject(
        method = "<clinit>",
        at = @At("HEAD")
    )
    private static void preClinit(CallbackInfo ci) {
        TempProtocolVersion.noop();
    }

    @Inject(
        method = "<clinit>",
        at = @At("TAIL")
    )
    private static void postClinit(CallbackInfo ci) {
        forVersionAndState.put(TempProtocolVersion.v1_21_4, forVersionAndState.get(ProtocolVersion.v1_21_2));
    }
}
