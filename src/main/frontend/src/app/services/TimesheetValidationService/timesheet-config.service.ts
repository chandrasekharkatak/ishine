import { Injectable } from '@angular/core';

/**
 * Day Type Configuration Interface
 */
export interface DayTypeConfig {
  fillableDayTypes: number[]; // Day types that require time entries (e.g., [1, 3, 8])
  nonFillableDayTypes: number[]; // Day types that don't require time entries (e.g., [4, 6, 7])
  requiresTimeEntry: boolean; // Whether fillable day types require time entry
  allowsNightShift: boolean; // Whether night shift is allowed
  defaultWorkLocationTypeId?: number; // Default work location type ID for non-fillable day types
}

/**
 * Timesheet Configuration Service
 * Manages configuration for timesheet validation rules
 */
@Injectable({
  providedIn: 'root'
})
export class TimesheetConfigService {
  /**
   * Day Type Configuration
   * Fillable day types: 1 (Working), 3 (Non-working), 8 (Other fillable types)
   * Non-fillable day types: 4 (Public Holiday), 6 (Week Off), 7 (Leave)
   */
  private dayTypeConfig: DayTypeConfig = {
    fillableDayTypes: [1, 3, 8],
    nonFillableDayTypes: [2, 4, 5, 6, 7],
    requiresTimeEntry: true,
    allowsNightShift: true,
    defaultWorkLocationTypeId: 4
  };

  /**
   * Checks if a day type is fillable (requires time entries)
   * @param dayType - The day type ID to check
   * @returns true if the day type requires time entries, false otherwise
   */
  isDayTypeFillable(dayType: number | null): boolean {
    if (!dayType) return false;
    return this.dayTypeConfig.fillableDayTypes.includes(dayType);
  }

  /**
   * Checks if a day type is non-fillable (doesn't require time entries)
   * @param dayType - The day type ID to check
   * @returns true if the day type doesn't require time entries, false otherwise
   */
  isDayTypeNonFillable(dayType: number | null): boolean {
    if (!dayType) return false;
    return this.dayTypeConfig.nonFillableDayTypes.includes(dayType);
  }

  /**
   * Gets the fillable day types
   * @returns Array of fillable day type IDs
   */
  getFillableDayTypes(): number[] {
    return [...this.dayTypeConfig.fillableDayTypes];
  }

  /**
   * Gets the non-fillable day types
   * @returns Array of non-fillable day type IDs
   */
  getNonFillableDayTypes(): number[] {
    return [...this.dayTypeConfig.nonFillableDayTypes];
  }

  /**
   * Checks if night shift is allowed for the given day type
   * @param dayType - The day type ID to check
   * @returns true if night shift is allowed, false otherwise
   */
  allowsNightShift(dayType: number | null): boolean {
    if (!dayType) return false;
    return this.dayTypeConfig.allowsNightShift && this.isDayTypeFillable(dayType);
  }

  /**
   * Gets the default work location type ID for non-fillable day types
   * @returns Default work location type ID
   */
  getDefaultWorkLocationTypeId(): number {
    return this.dayTypeConfig.defaultWorkLocationTypeId || 4;
  }

  /**
   * Updates the day type configuration
   * Useful for runtime configuration changes or testing
   * @param config - Partial configuration to update
   */
  updateConfig(config: Partial<DayTypeConfig>): void {
    this.dayTypeConfig = { ...this.dayTypeConfig, ...config };
  }

  /**
   * Gets the complete day type configuration
   * @returns Current day type configuration
   */
  getConfig(): DayTypeConfig {
    return { ...this.dayTypeConfig };
  }

  /**
   * Resets configuration to default values
   */
  resetConfig(): void {
    this.dayTypeConfig = {
      fillableDayTypes: [1, 3, 8],
      nonFillableDayTypes:[2,4,5, 6, 7],
      requiresTimeEntry: true,
      allowsNightShift: true,
      defaultWorkLocationTypeId: 4
    };
  }
}
