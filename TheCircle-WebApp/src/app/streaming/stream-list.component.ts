import { Component, OnInit } from '@angular/core';
import { mergeMap, map, concatMap } from 'rxjs';
import { User } from '../users/user.entity';
import { UsersService } from '../users/users.service';
import { Stream } from './stream.entity';
import { StreamsService } from './streams.service';

@Component({
  selector: 'stream-list',
  templateUrl: './stream-list.component.html',
  styleUrls: ['./stream-list.component.scss']
})
export class StreamListComponent implements OnInit {
    public isLoading: boolean = true

    public streams: [Stream, User][] = []

    constructor(
        private streamsService: StreamsService,
        private usersService: UsersService
    ) { }

    ngOnInit(): void {
        this.streamsService.getStreams().subscribe({
            next: streams => {
                this.isLoading = false
                this.streams = []
                for (const stream of streams) {
                    this.usersService.getUserById(stream.userId).subscribe({
                        next: user => {
                            this.streams.push([stream, user])
                        }
                    })
                }
            },
            error: _ => {
                this.isLoading = false
                this.streams = []
            }
        })
    }
}
