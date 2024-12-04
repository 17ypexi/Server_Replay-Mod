package me.senseiwells.replay.compat.via

import com.replaymod.replaystudio.lib.viaversion.api.protocol.version.ProtocolVersion

@Deprecated("Temporary, until ReplayStudio/ViaVersion is updated")
object TempProtocolVersion {
    @JvmField
    val v1_21_4: ProtocolVersion = ProtocolVersion.register(769, "1.21.4")

    @JvmStatic
    fun noop() {

    }
}