import { ApiProperty } from "@nestjs/swagger"
import { IsDateString, IsString, IsUUID } from "class-validator"

export class MessageReadDto {
    @ApiProperty()
    @IsUUID()
    messageId: string

    @ApiProperty()
    @IsUUID()
    chatId: string

    @ApiProperty()
    @IsUUID()
    senderId: string

    @ApiProperty()
    @IsString()
    message: string

    @ApiProperty()
    @IsDateString()
    creationDate: string

    @ApiProperty()
    @IsString()
    senderSignature: string

    @ApiProperty()
    @IsString()
    serverSignature: string
}
