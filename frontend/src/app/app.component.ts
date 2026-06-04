import { Component, Provider } from '@angular/core';
import { RouterOutlet } from '@angular/router';
import { LucideAngularModule, icons } from 'lucide-angular';

const lucideIcons = LucideAngularModule.pick(icons);

@Component({
  selector: 'app-root',
  standalone: true,
  imports: [RouterOutlet, LucideAngularModule],
  providers: [...((lucideIcons.providers ?? []) as Provider[])],
  templateUrl: './app.component.html',
  styleUrl: './app.component.css'
})
export class AppComponent {
  title = 'frontend';
}
