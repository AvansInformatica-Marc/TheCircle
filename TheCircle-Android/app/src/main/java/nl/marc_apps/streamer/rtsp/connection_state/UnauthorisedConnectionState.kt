package nl.marc_apps.streamer.rtsp.connection_state

import android.util.Log
import io.ktor.network.sockets.*
import io.ktor.utils.io.*
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.withContext
import nl.marc_apps.streamer.rtsp.commands.CommandsManager
import nl.marc_apps.streamer.rtsp.utils.RtspUrlInfo

class UnauthorisedConnectionState(
    private val commandsManager: CommandsManager,
    private val connection: Connection,
    private val url: RtspUrlInfo
) : ConnectionState() {
    override val isConnectionOpen = true

    override val isStreaming = false

    private val job = SupervisorJob()

    override suspend fun authorise(user: String?, password: String?): ConnectionState {
        return withContext(job) {
            val announceCommand = commandsManager.createAnnounce()
            commandsManager.logCommand(announceCommand)
            connection.output.writeStringUtf8(announceCommand.toString())
            connection.output.flush()

            authoriseToStream(user, password)

            UninitialisedConnectionState(commandsManager, connection, url)
        }
    }

    private suspend fun authoriseToStream(user: String?, password: String?) = coroutineScope {
        val announceResponse = commandsManager.getResponse(connection.input)
        when (announceResponse.statusCode) {
            STATUS_ACCESS_DENIED -> {
                Log.e(TAG, "Response 403, access denied")
                throw IllegalStateException("access denied")
            }
            STATUS_UNAUTHORISED -> {
                if (user == null || password == null) {
                    throw IllegalStateException("authorisation required: provide username and password")
                }

                val announceWithAuthCommand = commandsManager.createAnnounceWithAuth(announceResponse.headers, user, password)
                commandsManager.logCommand(announceWithAuthCommand)
                connection.output.writeStringUtf8(announceWithAuthCommand.toString())
                connection.output.flush()

                when (commandsManager.getResponse(connection.input).statusCode) {
                    STATUS_UNAUTHORISED -> {
                        throw IllegalStateException("authorisation invalid")
                    }
                    STATUS_SUCCESS -> {}
                    else -> throw IllegalStateException("announce with auth failed")
                }
            }
            STATUS_SUCCESS -> {}
            else -> throw IllegalStateException("announce failed")
        }
    }

    override suspend fun disconnect(): ConnectionState {
        job.cancelAndJoin()
        closeConnection(connection, commandsManager)
        return NotConnectedState(commandsManager)
    }

    companion object {
        private const val TAG = "UnauthorisedConnectionState"

        private const val STATUS_ACCESS_DENIED = 403

        private const val STATUS_UNAUTHORISED = 401

        private const val STATUS_SUCCESS = 200
    }
}
