import { Test, TestingModule } from '@nestjs/testing'
import { StreamController } from './stream.controller'

describe('StreamController', () => {
    let controller: StreamController

    beforeEach(async () => {
        const app: TestingModule = await Test.createTestingModule({
            controllers: [StreamController],
            providers: [
                {
                    provide: "StreamService",
                    useFactory: () => ({
                        getAllStreams: () => Promise.resolve([]),
                        findByStreamId: (_: string) => Promise.resolve(null)
                    })
                }
            ]
        }).compile()

        controller = app.get<StreamController>(StreamController)
    })

    describe('get streams', () => {
        it('should initially return empty list', () => {
            expect(controller.getAllStreams()).toBe([])
        })
    })
})
