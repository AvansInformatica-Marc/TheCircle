import { Component, OnInit } from '@angular/core';
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

    public streams: Stream[] = []

    names = new Map<string, string>()

    constructor(
        private streamsService: StreamsService,
        private usersService: UsersService
    ) { }

    ngOnInit(): void {
        this.streamsService.getStreams().subscribe({
            next: streams => {
                this.isLoading = false
                this.streams = streams
                for (const stream of streams) {
                    this.names.set(stream.userId, stream.userId)
                    this.usersService.getUserById(stream.userId).subscribe({
                        next: user => {
                            this.names.set(stream.userId, user.name)
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
