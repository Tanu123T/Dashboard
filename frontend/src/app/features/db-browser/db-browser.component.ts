import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { DbService } from '../../core/services/db.service';

@Component({
  selector: 'app-db-browser',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './db-browser.component.html',
  styleUrls: ['./db-browser.component.css']
})
export class DbBrowserComponent implements OnInit {
  tables: any[] = [];
  selectedTable: string | null = null;
  rows: any[] = [];
  total = 0;
  limit = 200;
  offset = 0;

  loadingTables = false;
  loadingRows = false;

  constructor(private db: DbService) {}

  ngOnInit(): void {
    this.loadTables();
  }

  loadTables() {
    this.loadingTables = true;
    this.db.listTables().subscribe({
      next: (res: any) => {
        this.tables = res || [];
        this.loadingTables = false;
      },
      error: () => (this.loadingTables = false)
    });
  }

  selectTable(table: string) {
    this.selectedTable = table;
    this.offset = 0;
    this.loadRows();
  }

  loadRows() {
    if (!this.selectedTable) return;
    this.loadingRows = true;
    this.db.getTable(this.selectedTable, this.limit, this.offset).subscribe({
      next: (res: any) => {
        this.rows = res.rows || [];
        this.total = res.total || 0;
        this.loadingRows = false;
      },
      error: () => (this.loadingRows = false)
    });
  }

  nextPage() {
    this.offset += this.limit;
    this.loadRows();
  }

  prevPage() {
    this.offset = Math.max(0, this.offset - this.limit);
    this.loadRows();
  }
}
