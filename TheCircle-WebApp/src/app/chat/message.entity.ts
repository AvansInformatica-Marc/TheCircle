import { IsDateString, IsString, IsUUID } from "class-validator"

export class Message {
    @IsUUID()
    messageId!: string

    @IsUUID()
    chatId!: string

    @IsUUID()
    senderId!: string

    @IsString()
    message!: string

    @IsDateString()
    creationDate!: string

    @IsString()
    senderSignature!: string
}
