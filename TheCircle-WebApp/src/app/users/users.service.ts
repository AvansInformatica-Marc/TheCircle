import { HttpClient } from '@angular/common/http'
import { Injectable } from "@angular/core"
import { plainToInstance } from 'class-transformer'
import { map, tap, of, Observable } from 'rxjs'
import { environment } from 'src/environments/environment'
import { User } from './user.entity'
import * as forge from "node-forge"

@Injectable({
    providedIn: 'root'
})
export class UsersService {
    private usersCache = new Map<string, User>()

    constructor(private readonly http: HttpClient) { }

    getUserById(userId: string): Observable<User> {
        if (this.usersCache.has(userId)) {
            return of(this.usersCache.get(userId)!)
        }

        return this.http.get<User>(`http://${environment.apiHost}:${environment.apiPort}/v1/users/${userId}`)
            .pipe(
                map(user => plainToInstance(User, user)),
                tap(user => this.usersCache.set(userId, user))
            )
    }

    async register(userName: string): Promise<Observable<User>> {
        const keyPair = await this.createKeyPair()
        const publicKey = forge.pki.publicKeyToPem(keyPair.publicKey)
        const message = `name:${userName};publicKey:${publicKey.replace(/\r\n/g, "")};`
        const md = forge.md.sha512.create();
        md.update(message, 'utf8');
        const signature = forge.util.encode64(keyPair.privateKey.sign(md));
        const body = {
            name: userName,
            publicKey: publicKey,
            userSignature: signature
        }
        return this.http.post<User>(`http://${environment.apiHost}:${environment.apiPort}/v1/users/`, body)
            .pipe(
                map(user => plainToInstance(User, user)),
                tap(user => {
                    localStorage["userId"] = user.userId;
                    localStorage["userName"] = user.name;
                    localStorage["privateKey"] = forge.pki.privateKeyToPem(keyPair.privateKey);
                }),
                tap(user => this.usersCache.set(user.userId, user))
            )
    }

    createKeyPair(): Promise<forge.pki.rsa.KeyPair> {
        return new Promise((resolve, reject) => {
            forge.pki.rsa.generateKeyPair({bits: 4096, workers: -1}, (error, keyPair) => {
                if (keyPair) {
                    resolve(keyPair)
                } else {
                    reject(error)
                }
            })
        })
    }
}
