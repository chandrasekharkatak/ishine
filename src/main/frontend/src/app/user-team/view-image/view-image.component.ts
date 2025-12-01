import { Component, Inject, OnInit } from '@angular/core';
import { MAT_DIALOG_DATA, MatDialog, MatDialogRef } from '@angular/material/dialog';

@Component({
  standalone: false,
  selector: 'app-view-milestone-image',
  templateUrl: './view-image.component.html',
  styleUrls: ['./view-image.component.css']
})
export class ViewImageComponent implements OnInit {

  constructor(@Inject(MAT_DIALOG_DATA) public data: { imageUrl: string, fileName: string },
  private dialogRef: MatDialogRef<ViewImageComponent> ) {}
  ngOnInit(): void {
  }
  closeModal(): void {
  this.dialogRef.close();
}

}