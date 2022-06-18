import { Body, Controller, Get, NotFoundException, Param, ParseUUIDPipe, Post, ValidationPipe } from "@nestjs/common"
import { ApiBadRequestResponse, ApiCreatedResponse, ApiNotFoundResponse, ApiOkResponse, ApiTags } from "@nestjs/swagger"
import { validationOptions } from "src/app.constants"
import { UserEntity } from "src/data/user.entity"
import { UserService } from "src/data/user.service"
import { UserAddDto } from "./user-add.dto"
import { UserReadDto } from "./user-read.dto"

@ApiTags("users")
@Controller({
    path: "users",
    version: "1"
})
export class UserController {
    constructor (
        private readonly userService: UserService
    ) {}

    @Get()
    @ApiOkResponse({ type: [UserReadDto] })
    async getAllUsers(): Promise<UserReadDto[]> {
        const users = await this.userService.getAllUsers()
        return users
    }

    @Get(":userId")
    @ApiNotFoundResponse()
    @ApiOkResponse({ type: UserReadDto })
    @ApiBadRequestResponse()
    async getUserById(
        @Param("userId", new ParseUUIDPipe()) userId: string
    ): Promise<UserReadDto> {
        const user = await this.userService.findByUserId(userId)

        if (user == null) {
            throw new NotFoundException()
        }

        return user
    }

    @Post()
    @ApiCreatedResponse({ type: UserReadDto })
    @ApiBadRequestResponse()
    async createUser(
        @Body(new ValidationPipe(validationOptions)) userAddDto: UserAddDto
    ): Promise<UserReadDto> {
        const userEntity = new UserEntity()
        userEntity.name = userAddDto.name
        userEntity.publicKey = userAddDto.publicKey
        userEntity.userSignature = userAddDto.userSignature
        return await this.userService.create(userEntity)
    }
}
