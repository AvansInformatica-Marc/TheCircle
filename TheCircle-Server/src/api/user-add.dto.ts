import { ApiProperty } from "@nestjs/swagger"
import { IsString } from "class-validator"

export class UserAddDto {
    @ApiProperty()
    @IsString()
    name: string

    @ApiProperty()
    @IsString()
    publicKey: string

    @ApiProperty()
    @IsString()
    userSignature: string
}
