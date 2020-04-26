import {Component, OnInit} from '@angular/core';
import * as d3 from 'd3';
import * as topojson from 'topojson';
import * as legend from 'd3-svg-legend';
import {isUndefined} from 'util';
import {DataService} from '../service/data.service';

@Component({
  selector: 'app-Map',
  templateUrl: 'Map.page.html',
  styleUrls: ['Map.page.scss']
})
export class MapPage implements OnInit {
  loadingWorld: boolean;
  errorWorld: string;
  worldWidth = 975;
  worldHeight = 610;
  loadingUS: boolean;
  errorUS: string;
  USWidth: number;
  USHeight: number;

  constructor(private dataService: DataService) {}

  ngOnInit(): void {
    this.dataService.getCountryData().subscribe((countries: any) => {
          this.createWorldMap(countries.CountryCounts);
          this.loadingWorld = false;
        },
        error => {
          this.errorWorld = 'Unable to load World Map';
          this.loadingWorld = false;
        });
  }

  createWorldMap(dataset) {
      console.log(dataset);
    const colorScale = d3.scaleThreshold()
        .domain([1, 10, 50, 100, 250, 500, 750, 1000])
        .range(d3.schemeBlues[8]);

    const svg = d3.select('#Worldchart')
        .append('svg')
        .attr('viewBox', `0,0,${this.worldWidth},${this.worldHeight}`)
        .attr('width', this.worldWidth)
        .attr('height', this.worldHeight);

    const projection = d3.geoMercator().scale(100).center([0, 20])
        .translate([this.worldWidth / 2, this.worldHeight / 2]);
    const path = d3.geoPath().projection(projection);
    const countByName = d3.map();

    d3.json('../assets/world.json').then(world => {
      dataset.forEach(data => countByName.set(data.country_code, data.count));
      const countries = topojson.feature(world, world.objects.countries);

      svg.append('g')
          .attr('class', 'legendLinear')
          .attr('transform', 'translate(810, 450)');

      const legendLinear = legend.legendColor()
          .shape('rect')
          /*.labels([
              '0',
              '0 to 10',
              '10 to 50',
              '50 to 100',
              '100 to 200',
              '200 to 300',
              '300 to 400',
              '400 to 500'
          ])*/
          .orient('vertical')
          .shapeWidth(40)
          .scale(colorScale);

      svg.selectAll('path').data(countries.features)
          .enter().append('path')
          .attr('d', path)
          // .attr('class', d => {console.log(d.properties.name + ' ' + d.properties.ISO_A2); })
          .attr('stroke', 'black')
          .attr('stroke-opacity', 0.2)
          .attr('fill', d => {
            const countryCount = countByName.get(d.properties.ISO_A2);
            if (isUndefined(countryCount)) {
               return colorScale(0);
             } else {
               return colorScale(countryCount);
             }
          });

      svg.select('.legendLinear')
        .call(legendLinear);
    });
  }

  createUSMap() {
    /*const svg = d3.select('#Worldchart')
        .append('svg')
        .attr('viewBox', `0,0,${this.USWidth},${this.USHeight}`)
        .attr('width', this.USWidth)
        .attr('height', this.USHeight);*/
  }
}
