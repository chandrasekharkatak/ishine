export const ENTITY_TYPES = {
    QUESTION: 1,
    GROUP: 2,
    PROJECT: 3
  } as const;
  
  export type EntityType = typeof ENTITY_TYPES[keyof typeof ENTITY_TYPES];
  