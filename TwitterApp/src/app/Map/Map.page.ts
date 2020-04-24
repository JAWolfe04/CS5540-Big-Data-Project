import {Component, OnInit} from '@angular/core';
import * as d3 from 'd3';
import {feature} from 'topojson';

@Component({
  selector: 'app-Map',
  templateUrl: 'Map.page.html',
  styleUrls: ['Map.page.scss']
})
export class MapPage implements OnInit {
  loadingWorld: boolean;
  errorWorld: string;
  worldWidth: number;
  worldHeight: number;
  loadingUS: boolean;
  errorUS: string;
  USWidth: number;
  USHeight: number;

  constructor() {}

  ngOnInit(): void {
    this.createWorldMap();
  }

  createWorldMap() {
    const svg = d3.select('#Worldchart')
        .append('svg')
        .attr('width', this.worldWidth)
        .attr('height', this.worldHeight);

    d3.json('https://unpkg.com/world-atlas@1.1.4/world/50m.json').then(data => {
      const countries = feature(data, data.objects.countries);
      // console.log(data);
    });
  }

  createUSMap() {
    const svg = d3.select('#Worldchart')
        .append('svg')
        .attr('viewBox', `0,0,${this.USWidth},${this.USHeight}`)
        .attr('width', this.USWidth)
        .attr('height', this.USHeight);
  }
}
