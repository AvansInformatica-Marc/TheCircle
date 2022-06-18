import { ApiProperty } from "@nestjs/swagger"
import { IsString, IsUUID } from "class-validator"

export class MessageAddDto {
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
    @IsString()
    senderSignature: string
}
