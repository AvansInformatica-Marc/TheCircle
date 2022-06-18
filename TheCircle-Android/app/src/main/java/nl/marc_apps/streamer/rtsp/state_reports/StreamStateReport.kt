package nl.marc_apps.streamer.rtsp.state_reports

import nl.marc_apps.streamer.rtsp.state_reports.StreamStateReportWriter.Companion.PACKET_LENGTH
import nl.marc_apps.streamer.rtsp.utils.RtspConstants
import nl.marc_apps.streamer.rtsp.utils.setLong
import java.util.concurrent.TimeUnit
import kotlin.math.pow

class StreamStateReport {
    private val buffer = ByteArray(RtspConstants.MTU)

    var ssrc = 0L

    var packetCount = 0L
    var octetCount = 0L

    data class StreamStateReportData(
        val ssrc: Long = 0L,
        val packetCount: Long = 0L,
        val octetCount: Long = 0L,
        val frameTimeStamp: Long = 0L
    ) {
        private val currentTimeNanoSeconds = System.nanoTime()

        val ntpTimestampHb = TimeUnit.SECONDS.convert(currentTimeNanoSeconds, TimeUnit.NANOSECONDS)

        private val integerValueRepresentation = 2.0.pow(Int.SIZE_BITS).toLong()

        val ntpTimestampLb = TimeUnit.SECONDS.convert(
            (currentTimeNanoSeconds - TimeUnit.NANOSECONDS.convert(ntpTimestampHb, TimeUnit.SECONDS)) * integerValueRepresentation,
            TimeUnit.NANOSECONDS
        )

        override fun toString(): String {
            return "ssrc=$ssrc; packetCount=$packetCount; octetCount=$octetCount; frameTimeStamp=$frameTimeStamp; ntpTimestamp=$ntpTimestampHb.$ntpTimestampLb"
        }
    }

    fun createStreamStateReportData(frameTimeStamp: Long): StreamStateReportData {
        return StreamStateReportData(ssrc, packetCount, octetCount, frameTimeStamp)
    }

    fun writeToBuffer(streamStateReportData: StreamStateReportData): ByteArray {
        /*	   Version(2)  Padding(0)					*/
        /*			 ^		  ^			PT = 0	    	*/
        /*			 |		  |				^			*/
        /*			 | --------			 	|			*/
        /*			 | |---------------------			*/
        /*			 | ||								*/
        buffer[0] = 0x80.toByte()

        /* Packet Type PT */
        buffer[1] = 200.toByte()

        /* Byte 2,3     ->  Length		                 */
        buffer.setLong(PACKET_LENGTH / 4 - 1L, 2 until 4)

        /* Byte 4..7    ->  SSRC                         */
        buffer.setLong(streamStateReportData.ssrc, 4 until 8)

        /* Byte 8..11   ->  NTP timestamp hb			 */
        buffer.setLong(streamStateReportData.ntpTimestampHb, 8 until 12)

        /* Byte 12..15  ->  NTP timestamp lb			 */
        buffer.setLong(streamStateReportData.ntpTimestampLb, 12 until 16)

        /* Byte 16..19  ->  RTP timestamp		         */
        buffer.setLong(streamStateReportData.frameTimeStamp, 16 until 20)

        /* Byte 20..23  ->  packet count	    	 	 */
        buffer.setLong(streamStateReportData.packetCount, 20 until 24)

        /* Byte 24..27  ->  octet count			         */
        buffer.setLong(streamStateReportData.octetCount, 24 until 28)

        return buffer
    }
}
