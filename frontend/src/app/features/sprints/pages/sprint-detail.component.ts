import { Component, OnInit, OnDestroy } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivatedRoute, Router } from '@angular/router';
import { SprintFeatureService } from '../services/sprint-feature.service';
import { Subject } from 'rxjs';
import { takeUntil } from 'rxjs/operators';

@Component({
  selector: 'app-sprint-detail',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './sprint-detail.component.html',
  styleUrls: ['./sprint-detail.component.css']
})
export class SprintDetailComponent implements OnInit, OnDestroy {
  sprintId: number | null = null;
  sprintDetail: any = null;
  loading = true;
  error: string | null = null;
  private destroy$ = new Subject<void>();

  constructor(
    private route: ActivatedRoute,
    private router: Router,
    private sprintService: SprintFeatureService
  ) {}

  ngOnInit() {
    this.route.params.pipe(takeUntil(this.destroy$)).subscribe(params => {
      this.sprintId = +params['id'];
      if (this.sprintId) {
        this.loadSprintDetail(this.sprintId);
      }
    });
  }

  private loadSprintDetail(id: number) {
    this.loading = true;
    this.sprintService.getSprintDetail(id)
      .pipe(takeUntil(this.destroy$))
      .subscribe({
        next: (data: any) => {
          this.sprintDetail = data;
          this.loading = false;
        },
        error: (err: any) => {
          console.error('Error loading sprint detail:', err);
          this.error = 'Failed to load sprint details';
          this.loading = false;
        }
      });
  }

  ngOnDestroy() {
    this.destroy$.next();
    this.destroy$.complete();
  }

  goBack() {
    this.router.navigate(['/dashboard/sprints']);
  }
}
