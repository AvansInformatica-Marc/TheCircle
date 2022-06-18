import { BadRequestException, Body, Controller, Get, NotFoundException, Param, ParseUUIDPipe, Post, ValidationPipe } from "@nestjs/common";
import { ApiBadRequestResponse, ApiCreatedResponse, ApiNotFoundResponse, ApiOkResponse, ApiTags } from "@nestjs/swagger";
import { validationOptions } from "src/app.constants";
import { MessageEntity } from "src/data/message.entity";
import { MessageService } from "src/data/message.service";
import { MessageAddDto } from "./message-add.dto";
import { MessageReadDto } from "./message-read.dto";

@ApiTags("chat")
@Controller({
    path: "chat",
    version: "1"
})
export class ChatController {
    constructor (
        private readonly messageService: MessageService
    ) {}

    @Get(":chatId/messages")
    @ApiOkResponse({ type: [MessageReadDto] })
    @ApiBadRequestResponse()
    async getMessagesForChat(
        @Param("chatId", new ParseUUIDPipe()) chatId: string
    ): Promise<MessageReadDto[]> {
        const messages = await this.messageService.getMessagesForChat(chatId)
        return messages
    }

    @Get(":chatId/messages/:messageId")
    @ApiNotFoundResponse()
    @ApiOkResponse({ type: MessageReadDto })
    @ApiBadRequestResponse()
    async getMessageById(
        @Param("chatId", new ParseUUIDPipe()) chatId: string,
        @Param("messageId", new ParseUUIDPipe()) messageId: string
    ): Promise<MessageReadDto> {
        const message = await this.messageService.findByMessageId(messageId)

        if (message == null || message.chatId != chatId) {
            throw new NotFoundException()
        }

        return message
    }

    @Post(":chatId/messages")
    @ApiCreatedResponse({ type: MessageReadDto })
    @ApiBadRequestResponse()
    async createMessage(
        @Param("chatId", new ParseUUIDPipe()) chatId: string,
        @Body(new ValidationPipe(validationOptions)) messageAddDto: MessageAddDto
    ): Promise<MessageReadDto> {
        if (chatId != messageAddDto.chatId) {
            throw new BadRequestException()
        }

        const messageEntity = new MessageEntity()
        messageEntity.chatId = messageAddDto.chatId
        messageEntity.senderId = messageAddDto.senderId
        messageEntity.message = messageAddDto.message
        messageEntity.senderSignature = messageAddDto.senderSignature
        return await this.messageService.create(messageEntity)
    }
}
