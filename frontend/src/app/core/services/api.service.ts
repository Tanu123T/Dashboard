import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { environment } from 'src/environments/environment';

@Injectable({
  providedIn: 'root'
})
export class ApiService {

  private baseUrl = environment.apiUrl;

  constructor(private http: HttpClient) {}

  // GET
  get(url: string) {
    return this.http.get(`${this.baseUrl}${url}`);
  }

  // POST
  post(url: string, body: any) {
    return this.http.post(`${this.baseUrl}${url}`, body);
  }

  // PUT
  put(url: string, body: any) {
    return this.http.put(`${this.baseUrl}${url}`, body);
  }

  // DELETE
  delete(url: string) {
    return this.http.delete(`${this.baseUrl}${url}`);
  }
}