import { ApiProperty } from "@nestjs/swagger"
import { IsString, IsUUID } from "class-validator"

export class StreamAddDto {
    @ApiProperty()
    @IsUUID()
    userId: string

    @ApiProperty()
    @IsString()
    userSignature: string
}
