export class OrgChartNode {
  name: string = '';
  title?: string = '';
  cssClass: string = '';
  childs: OrgChartNode[] = [];
  image: string = ''; // Required by INode interface
  description: string;

  
  // Additional properties for project structure
  type?: 'root' | 'department' | 'client' | 'project' = 'project';
  deptName?: string;
  clientName?: string;
  projectName?: string;

  constructor(
    name?: string, 
    cssClass?: string, 
    title?: string, 
    description?  : string,
    type?: 'root' | 'department' | 'client' | 'project',
    image: string = ''
  ) {
    this.name = name || '';
    this.cssClass = cssClass || '';
    this.title = title || '';
    this.type = type || 'project';
    this.image = image || ''; // Set default empty image
  }
}