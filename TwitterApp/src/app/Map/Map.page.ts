import {Component, OnInit} from '@angular/core';

@Component({
  selector: 'app-Map',
  templateUrl: 'Map.page.html',
  styleUrls: ['Map.page.scss']
})
export class MapPage implements OnInit {
  loadingWorld: boolean;
  errorWorld: string;
  loadingUS: boolean;
  errorUS: string;

  constructor() {}

  ngOnInit(): void {
  }

}
