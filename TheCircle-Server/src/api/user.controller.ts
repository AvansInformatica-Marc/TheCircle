import { Body, Controller, Get, NotFoundException, Param, ParseUUIDPipe, Post, ValidationPipe } from "@nestjs/common"
import { ApiBadRequestResponse, ApiCreatedResponse, ApiNotFoundResponse, ApiOkResponse, ApiTags } from "@nestjs/swagger"
import { issuerAttrs, validationOptions } from "src/app.constants"
import { UserEntity } from "src/data/user.entity"
import { UserService } from "src/data/user.service"
import { UserAddDto } from "./user-add.dto"
import { UserReadDto } from "./user-read.dto"
import { pki } from "node-forge"
import { promises as fs } from "node:fs"

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

        const createdUser = await this.userService.create(userEntity)

        const userKey = pki.publicKeyFromPem(userAddDto.publicKey)

        const cert = pki.createCertificate()
        cert.publicKey = userKey

        let serial = createdUser.userId.replace("-", "")
        if (serial.startsWith("1")) {
            serial = "0" + serial
        }
        cert.serialNumber = serial

        cert.validity.notBefore = new Date(createdUser.creationDate)
        cert.validity.notAfter = new Date(createdUser.creationDate)
        cert.validity.notAfter.setFullYear(cert.validity.notBefore.getFullYear() + 4)

        const subjectAttrs = [{
          name: 'commonName',
          value: createdUser.userId
        }, {
          name: 'countryName',
          value: 'NL'
        }, {
          name: 'organizationName',
          value: 'The Circle'
        }, {
          shortName: 'OU',
          value: 'The Circle Users'
        }]
        cert.setSubject(subjectAttrs)

        cert.setIssuer(issuerAttrs)

        cert.setExtensions([{
          name: 'basicConstraints',
          cA: true
        }, {
          name: 'keyUsage',
          keyCertSign: true,
          digitalSignature: true,
          nonRepudiation: true,
          keyEncipherment: true,
          dataEncipherment: true
        }, {
          name: 'extKeyUsage',
          serverAuth: false,
          clientAuth: true,
          codeSigning: false,
          emailProtection: false,
          timeStamping: true
        }, {
          name: 'nsCertType',
          client: true,
          server: false,
          email: false,
          objsign: false,
          sslCA: false,
          emailCA: false,
          objCA: false
        }, {
          name: 'subjectKeyIdentifier'
        }])

        const serverKey = await fs.readFile("server.key.pem")
        cert.sign(pki.privateKeyFromPem(serverKey.toString()))

        return await this.userService.addCertificate(createdUser, pki.certificateToPem(cert))
    }
}
