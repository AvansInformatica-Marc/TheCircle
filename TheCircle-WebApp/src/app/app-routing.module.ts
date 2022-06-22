import { NgModule } from '@angular/core'
import { RouterModule, Routes } from '@angular/router'
import { StreamListComponent } from './streaming/stream-list.component'
import { StreamComponent } from './streaming/stream.component'
import { RegisterComponent } from './users/register.component'

const routes: Routes = [
    { path: '', component: StreamListComponent },
    { path: 'register', component: RegisterComponent },
    { path: 'streams/:streamId', component: StreamComponent }
]

@NgModule({
    imports: [RouterModule.forRoot(routes)],
    exports: [RouterModule]
})
export class AppRoutingModule { }
