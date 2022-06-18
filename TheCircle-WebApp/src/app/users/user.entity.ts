import { IsDateString, IsString, IsUrl, IsUUID } from "class-validator"

export class User {
    @IsUUID()
    userId!: string

    @IsString()
    name!: string

    @IsDateString()
    creationDate!: string

    @IsString()
    publicKey!: string

    @IsString()
    userSignature!: string
}
