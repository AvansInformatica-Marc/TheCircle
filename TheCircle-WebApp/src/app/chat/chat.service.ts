import { HttpClient } from '@angular/common/http'
import { Injectable } from "@angular/core"
import { plainToInstance } from 'class-transformer'
import { catchError, map, Observable } from 'rxjs'
import { environment } from 'src/environments/environment'
import { Message } from './message.entity'
import * as forge from "node-forge"

@Injectable({
    providedIn: 'root'
})
export class ChatService {
    constructor(private readonly http: HttpClient) { }

    getChat(chatId: string): Observable<Message[]> {
        return this.http.get<Message[]>(`http://${environment.apiHost}:${environment.apiPort}/v1/chat/${chatId}/messages`)
            .pipe(
                catchError(_ => []),
                map(message => plainToInstance(Message, message))
            )
    }

    getMessageById(chatId: string, messageId: string): Observable<Message> {
        return this.http.get<Message>(`http://${environment.apiHost}:${environment.apiPort}/v1/chat/${chatId}/messages/${messageId}`)
            .pipe(
                map(message => plainToInstance(Message, message))
            )
    }

    async sendMessage(chatId: string, message: string): Promise<Observable<Message>> {
        const senderId = localStorage["userId"] ?? ""
        const privateKey = forge.pki.privateKeyFromPem(localStorage["privateKey"])
        const signatureMessage = `chatId:${chatId};senderId:${senderId};message:${message};`
        const md = forge.md.sha512.create();
        md.update(signatureMessage, 'utf8');
        const senderSignature = forge.util.encode64(privateKey.sign(md));
        const msg = { chatId, message, senderId, senderSignature }
        return this.http.post<Message>(`http://${environment.apiHost}:${environment.apiPort}/v1/chat/${msg.chatId}/messages`, msg)
            .pipe(
                map(message => plainToInstance(Message, message))
            )
    }
}
