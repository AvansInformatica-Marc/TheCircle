import { ApiProperty } from "@nestjs/swagger"
import { IsDateString, IsString, IsUrl, IsUUID } from "class-validator"

export class StreamReadDto {
    @ApiProperty()
    @IsUUID()
    streamId: string

    @ApiProperty()
    @IsUUID()
    userId: string

    @ApiProperty()
    @IsUrl()
    hlsPlaylistUrl: string

    @ApiProperty()
    @IsUrl()
    hlsEmbedUrl: string

    @ApiProperty()
    @IsUrl({
        protocols: ["rtsp"],
        require_protocol: true
    })
    rtspUrl: string

    @ApiProperty()
    @IsDateString()
    creationDate: string

    @ApiProperty()
    @IsString()
    userSignature: string

    @ApiProperty()
    @IsString()
    serverSignature: string
}
