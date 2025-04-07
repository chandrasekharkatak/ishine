import { Pipe, PipeTransform } from '@angular/core';

@Pipe({
  name: 'colFilter'
})
export class ColFilterPipe implements PipeTransform {
  transform(items: any, filter: any, defaultFilter: boolean): any {
    if (!filter){
      return items;
    }

    if (!Array.isArray(items)){
      return items;
    }

    if (filter && Array.isArray(items)) {
      let filterKeys = Object.keys(filter);

      if (defaultFilter) {
        return items.filter(item =>
            filterKeys.reduce((x, keyName) =>
                (x && new RegExp(this.escapeRegExp(filter[keyName]), 'gi').test(item[keyName])) || filter[keyName] == "", true));
      }
      else {
        return items.filter(item => {
          return filterKeys.every((keyName) => {
            return new RegExp(this.escapeRegExp(filter[keyName]), 'gi').test(item[keyName]) || filter[keyName] == "";
          });
        });
      }
    }
  }
  escapeRegExp(input: string): string {
    return input.replace(/[.*+?^${}()|[\]\\]/g, '\\$&');
  }
}

// import { Pipe, PipeTransform } from '@angular/core';

// @Pipe({
//   name: 'colFilter'
// })
// export class ColFilterPipe implements PipeTransform {
//   transform(items: any[], filter: any, defaultFilter: boolean = false): any[] {
//     if (!filter || !Array.isArray(items)) {
//       return items;
//     }

//     const filterKeys = Object.keys(filter);
    
//     return items.filter(item => {
//       return filterKeys.every(key => {
//         if (filter[key] == "") {
//           return true;
//         }
//         const regex = new RegExp(filter[key], 'gi');
//         return regex.test(item[key]);
//       });
//     });
//   }
// }

