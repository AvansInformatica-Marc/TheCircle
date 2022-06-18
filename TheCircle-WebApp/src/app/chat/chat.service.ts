import { HttpClient } from '@angular/common/http'
import { Injectable } from "@angular/core"
import { plainToInstance } from 'class-transformer'
import { catchError, map, Observable } from 'rxjs'
import { environment } from 'src/environments/environment'
import { Message } from './message.entity'

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

    sendMessage(message: {chatId: string, senderId: string, message: string}): Observable<Message> {
        const msg = { ...message, senderSignature: "" }
        return this.http.post<Message>(`http://${environment.apiHost}:${environment.apiPort}/v1/chat/${msg.chatId}/messages`, msg)
            .pipe(
                map(message => plainToInstance(Message, message))
            )
    }
}
