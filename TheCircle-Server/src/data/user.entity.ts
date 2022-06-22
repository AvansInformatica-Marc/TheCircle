import { IsDateString, IsOptional, IsString, IsUUID } from "class-validator"
import { Column, CreateDateColumn, Entity, IsNull, PrimaryGeneratedColumn } from "typeorm"

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

    @Column({ nullable: true })
    @IsOptional()
    @IsString()
    certificate?: string

    @Column()
    @IsString()
    userSignature: string
}
