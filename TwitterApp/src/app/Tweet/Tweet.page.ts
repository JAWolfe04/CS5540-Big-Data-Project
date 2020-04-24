import { Component, OnInit } from '@angular/core';
import {DataService} from '../service/data.service';
import * as d3 from 'd3';

@Component({
  selector: 'app-Tweet',
  templateUrl: './Tweet.page.html',
  styleUrls: ['./Tweet.page.scss'],
})
export class TweetPage implements OnInit {
  freqError: string;
  freqWidth = 500;
  freqHeight = 500;

  retweetError: string;


  constructor(private dataService: DataService) { }

  ngOnInit() {
    this.dataService.getTweetFreq().subscribe((freqData: any) => {
      this.createPie([
            {name: 'Retweets', percent: freqData.Retweets},
            {name: 'Replies', percent: freqData.Replies},
            {name: 'Tweets', percent: freqData.Tweets}
          ]);
        },
        error => {
          this.freqError = 'Unable to load Tweet Frequency Data';
        }
    );
  }

  createPie(dataset) {
    const color = d3.scaleOrdinal(d3.schemeCategory10);
    const radius = this.freqWidth * 0.49;

    const svg = d3.select('#Frequencies')
        .append('svg')
        .attr('width', this.freqWidth)
        .attr('height', this.freqHeight)
        .append('g')
        .attr('transform', `translate(${this.freqWidth / 2},${this.freqHeight / 2})`);

    const pie = d3.pie()
        .value(d => d.percent)
        .sort(null);

    const arc = d3.arc()
        .outerRadius(radius)
        .innerRadius(0);

    const label = d3.arc()
        .outerRadius(radius * 0.9)
        .innerRadius(radius * 0.5);

    svg.selectAll('path')
        .data(pie(dataset))
        .enter()
        .append('path')
        .attr('d', arc)
        .attr('fill', (d, i) => color(i));

    svg.selectAll('text')
        .data(pie(dataset))
        .enter()
        .append('text')
        .attr('transform', d => 'translate(' + label.centroid(d) + ')')
        .attr('dy', '.4em')
        .attr('text-anchor', 'middle')
        .text(d => d.data.name)
        .attr('fill', '#fff')
        .attr('font-size', '20px');
  }
}
