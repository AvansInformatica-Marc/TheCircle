import { ApiProperty } from "@nestjs/swagger"
import { IsDateString, IsString, IsUUID } from "class-validator"

export class UserReadDto {
    @ApiProperty()
    @IsUUID()
    userId: string

    @ApiProperty()
    @IsString()
    name: string

    @ApiProperty()
    @IsDateString()
    creationDate: string

    @ApiProperty()
    @IsString()
    publicKey: string

    @ApiProperty()
    @IsString()
    userSignature: string
}
