import { Pipe, PipeTransform } from '@angular/core';

@Pipe({
  name: 'rewardFilter'
})
export class RewardFilterPipe implements PipeTransform {

  transform(items: any[], searchText: string): any[] {
    if (!items) return [];
    if (!searchText) return items;

    searchText = searchText.toLowerCase();
    return items.filter(item => {
      return item.employeeNameForReward?.toLowerCase().includes(searchText) || 
             item.teamName?.toLowerCase().includes(searchText);
    });
  }

}
