import { HttpClient } from '@angular/common/http'
import { Injectable } from "@angular/core"
import { plainToInstance } from 'class-transformer'
import { catchError, map, Observable } from 'rxjs'
import { environment } from 'src/environments/environment'
import { Stream } from './stream.entity'

@Injectable({
    providedIn: 'root'
})
export class StreamsService {
    constructor(private readonly http: HttpClient) { }

    getStreams(): Observable<Stream[]> {
        return this.http.get<Stream[]>(`http://${environment.apiHost}:${environment.apiPort}/v1/streams`)
            .pipe(
                catchError(_ => []),
                map(streams => plainToInstance(Stream, streams))
            )
    }

    getStreamById(streamId: string): Observable<Stream> {
        return this.http.get<Stream>(`http://${environment.apiHost}:${environment.apiPort}/v1/streams/${streamId}`)
            .pipe(
                map(stream => plainToInstance(Stream, stream))
            )
    }
}
