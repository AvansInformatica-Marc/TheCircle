import { NgModule } from '@angular/core'
import { RouterModule, Routes } from '@angular/router'
import { StreamListComponent } from './streaming/stream-list.component'
import { StreamComponent } from './streaming/stream.component'

const routes: Routes = [
    { path: '', component: StreamListComponent },
    { path: 'streams/:streamId', component: StreamComponent }
]

@NgModule({
    imports: [RouterModule.forRoot(routes)],
    exports: [RouterModule]
})
export class AppRoutingModule { }
