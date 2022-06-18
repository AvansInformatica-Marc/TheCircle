import { IsDateString, IsString, IsUUID } from "class-validator"
import { Column, CreateDateColumn, Entity, PrimaryGeneratedColumn } from "typeorm"

@Entity()
export class StreamEntity {
    @PrimaryGeneratedColumn("uuid")
    @IsUUID()
    streamId: string

    @Column("uuid")
    @IsUUID()
    userId: string

    @CreateDateColumn({
        type: 'timestamp',
        precision: 3
    })
    @IsDateString()
    creationDate: string

    @Column()
    @IsString()
    userSignature: string
}
