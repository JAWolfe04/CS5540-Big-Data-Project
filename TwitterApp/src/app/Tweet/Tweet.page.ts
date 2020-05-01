import { Component, OnInit } from '@angular/core';
import {DataService} from '../service/data.service';
import * as d3 from 'd3';
import {environment} from '../../environments/environment';

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
  retweetWidth = 500;
  retweetHeight = 500;

  constructor(private dataService: DataService) { }

  ngOnInit() {
    if (!environment.testing) {
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

        /*this.createStacked([{Time: '03-13-23', Retweet_Count: 23758, Followers_Count: 235, Listed_Count: 54},
            {Time: '03-14-00', Retweet_Count: 73265, Followers_Count: 325, Listed_Count: 93},
            {Time: '03-14-01', Retweet_Count: 63495, Followers_Count: 763, Listed_Count: 25}]);*/
    }
  }

  createStacked(dataset) {

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
