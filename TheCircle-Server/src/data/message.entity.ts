import { IsDateString, IsString, IsUUID } from "class-validator"
import { Column, CreateDateColumn, Entity, PrimaryGeneratedColumn } from "typeorm"

@Entity()
export class MessageEntity {
    @PrimaryGeneratedColumn("uuid")
    @IsUUID()
    messageId: string

    @Column("uuid")
    @IsUUID()
    chatId: string

    @Column("uuid")
    @IsUUID()
    senderId: string

    @Column()
    @IsString()
    message: string

    @CreateDateColumn({
        type: 'timestamp',
        precision: 3
    })
    @IsDateString()
    creationDate: string

    @Column()
    @IsString()
    senderSignature: string
}
