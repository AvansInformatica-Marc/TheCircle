import { Body, Controller, Delete, Get, HttpCode, NotFoundException, Param, ParseUUIDPipe, Post, ValidationPipe } from "@nestjs/common";
import { ApiBadRequestResponse, ApiCreatedResponse, ApiNoContentResponse, ApiNotFoundResponse, ApiOkResponse, ApiTags } from "@nestjs/swagger";
import { validationOptions } from "src/app.constants";
import { StreamEntity } from "src/data/stream.entity";
import { StreamService } from "src/data/stream.service";
import { StreamAddDto } from "./stream-add.dto";
import { StreamReadDto } from "./stream-read.dto";
import * as crypto from "node:crypto"
import { promises as fs } from "node:fs"

@ApiTags("streams")
@Controller({
    path: "streams",
    version: "1"
})
export class StreamController {
    constructor (
        private readonly streamService: StreamService
    ) {}

    @Get()
    @ApiOkResponse({ type: [StreamReadDto] })
    async getAllStreams(): Promise<StreamReadDto[]> {
        const streams = await this.streamService.getAllStreams()
        return await Promise.all(streams.map(stream => this.mapStreamEntityToReadDto(stream)))
    }

    @Get(":streamId")
    @ApiNotFoundResponse()
    @ApiOkResponse({ type: StreamReadDto })
    @ApiBadRequestResponse()
    async getStreamById(
        @Param("streamId", new ParseUUIDPipe()) streamId: string
    ): Promise<StreamReadDto> {
        const stream = await this.streamService.findByStreamId(streamId)

        if (stream == null) {
            throw new NotFoundException()
        }

        return await this.mapStreamEntityToReadDto(stream)
    }

    @Post()
    @ApiCreatedResponse({ type: StreamReadDto })
    @ApiBadRequestResponse()
    async createStream(
        @Body(new ValidationPipe(validationOptions)) streamAddDto: StreamAddDto
    ): Promise<StreamReadDto> {
        const streamEntity = new StreamEntity()
        streamEntity.userId = streamAddDto.userId
        streamEntity.userSignature = streamAddDto.userSignature
        return await this.mapStreamEntityToReadDto(await this.streamService.create(streamEntity))
    }

    @Delete(":streamId")
    @HttpCode(204)
    @ApiNoContentResponse()
    async deleteStream(
        @Param("streamId", new ParseUUIDPipe()) streamId: string
    ): Promise<void> {
        // TODO: Request signature
        await this.streamService.delete(streamId)
    }

    private async mapStreamEntityToReadDto(entity: StreamEntity): Promise<StreamReadDto> {
        const stream = new StreamReadDto()
        stream.userId = entity.userId
        stream.streamId = entity.streamId
        stream.creationDate = new Date(entity.creationDate).toISOString()
        stream.userSignature = entity.userSignature
        const streamHost = process.env["STREAM_HOST"] ?? "127.0.0.1"
        const rtspPort = process.env["STREAM_RTSP_PORT"] ?? "554"
        const hlsPort = process.env["STREAM_HLS_PORT"] ?? "8888"
        stream.rtspUrl = `rtsp://${streamHost}:${rtspPort}/${entity.streamId}`
        stream.hlsEmbedUrl = `http://${streamHost}:${hlsPort}/${entity.streamId}`
        stream.hlsPlaylistUrl= `http://${streamHost}:${hlsPort}/${entity.streamId}/index.m3u8`

        const message = `userId:${stream.userId};streamId:${stream.streamId};creationDate:${stream.creationDate};` +
            `rtspUrl:${stream.rtspUrl};hlsEmbedUrl:${stream.hlsEmbedUrl};hlsPlaylistUrl:${stream.hlsPlaylistUrl};` +
            `userSignature:${stream.userSignature};`
        const signer = crypto.createSign('RSA-SHA512')
        signer.write(message)
        signer.end()
        stream.serverSignature = signer.sign(await fs.readFile("server.key.pem"), 'base64')

        return stream
    }
}
