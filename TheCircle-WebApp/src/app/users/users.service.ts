import { HttpClient } from '@angular/common/http'
import { Injectable } from "@angular/core"
import { plainToInstance } from 'class-transformer'
import { map, Observable } from 'rxjs'
import { environment } from 'src/environments/environment'
import { User } from './user.entity'

@Injectable({
    providedIn: 'root'
})
export class UsersService {
    constructor(private readonly http: HttpClient) { }

    getUserById(userId: string): Observable<User> {
        return this.http.get<User>(`http://${environment.apiHost}:${environment.apiPort}/v1/users/${userId}`)
            .pipe(
                map(stream => plainToInstance(User, stream))
            )
    }
}
