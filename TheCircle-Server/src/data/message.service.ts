import { Injectable } from "@nestjs/common";
import { InjectRepository } from "@nestjs/typeorm";
import { Repository } from "typeorm";
import { MessageEntity } from "./message.entity";

@Injectable()
export class MessageService {
    constructor(
        @InjectRepository(MessageEntity)
        private readonly messageRepository: Repository<MessageEntity>
    ) {}

    getAllMessages(): Promise<MessageEntity[]> {
        return this.messageRepository.find()
    }

    findByMessageId(messageId: string): Promise<MessageEntity | null> {
        return this.messageRepository.findOneBy({ messageId })
    }

    getMessagesForChat(chatId: string): Promise<MessageEntity[]> {
        return this.messageRepository.findBy({ chatId })
    }

    create(message: MessageEntity): Promise<MessageEntity> {
        return this.messageRepository.save(message)
    }
}
