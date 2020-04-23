import {Component, Injectable, OnInit} from '@angular/core';
import {DataService} from '../service/data.service';
import * as d3 from 'd3';

@Component({
  selector: 'app-Hashtag',
  templateUrl: 'Hashtag.page.html',
  styleUrls: ['Hashtag.page.scss']
})
export class HashtagPage implements OnInit {
  loadingBubble: boolean;
  errorBubble: string;
  bubbleHeight = 1000;
  bubbleWidth = 1000;

  loadingTopHash: boolean;
  errorTopHash: string;
  TopHashHeight = 500;
  TopHashWidth = 1200;

  loadingTopHashTime: boolean;
  errorTopHashTime: string;
  TopHashTimeHeight = 500;
  TopHashTimeWidth = 750;

  constructor(private dataservice: DataService) {}

  ngOnInit() {
    this.loadingBubble = true;
    this.loadingTopHash = true;
    this.loadingTopHashTime = true;

    this.dataservice.getBubbleChart().subscribe(hashtags => {
          this.createBubbles(hashtags);
          this.loadingBubble = false;
        },
        error => {
          this.errorBubble = 'Unable to load Bubble Chart';
          this.loadingBubble = false;
    });

    this.dataservice.getTop10Hash().subscribe(top10Hash => {
          this.createTop10Hash(top10Hash);
          this.loadingTopHash = false;
        },
        error => {
          this.errorTopHash = 'Unable to load Top 10 Hashtags';
          this.loadingTopHash = false;
    });

    this.dataservice.getTopHashTime().subscribe(topHashTime => {
          this.createTopHashTime(topHashTime);
          this.loadingTopHashTime = false;
        },
        error => {
            this.errorTopHashTime = 'Unable to load Hashtag Chart';
            this.loadingTopHashTime = false;
    });
  }

  createTop10Hash(dataset) {
      const margin = 200;
      const width = this.TopHashWidth - margin;
      const height = this.TopHashHeight - margin;

      const svg = d3.select('#Top10Hash')
          .append('svg')
          .attr('viewBox', `0,0,${this.TopHashWidth},${this.TopHashHeight}`)
          .attr('width', this.TopHashWidth)
          .attr('height', this.TopHashHeight);

      const xScale = d3.scaleBand().range ([0, width]).padding(0.4);
      const yScale = d3.scaleLinear().range ([height, 0]);

      const g = svg.append('g').attr('transform', 'translate(' + 100 + ',' + 100 + ')');

      xScale.domain(dataset.Hashtags.map(d => d.text));
      yScale.domain([0, d3.max(dataset.Hashtags, d => d.count)]);

      g.append('g')
          .attr('transform', 'translate(0,' + height + ')')
          .call(d3.axisBottom(xScale))
          .append('text')
          .attr('y', height - 250)
          .attr('x', width - 100)
          .attr('text-anchor', 'end')
          .attr('stroke', 'black')
          .text('Hashtag');

      g.append('g')
          .call(d3.axisLeft(yScale).tickFormat(d => {
              return d;
          }).ticks(10))
          .append('text')
          .attr('transform', 'rotate(-90)')
          .attr('y', 6)
          .attr('dy', '-5.1em')
          .attr('text-anchor', 'end')
          .attr('stroke', 'black')
          .text('Tweets with Hashtag');

      g.selectAll('.bar')
          .data(dataset.Hashtags)
          .enter().append('rect')
          .style('fill', 'steelblue')
          .attr('x', d => xScale(d.text))
          .attr('y', d => yScale(d.count))
          .attr('width', xScale.bandwidth())
          .attr('height', d => height - yScale(d.count));
  }

  createTopHashTime(dataset) {

  }

  createBubbles(dataset) {
    const color = d3.scaleOrdinal(d3.schemeCategory10);

    const bubble = d3.pack()
        .size([this.bubbleWidth, this.bubbleHeight])
        .padding(1.5);

    const svg = d3.select('#BubbleChart')
        .append('svg')
        .attr('viewBox', `0,0,${this.bubbleWidth},${this.bubbleHeight}`)
        .attr('width', this.bubbleWidth)
        .attr('height', this.bubbleHeight)
        .attr('class', 'bubble');

    const nodes = d3.hierarchy(dataset)
        .sum((d: any) => {
          return d.count;
        });

    const node = svg.selectAll('.node')
        .data(bubble(nodes).descendants())
        .enter()
        .filter(d => {
          return !d.children;
        })
        .append('g')
        .attr('class', 'node')
        .attr('transform', d => 'translate(' + d.x + ',' + d.y + ')')
        .style('fill', (d, i: any) => color(i));

    node.append('title')
        .text((d: any) =>  d.data.text + ': ' + d.data.count);

    node.append('circle')
        .attr('x', (d) => d.x)
        .attr('y', (d) => d.y)
        .attr('r', (d) => d.r)
        .style('fill', (d, i: any) => color(i));

    node.append('text')
        .attr('dy', '.2em')
        .style('text-anchor', 'middle')
        .text((d: any) => d.data.text)
        .attr('font-family', 'sans-serif')
        .attr('font-size', (d) => d.r / 5)
        .attr('fill', 'white');

    node.append('text')
        .attr('dy', '1.3em')
        .style('text-anchor', 'middle')
        .text((d: any) => d.data.count)
        .attr('font-family', 'Gill Sans')
        .attr('font-size', (d) =>  d.r / 5)
        .attr('fill', 'white');
  }
}