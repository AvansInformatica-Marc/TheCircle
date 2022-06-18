import { Injectable } from "@nestjs/common";
import { InjectRepository } from "@nestjs/typeorm";
import { Repository } from "typeorm";
import { StreamEntity } from "./stream.entity";

@Injectable()
export class StreamService {
    constructor(
        @InjectRepository(StreamEntity)
        private readonly streamRepository: Repository<StreamEntity>
    ) {}

    getAllStreams(): Promise<StreamEntity[]> {
        return this.streamRepository.find()
    }

    findByStreamId(streamId: string): Promise<StreamEntity | null> {
        return this.streamRepository.findOneBy({ streamId })
    }

    findByUserId(userId: string): Promise<StreamEntity[]> {
        return this.streamRepository.findBy({ userId })
    }

    create(stream: StreamEntity): Promise<StreamEntity> {
        return this.streamRepository.save(stream)
    }

    async delete(streamId: string): Promise<void> {
        await this.streamRepository.delete({ streamId })
    }
}
