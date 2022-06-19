import { Component, Input, OnInit } from "@angular/core";
import { timer } from "rxjs";
import { __classPrivateFieldIn } from "tslib";
import { UsersService } from "../users/users.service";
import { ChatService } from "./chat.service";
import { Message } from "./message.entity";

@Component({
    selector: 'chat',
    templateUrl: './chat.component.html',
    styleUrls: ['./chat.component.scss']
})
export class ChatComponent implements OnInit {
    @Input()
    userId!: string

    messages: Message[] = []

    names = new Map<string, string>()

    message = ""

    constructor(
        private readonly usersService: UsersService,
        private readonly chatService: ChatService
    ) {}

    ngOnInit(): void {
        timer(0, 3 * 1000).subscribe({
            next: _ => {
                this.updateMessages()
            }
        })
    }

    updateMessages() {
        this.chatService.getChat(this.userId).subscribe({
            next: response => {
                this.messages = response
                for (const message of response) {
                    this.names.set(message.senderId, message.senderId)
                    this.usersService.getUserById(message.senderId).subscribe({
                        next: user => {
                            this.names.set(message.senderId, user.name)
                        }
                    })
                }
            }
        })
    }

    sendMessage() {
        const tmpMessage = this.message
        this.message = ""
        this.chatService.sendMessage({
            chatId: this.userId,
            senderId: "a1952b62-4f3f-452b-8322-538b5ad0c2af",
            message: tmpMessage
        }).subscribe({
            next: _ => this.updateMessages()
        })
    }
}
