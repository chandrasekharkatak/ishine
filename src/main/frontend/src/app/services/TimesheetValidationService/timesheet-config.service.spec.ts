import { TestBed } from '@angular/core/testing';
import { TimesheetConfigService } from './timesheet-config.service';

describe('TimesheetConfigService', () => {
  let service: TimesheetConfigService;

  beforeEach(() => {
    TestBed.configureTestingModule({});
    service = TestBed.inject(TimesheetConfigService);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });

  describe('isDayTypeFillable', () => {
    it('should return true for fillable day types (1, 3, 8)', () => {
      expect(service.isDayTypeFillable(1)).toBe(true);
      expect(service.isDayTypeFillable(3)).toBe(true);
      expect(service.isDayTypeFillable(8)).toBe(true);
    });

    it('should return false for non-fillable day types (4, 6, 7)', () => {
      expect(service.isDayTypeFillable(4)).toBe(false);
      expect(service.isDayTypeFillable(6)).toBe(false);
      expect(service.isDayTypeFillable(7)).toBe(false);
    });

    it('should return false for null day type', () => {
      expect(service.isDayTypeFillable(null)).toBe(false);
    });

    it('should return false for unknown day types', () => {
      expect(service.isDayTypeFillable(99)).toBe(false);
      expect(service.isDayTypeFillable(0)).toBe(false);
      expect(service.isDayTypeFillable(2)).toBe(false);
      expect(service.isDayTypeFillable(5)).toBe(false);
    });
  });

  describe('isDayTypeNonFillable', () => {
    it('should return true for non-fillable day types (4, 6, 7)', () => {
      expect(service.isDayTypeNonFillable(4)).toBe(true);
      expect(service.isDayTypeNonFillable(6)).toBe(true);
      expect(service.isDayTypeNonFillable(7)).toBe(true);
    });

    it('should return false for fillable day types (1, 3, 8)', () => {
      expect(service.isDayTypeNonFillable(1)).toBe(false);
      expect(service.isDayTypeNonFillable(3)).toBe(false);
      expect(service.isDayTypeNonFillable(8)).toBe(false);
    });

    it('should return false for null day type', () => {
      expect(service.isDayTypeNonFillable(null)).toBe(false);
    });
  });

  describe('getFillableDayTypes', () => {
    it('should return array of fillable day types', () => {
      const fillableTypes = service.getFillableDayTypes();
      expect(fillableTypes).toEqual([1, 3, 8]);
      expect(fillableTypes.length).toBe(3);
    });

    it('should return a copy, not the original array', () => {
      const fillableTypes1 = service.getFillableDayTypes();
      const fillableTypes2 = service.getFillableDayTypes();
      expect(fillableTypes1).not.toBe(fillableTypes2); // Different references
      expect(fillableTypes1).toEqual(fillableTypes2); // Same values
    });
  });

  describe('getNonFillableDayTypes', () => {
    it('should return array of non-fillable day types', () => {
      const nonFillableTypes = service.getNonFillableDayTypes();
      expect(nonFillableTypes).toEqual([4, 6, 7]);
      expect(nonFillableTypes.length).toBe(3);
    });

    it('should return a copy, not the original array', () => {
      const nonFillableTypes1 = service.getNonFillableDayTypes();
      const nonFillableTypes2 = service.getNonFillableDayTypes();
      expect(nonFillableTypes1).not.toBe(nonFillableTypes2);
      expect(nonFillableTypes1).toEqual(nonFillableTypes2);
    });
  });

  describe('allowsNightShift', () => {
    it('should return true for fillable day types', () => {
      expect(service.allowsNightShift(1)).toBe(true);
      expect(service.allowsNightShift(3)).toBe(true);
      expect(service.allowsNightShift(8)).toBe(true);
    });

    it('should return false for non-fillable day types', () => {
      expect(service.allowsNightShift(4)).toBe(false);
      expect(service.allowsNightShift(6)).toBe(false);
      expect(service.allowsNightShift(7)).toBe(false);
    });

    it('should return false for null day type', () => {
      expect(service.allowsNightShift(null)).toBe(false);
    });
  });

  describe('getDefaultWorkLocationTypeId', () => {
    it('should return default work location type ID', () => {
      const defaultId = service.getDefaultWorkLocationTypeId();
      expect(defaultId).toBe(4);
    });
  });

  describe('updateConfig', () => {
    it('should update configuration', () => {
      service.updateConfig({
        fillableDayTypes: [1, 2, 3]
      });

      expect(service.getFillableDayTypes()).toEqual([1, 2, 3]);
      expect(service.isDayTypeFillable(2)).toBe(true);
    });

    it('should partially update configuration', () => {
      const originalFillable = service.getFillableDayTypes();
      service.updateConfig({
        defaultWorkLocationTypeId: 5
      });

      expect(service.getDefaultWorkLocationTypeId()).toBe(5);
      expect(service.getFillableDayTypes()).toEqual(originalFillable); // Unchanged
    });

    it('should update allowsNightShift flag', () => {
      service.updateConfig({
        allowsNightShift: false
      });

      expect(service.allowsNightShift(1)).toBe(false);
    });
  });

  describe('getConfig', () => {
    it('should return complete configuration', () => {
      const config = service.getConfig();
      
      expect(config.fillableDayTypes).toEqual([1, 3, 8]);
      expect(config.nonFillableDayTypes).toEqual([4, 6, 7]);
      expect(config.requiresTimeEntry).toBe(true);
      expect(config.allowsNightShift).toBe(true);
      expect(config.defaultWorkLocationTypeId).toBe(4);
    });

    it('should return a copy, not the original object', () => {
      const config1 = service.getConfig();
      const config2 = service.getConfig();
      
      expect(config1).not.toBe(config2); // Different references
      expect(config1).toEqual(config2); // Same values
    });
  });

  describe('resetConfig', () => {
    it('should reset configuration to default values', () => {
      // Modify configuration
      service.updateConfig({
        fillableDayTypes: [9, 10],
        defaultWorkLocationTypeId: 99
      });

      // Reset
      service.resetConfig();

      // Verify default values restored
      expect(service.getFillableDayTypes()).toEqual([1, 3, 8]);
      expect(service.getDefaultWorkLocationTypeId()).toBe(4);
      expect(service.isDayTypeFillable(1)).toBe(true);
      expect(service.isDayTypeFillable(9)).toBe(false);
    });
  });

  describe('Configuration consistency', () => {
    it('should ensure fillable and non-fillable day types are mutually exclusive', () => {
      const fillable = service.getFillableDayTypes();
      const nonFillable = service.getNonFillableDayTypes();
      
      const intersection = fillable.filter(type => nonFillable.includes(type));
      expect(intersection.length).toBe(0);
    });

    it('should handle custom configuration updates correctly', () => {
      service.updateConfig({
        fillableDayTypes: [1, 2, 3],
        nonFillableDayTypes: [4, 5]
      });

      expect(service.isDayTypeFillable(1)).toBe(true);
      expect(service.isDayTypeFillable(2)).toBe(true);
      expect(service.isDayTypeFillable(3)).toBe(true);
      expect(service.isDayTypeFillable(4)).toBe(false);
      expect(service.isDayTypeNonFillable(4)).toBe(true);
      expect(service.isDayTypeNonFillable(5)).toBe(true);
    });
  });
});
