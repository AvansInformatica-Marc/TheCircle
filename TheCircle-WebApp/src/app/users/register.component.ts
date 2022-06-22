import { Component } from "@angular/core";
import { Router } from "@angular/router";
import { __classPrivateFieldIn } from "tslib";
import { UsersService } from "../users/users.service";

@Component({
    selector: 'register',
    templateUrl: './register.component.html',
    styleUrls: ['./register.component.scss']
})
export class RegisterComponent {
    username = ""

    loading = false

    constructor(
        private readonly usersService: UsersService,
        private readonly router: Router
    ) {}

    async register(): Promise<void> {
        this.loading = true;
        (await this.usersService.register(this.username)).subscribe({
            next: _ => {
                this.loading = false
                this.router.navigate(["/"])
            },
            error: _ => {
                this.loading = false
            }
        })
    }
}
