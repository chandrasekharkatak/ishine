import { TableFieldConfig } from "./tableFieldConfig";

export class FormField {
    id: string;
    type: string;
    label: string;
    name: string;
    required?: boolean;
    placeholder?: string;
    defaultValue?: any;
    options?: any;
    optionSource?: 'static' | 'api' | 'dependent';
    apiUrl?: string;
    apiLabelKey?: string;
    apiValueKey?: string;
    width: number;
    rowPosition: number;
    parentField?: string;
    dependentApiUrl?: string;
    dependentLabelKey?: string;
    dependentValueKey?: string;
    dependentParamName?: string;
    multiple?: boolean;
    tableConfig?: TableFieldConfig = new TableFieldConfig();
}