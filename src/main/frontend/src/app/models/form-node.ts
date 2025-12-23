export class FormNode{
  fields: any;
  formDef: any;
  children: FormNode[];

  constructor(formDef: any, fields: any = {}, children: FormNode[] = []) {
    this.formDef = formDef;
    this.fields = fields;
    this.children = children;
  }
}