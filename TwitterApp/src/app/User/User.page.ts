import {Component, OnInit} from '@angular/core';
import {DataService} from '../service/data.service';
import * as cloud from 'd3-cloud';
import * as d3 from 'd3';

@Component({
  selector: 'app-User',
  templateUrl: 'User.page.html',
  styleUrls: ['User.page.scss']
})
export class UserPage implements OnInit {
  botLoading: boolean;
  botError: string;
  botWidth = 400;
  botHeight = 400;

  influencerLoading: boolean;
  influencerError: string;
  influencerWidth = 750;
  influencerHeight = 500;

  constructor(private dataService: DataService) {}

  ngOnInit(): void {
    this.influencerLoading = true;
    this.botLoading = true;

    this.dataService.getBots().subscribe((botData: any) => {
          this.createDonuts(botData.Total, '#BotsTotals');
          this.createDonuts(botData.Frequency, '#BotsFrequency');
          this.botLoading = false;
        },
        error => {
          this.botError = 'Unable to load Bot Donut Charts';
          this.botLoading = false;
        }
    );

    this.dataService.getInfluencers().subscribe((influencerData: any) => {
          this.createInfluencerCloud(influencerData.Influencers);
          this.influencerLoading = false;
        },
        error => {
          this.influencerError = 'Unable to load Influencer Word Cloud';
          this.influencerLoading = false;
        }
    );
  }

  createDonuts(dataset, chartName) {
    const color = d3.scaleOrdinal(d3.schemeCategory10);
    const radius = this.botWidth * 0.47;

    const svg = d3.select(chartName)
        .append('svg')
        .attr('width', this.botWidth)
        .attr('height', this.botHeight)
        .append('g')
        .attr('transform', `translate(${this.botWidth / 2},${this.botHeight / 2})`);

    const pie = d3.pie(dataset)
        .value(d => d.percentage)
        .sort(null);

    const arc = d3.arc()
        .outerRadius(radius)
        .innerRadius(radius * 0.7);

    svg.selectAll('path')
        .data(pie(dataset))
        .enter()
        .append('path')
        .attr('d', arc)
        .attr('fill', ((d, i: any) => color(d.data.name)));

    svg.selectAll('text')
        .data(pie(dataset))
        .enter()
        .append('text')
        .attr('transform', d => 'translate(' + arc.centroid(d) + ')')
        .attr('dy', '.4em')
        .attr('text-anchor', 'middle')
        .text(d => d.data.percentage + ' %')
        .attr('fill', '#fff')
        .attr('font-size', '10px');

    const legendRectSize = 20;
    const legendSpacing = 7;

    const legend = svg.selectAll('.legend')
        .data(color.domain())
        .enter()
        .append('g')
        .attr('class', 'legend')
        .attr('transform', (d, i) => 'translate(-35,' + ((i * (legendRectSize + legendSpacing)) - 65) + ')');

    legend.append('rect')
        .attr('width', legendRectSize)
        .attr('height', legendRectSize)
        .attr('rx', 20)
        .attr('ry', 20)
        .attr('fill', color)
        .attr('stroke', color);

    legend.append('text')
        .attr('x', 30)
        .attr('y', 15)
        .text(d =>  d)
        .attr('fill', '#929DAF')
        .attr('font-size', '14px');
  }

  createInfluencerCloud(dataset) {
    const color = d3.scaleOrdinal(d3.schemeCategory10);
    const xScale = d3.scaleLinear()
        .domain([0, d3.max(dataset, d => d.followers_count)])
        .range([5, 70]);

    const layout = cloud()
        .size([this.influencerWidth, this.influencerHeight])
        .words(dataset)
        .padding(0.5)
        .font('Impact')
        .fontSize(d => xScale(d.followers_count))
        .text(d => d.name)
        .on('end', draw);

    layout.start();

    function draw(words) {
      d3.select('#Influencers')
          .append('svg')
          .attr('width', layout.size()[0])
          .attr('height', layout.size()[1])
          .append('g')
          .attr('transform', 'translate(' + layout.size()[0] / 2 + ',' + layout.size()[1] / 2 + ')')
          .selectAll('text')
          .data(words)
          .enter()
          .append('text')
          .style('font-size', (d) => d.size + 'px')
          .style('font-family', (d) => d.font)
          .style('fill', (d, i) => color(i))
          .attr('text-anchor', 'middle')
          .attr('transform', (d) => 'translate(' + [d.x, d.y] + ')rotate(' + d.rotate + ')')
          .text((d) => d.text);
    }
  }
}
