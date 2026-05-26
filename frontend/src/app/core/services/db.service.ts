import { Injectable } from '@angular/core';
import { ApiService } from './api.service';

@Injectable({ providedIn: 'root' })
export class DbService {
  constructor(private api: ApiService) {}

  listTables() {
    return this.api.get('/internal/db/tables');
  }

  getTable(table: string, limit = 200, offset = 0) {
    return this.api.get(`/internal/db/table/${encodeURIComponent(table)}?limit=${limit}&offset=${offset}`);
  }
}
