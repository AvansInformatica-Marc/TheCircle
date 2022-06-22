import { Module } from '@nestjs/common'
import { TypeOrmModule } from '@nestjs/typeorm'
import { ChatController } from './api/chat.controller'
import { StreamController } from './api/stream.controller'
import { UserController } from './api/user.controller'
import { MessageEntity } from './data/message.entity'
import { MessageService } from './data/message.service'
import { StreamEntity } from './data/stream.entity'
import { StreamService } from './data/stream.service'
import { UserEntity } from './data/user.entity'
import { UserService } from './data/user.service'

@Module({
    imports: [
        TypeOrmModule.forFeature([StreamEntity, UserEntity, MessageEntity])
    ],
    controllers: [StreamController, UserController, ChatController],
    providers: [StreamService, UserService, MessageService]
})
export class TheCircleModule {}
