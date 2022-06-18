package nl.marc_apps.streamer.rtsp

enum class RtspLoggingLevel {
    /** No logs. */
    NONE,

    /**
     * Logs request and response lines, with a sequence number and response time.
     *
     * Example:
     * ```
     * --> OPTIONS rtsp://example.com/my-live-stream RTSP/1.0 (#1)
     *
     * <-- RTSP/1.0 200 OK (22ms, #1)
     * ```
     */
    BASIC,

    /**
     * Logs request and response lines and their respective headers.
     *
     * Example:
     * ```
     * --> SETUP rtsp://example.com/my-live-stream/streamid=0 RTSP/1.0
     * CSeq: 3
     * Transport: RTP/AVP;unicast;client_port=8000-8001
     * --> END SETUP
     *
     * <-- RTSP/1.0 200 OK (22ms, #1)
     * CSeq: 3
     * Transport: RTP/AVP;unicast;client_port=8000-8001;server_port=9000-9001;ssrc=1234ABCD
     * Session: 12345678
     * <-- END RTSP
     * ```
     */
    HEADERS,

    /**
     * Logs request and response lines and their respective headers and plain text bodies (if present).
     *
     * Example:
     * ```
     * --> ANNOUNCE rtsp://example.com/media.mp4 RTSP/1.0
     * CSeq: 7
     * Date: 23 Jan 1997 15:35:06 GMT
     * Session: 12345678
     * Content-Type: application/sdp
     * Content-Length: 332
     *
     * v=0
     * o=- 2890844526 2890845468 IN IP4 126.16.64.4
     * ...
     * m=audio 3456 RTP/AVP 0
     * m=video 2232 RTP/AVP 31
     * --> END ANNOUNCE
     * ```
     */
    PLAIN_TEXT_BODY,

    /**
     * Logs request and response lines and their respective headers and bodies (if present).
     * Binary requests/responses will be represented as base64.
     */
    FULL
}
