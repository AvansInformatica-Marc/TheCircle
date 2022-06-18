import { Module } from '@nestjs/common'
import { TypeOrmModule } from '@nestjs/typeorm'
import { StreamController } from './api/stream.controller'
import { UserController } from './api/user.controller'
import { StreamEntity } from './data/stream.entity'
import { StreamService } from './data/stream.service'
import { UserEntity } from './data/user.entity'
import { UserService } from './data/user.service'

@Module({
    imports: [
        TypeOrmModule.forFeature([StreamEntity, UserEntity])
    ],
    controllers: [StreamController, UserController],
    providers: [StreamService, UserService]
})
export class TheCircleModule {}
