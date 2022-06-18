package nl.marc_apps.streamer.rtsp.connection_state

import io.ktor.network.selector.*
import io.ktor.network.sockets.*
import io.ktor.network.tls.*
import io.ktor.utils.io.*
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.withContext
import nl.marc_apps.streamer.rtsp.commands.CommandsManager
import nl.marc_apps.streamer.rtsp.utils.RtspUrlInfo

class NotConnectedState(
    private val commandsManager: CommandsManager
) : ConnectionState() {
    override val isConnectionOpen = false

    override val isStreaming = false

    private val job = SupervisorJob()

    private var connection: Connection? = null

    override suspend fun setupConnection(url: RtspUrlInfo, actorSelectorManager: ActorSelectorManager): ConnectionState {
        return withContext(job) {
            val socket = aSocket(actorSelectorManager).tcp().connect(url.host, url.port).apply {
                if (url.isTlsEnabled) {
                    tls(coroutineContext)
                }
            }

            val connection = Connection(
                socket,
                socket.openReadChannel(),
                socket.openWriteChannel()
            ).also {
                this@NotConnectedState.connection = it
            }

            val optionsCommand = commandsManager.createOptions()
            commandsManager.logCommand(optionsCommand)
            connection.output.writeStringUtf8(optionsCommand.toString())
            connection.output.flush()

            commandsManager.getResponse(connection.input)

            UnauthorisedConnectionState(commandsManager, connection, url)
        }
    }

    override suspend fun disconnect(): ConnectionState {
        job.cancelAndJoin()
        connection?.let {
            closeConnection(it, commandsManager)
        }
        return NotConnectedState(commandsManager)
    }
}
