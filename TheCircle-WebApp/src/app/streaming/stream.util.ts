import { Stream } from "./stream.entity"

export type HlsJS = import("hls.js/dist/hls.js").default

type Hls = typeof import("hls.js/dist/hls.js").default

declare const Hls: Hls

export const loadStream = async (stream: Stream, videoPlayer: HTMLMediaElement, hlsRegistered: (hls: HlsJS) => void) => {
    if (Hls.isSupported()) {
        const hls = new Hls({})
        hlsRegistered(hls)

        hls.on(Hls.Events.ERROR, (_, data) => {
            if (data.fatal) {
                hls.destroy()

                setTimeout(() => loadStream(stream, videoPlayer, hlsRegistered), 2000)
            }
        })

        hls.loadSource(stream.hlsPlaylistUrl)
        hls.attachMedia(videoPlayer)
    } else if (videoPlayer.canPlayType('application/vnd.apple.mpegurl')) {
        try {
            await fetch(stream.hlsPlaylistUrl.replace("index.m3u8", "stream.m3u8"))
        } catch (ignored) {}
        videoPlayer.src = stream.hlsPlaylistUrl
    }
}
