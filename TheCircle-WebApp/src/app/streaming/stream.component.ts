import { AfterViewInit, Component, ElementRef, OnDestroy, ViewChild } from "@angular/core"
import { ActivatedRoute } from "@angular/router"
import { User } from "../users/user.entity"
import { UsersService } from "../users/users.service"
import { Stream } from "./stream.entity"
import { HlsJS, loadStream } from "./stream.util"
import { StreamsService } from "./streams.service"

@Component({
    selector: 'stream',
    templateUrl: './stream.component.html',
    styleUrls: ['./stream.component.scss']
})
export class StreamComponent implements AfterViewInit, OnDestroy {
    @ViewChild("videoPlayer")
    player!: ElementRef<HTMLMediaElement>

    streamId: string | null = null

    stream: Stream | undefined

    user: User | undefined

    hls: HlsJS | null = null

    constructor(
        private readonly route: ActivatedRoute,
        private readonly streamsService: StreamsService,
        private readonly usersService: UsersService
    ) { }

    ngAfterViewInit(): void {
        const routeParams = this.route.snapshot.paramMap
        this.streamId = routeParams.get('streamId')

        if (this.streamId) {
            this.streamsService.getStreamById(this.streamId).subscribe({
                next: response => {
                    this.stream = response

                    loadStream(response, this.player.nativeElement, (hls) => {
                        this.hls = hls
                    })

                    this.usersService.getUserById(response.userId).subscribe({
                        next: response => {
                            this.user = response
                        }
                    })
                }
            })
        }
    }

    ngOnDestroy(): void {
        this.hls?.destroy()
    }
}
