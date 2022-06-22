import { ApiProperty, ApiPropertyOptional } from "@nestjs/swagger"
import { IsDateString, IsOptional, IsString, IsUUID } from "class-validator"

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

    @ApiPropertyOptional()
    @IsOptional()
    @IsString()
    certificate?: string

    @ApiProperty()
    @IsString()
    userSignature: string

    @ApiProperty()
    @IsString()
    serverSignature: string
}
