package me.senseiwells.replay.util

import me.senseiwells.replay.ServerReplay
import me.senseiwells.replay.recorder.ReplayRecorder
import net.minecraft.network.protocol.Packet
import net.minecraft.network.protocol.game.*
import net.minecraft.network.protocol.login.ClientboundLoginCompressionPacket
import net.minecraft.server.level.ServerLevel
import net.minecraft.world.entity.Entity
import net.minecraft.world.entity.item.PrimedTnt
import net.minecraft.world.entity.projectile.Projectile

object ReplayOptimizerUtils {
    // Set of packets that are ignored by replay mod
    private val IGNORED = setOf<Class<out Packet<*>>>(
        ClientboundBlockChangedAckPacket::class.java,
        ClientboundOpenBookPacket::class.java,
        ClientboundOpenScreenPacket::class.java,
        ClientboundUpdateRecipesPacket::class.java,
        ClientboundUpdateAdvancementsPacket::class.java,
        ClientboundSelectAdvancementsTabPacket::class.java,
        ClientboundSetCameraPacket::class.java,
        ClientboundHorseScreenOpenPacket::class.java,
        ClientboundContainerClosePacket::class.java,
        ClientboundContainerSetSlotPacket::class.java,
        ClientboundContainerSetDataPacket::class.java,
        ClientboundOpenSignEditorPacket::class.java,
        ClientboundAwardStatsPacket::class.java,
        ClientboundSetExperiencePacket::class.java,
        ClientboundPlayerAbilitiesPacket::class.java,
        ClientboundLoginCompressionPacket::class.java,
        ClientboundCommandSuggestionsPacket::class.java,
        ClientboundCustomChatCompletionsPacket::class.java,
        ClientboundCommandsPacket::class.java
    )
    // Set of all chat related packs
    private val CHAT = setOf<Class<out Packet<*>>>(
        ClientboundPlayerChatPacket::class.java,
        ClientboundDeleteChatPacket::class.java,
        ClientboundSystemChatPacket::class.java,
        ClientboundDisguisedChatPacket::class.java
    )
    // Set of all scoreboard-related packets
    private val SCOREBOARD = setOf<Class<out Packet<*>>>(
        ClientboundSetScorePacket::class.java,
        ClientboundResetScorePacket::class.java,
        ClientboundSetObjectivePacket::class.java,
        ClientboundSetDisplayObjectivePacket::class.java
    )
    // Set of all sound related packets
    private val SOUNDS = setOf<Class<out Packet<*>>>(
        ClientboundSoundPacket::class.java,
        ClientboundSoundEntityPacket::class.java
    )
    // Set of all packets related to entity movement
    private val ENTITY_MOVEMENT = setOf<Class<out Packet<*>>>(
        ClientboundMoveEntityPacket.Pos::class.java,
        ClientboundTeleportEntityPacket::class.java,
        ClientboundSetEntityMotionPacket::class.java,
        ClientboundTeleportEntityPacket::class.java
    )
    private val ENTITY_MAPPERS = HashMap<Class<*>, (Any, ServerLevel) -> Entity?>()

    init {
        this.addEntityPacket(ClientboundEntityEventPacket::class.java) { packet, level -> packet.getEntity(level) }
        this.addEntityPacket(ClientboundMoveEntityPacket.Pos::class.java) { packet, level -> packet.getEntity(level) }
        this.addEntityPacket(ClientboundSetEntityDataPacket::class.java) { packet, level -> level.getEntity(packet.id) }
        this.addEntityPacket(ClientboundTeleportEntityPacket::class.java) { packet, level -> level.getEntity(packet.id) }
        this.addEntityPacket(ClientboundSetEntityDataPacket::class.java) { packet, level -> level.getEntity(packet.id) }
        this.addEntityPacket(ClientboundSetEntityMotionPacket::class.java) { packet, level -> level.getEntity(packet.id) }
        this.addEntityPacket(ClientboundTeleportEntityPacket::class.java) { packet, level -> level.getEntity(packet.id) }
    }

    fun optimisePackets(recorder: ReplayRecorder, packet: Packet<*>): Boolean {
        if (ServerReplay.config.optimizeEntityPackets) {
            if (this.optimiseEntity(recorder, packet)) {
                return true
            }
        }

        if (ServerReplay.config.ignoreLightPackets && packet is ClientboundLightUpdatePacket) {
            return true
        }

        val time = ServerReplay.config.fixedDaylightCycle
        if (time >= 0 && packet is ClientboundSetTimePacket && packet.dayTime != -time) {
            recorder.record(ClientboundSetTimePacket(packet.gameTime, time, false))
            return true
        }

        val type = packet::class.java
        if (ServerReplay.config.ignoreSoundPackets && SOUNDS.contains(type)) {
            return true
        }
        if (ServerReplay.config.ignoreChatPackets && CHAT.contains(type)) {
            if (!(packet is ClientboundSystemChatPacket && packet.overlay)) {
                return true
            }
        }
        if (ServerReplay.config.ignoreActionBarPackets) {
            if (packet is ClientboundSystemChatPacket && packet.overlay || packet is ClientboundSetActionBarTextPacket) {
                return true
            }
        }
        if (ServerReplay.config.ignoreScoreboardPackets && SCOREBOARD.contains(type)) {
            return true
        }
        return IGNORED.contains(type)
    }

    private fun optimiseEntity(recorder: ReplayRecorder, packet: Packet<*>): Boolean {
        val type = packet::class.java
        val mapper = ENTITY_MAPPERS[type] ?: return false
        val entity = mapper(packet, recorder.level) ?: return false

        // The client can calculate TNT and projectile movement itself.
        if (entity is PrimedTnt) {
            return true
        }
        if (entity is Projectile && ENTITY_MOVEMENT.contains(type)) {
            return true
        }
        return false
    }

    @Suppress("UNCHECKED_CAST")
    private fun <T: Any> addEntityPacket(type: Class<T>, getter: (T, ServerLevel) -> Entity?) {
        this.ENTITY_MAPPERS[type] = getter as (Any, ServerLevel) -> Entity?
    }
}