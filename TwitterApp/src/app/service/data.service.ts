import { Injectable } from '@angular/core';
import {HttpClient} from '@angular/common/http';
import {Observable} from 'rxjs';
import {BubbleChart} from '../interface/bubblechart';

@Injectable({
  providedIn: 'root'
})
export class DataService {
  serverURL = 'http://localhost:8001';

  constructor(private http: HttpClient) { }

  getBubbleChart() {
    return this.http.get(`${this.serverURL}/bubblechart`);
  }

  getBots() {
    return this.http.get(`${this.serverURL}/bots`);
  }

  getInfluencers() {
    return this.http.get(`${this.serverURL}/influencers`);
  }

  getTop10Hash() {
    return this.http.get(`${this.serverURL}/top10hashtags`);
  }

  getTopHashTime() {
    return this.http.get(`${this.serverURL}/`);
  }
}
