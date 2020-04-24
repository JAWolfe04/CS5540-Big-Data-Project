import { Injectable } from '@angular/core';
import {HttpClient} from '@angular/common/http';

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

  getgeoData() {
    return this.http.get(`${this.serverURL}/geodata`);
  }

  getNews() {
    return this.http.get(`${this.serverURL}/newsdata`);
  }

  getTweetFreq() {
    return this.http.get(`${this.serverURL}/tweetfreqdata`);
  }

  getCountryData() {
    return this.http.get(`${this.serverURL}/countrydata`);
  }
}
