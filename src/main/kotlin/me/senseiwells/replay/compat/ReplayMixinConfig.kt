package me.senseiwells.replay.compat

import com.google.common.collect.HashMultimap
import net.fabricmc.loader.api.FabricLoader
import org.objectweb.asm.tree.ClassNode
import org.spongepowered.asm.mixin.extensibility.IMixinConfigPlugin
import org.spongepowered.asm.mixin.extensibility.IMixinInfo

class ReplayMixinConfig: IMixinConfigPlugin {
    companion object {
        private const val MIXIN_COMPAT = "me.senseiwells.replay.mixin.compat."

        private val incompatible = HashMultimap.create<String, String>()

        init {
            this.incompatible.put("me.senseiwells.replay.mixin.chunk.ChunkMapMixin", "c2me")
            this.incompatible.put("me.senseiwells.replay.mixin.chunk.ChunkHolderMixin", "c2me")
        }
    }

    override fun onLoad(mixinPackage: String?) {

    }

    override fun getRefMapperConfig(): String? {
        return null
    }

    override fun shouldApplyMixin(targetClassName: String, mixinClassName: String): Boolean {
        if (mixinClassName.startsWith(MIXIN_COMPAT)) {
            val modId = mixinClassName.removePrefix(MIXIN_COMPAT).substringBefore('.')
            return FabricLoader.getInstance().isModLoaded(modId)
        }
        for (modId in incompatible.get(mixinClassName)) {
            if (FabricLoader.getInstance().isModLoaded(modId)) {
                return false
            }
        }
        return true
    }

    override fun acceptTargets(myTargets: MutableSet<String>, otherTargets: MutableSet<String>) {

    }

    override fun getMixins(): MutableList<String>? {
        return null
    }

    override fun preApply(
        targetClassName: String,
        targetClass: ClassNode,
        mixinClassName: String,
        mixinInfo: IMixinInfo
    ) {

    }

    override fun postApply(
        targetClassName: String,
        targetClass: ClassNode,
        mixinClassName: String,
        mixinInfo: IMixinInfo
    ) {

    }
}