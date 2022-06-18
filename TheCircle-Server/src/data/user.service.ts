import { Injectable } from "@nestjs/common"
import { InjectRepository } from "@nestjs/typeorm"
import { Repository } from "typeorm"
import { UserEntity } from "./user.entity"

@Injectable()
export class UserService {
    constructor(
        @InjectRepository(UserEntity)
        private readonly userRepository: Repository<UserEntity>
    ) {}

    getAllUsers(): Promise<UserEntity[]> {
        return this.userRepository.find()
    }

    findByUserId(userId: string): Promise<UserEntity | null> {
        return this.userRepository.findOneBy({ userId })
    }

    create(user: UserEntity): Promise<UserEntity> {
        return this.userRepository.save(user)
    }

    async delete(userId: string): Promise<void> {
        await this.userRepository.delete({ userId })
    }
}
