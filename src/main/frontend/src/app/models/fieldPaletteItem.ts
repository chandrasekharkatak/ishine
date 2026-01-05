export class FieldPaletteItem {
  constructor(public type: string, public label: string) {}
}

export class FieldPalette {
  static TEXT = new FieldPaletteItem('text', 'Text Input');
  static TEXTAREA = new FieldPaletteItem('textarea', 'Text Area');
  static SELECT = new FieldPaletteItem('select', 'Dropdown');
  static CHECKBOX = new FieldPaletteItem('checkbox', 'Checkbox');
  static RADIO = new FieldPaletteItem('radio', 'Radio');
  static DATE = new FieldPaletteItem('date', 'Date');
  static NUMBER = new FieldPaletteItem('number', 'Number');
  static EMAIL = new FieldPaletteItem('email', 'Email');
  static FILE = new FieldPaletteItem('file', 'File Upload');
  static TABLE = new FieldPaletteItem('table', 'Table');

  // Optional: Get all items as a list
  static getAll(): FieldPaletteItem[] {
    return [
      FieldPalette.TEXT,
      FieldPalette.TEXTAREA,
      FieldPalette.SELECT,
      FieldPalette.CHECKBOX,
      FieldPalette.RADIO,
      FieldPalette.DATE,
      FieldPalette.NUMBER,
      FieldPalette.EMAIL,
      FieldPalette.FILE,
      FieldPalette.TABLE
    ];
  }
}
