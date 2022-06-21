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

    isRegistered = !!localStorage["userId"]

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

    async sendMessage() {
        const tmpMessage = this.message
        this.message = "";
        (await this.chatService.sendMessage(this.userId, tmpMessage)).subscribe({
            next: _ => this.updateMessages()
        })
    }
}
