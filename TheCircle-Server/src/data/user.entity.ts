import { IsDateString, IsString, IsUUID } from "class-validator"
import { Column, CreateDateColumn, Entity, PrimaryGeneratedColumn } from "typeorm"

@Entity()
export class UserEntity {
    @PrimaryGeneratedColumn("uuid")
    @IsUUID()
    userId: string

    @Column()
    @IsString()
    name: string

    @CreateDateColumn({
        type: 'timestamp',
        precision: 3
    })
    @IsDateString()
    creationDate: string

    @Column()
    @IsString()
    publicKey: string

    @Column()
    @IsString()
    userSignature: string
}
