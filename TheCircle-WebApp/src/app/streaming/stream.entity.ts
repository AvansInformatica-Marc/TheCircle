import { IsDateString, IsString, IsUrl, IsUUID } from "class-validator"

export class Stream {
    @IsUUID()
    streamId!: string

    @IsUUID()
    userId!: string

    @IsUrl()
    hlsPlaylistUrl!: string

    @IsUrl()
    hlsEmbedUrl!: string

    @IsUrl({
        protocols: ["rtsp"],
        require_protocol: true
    })
    rtspUrl!: string

    @IsDateString()
    creationDate!: string

    @IsString()
    userSignature!: string
}
