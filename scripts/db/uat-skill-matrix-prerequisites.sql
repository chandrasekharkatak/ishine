-- Skill Matrix prerequisites on db_emp_backup_new (when skills_master / skill_category_master missing).
-- Matches emp_portal_db structure; empty tables — seed skills via app or copy from emp_portal_db if needed.

CREATE TABLE IF NOT EXISTS skill_category_master (
  category_id INT NOT NULL AUTO_INCREMENT,
  category_name VARCHAR(100) NOT NULL,
  PRIMARY KEY (category_id),
  UNIQUE KEY category_name (category_name)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE IF NOT EXISTS skills_master (
  skill_id INT NOT NULL AUTO_INCREMENT,
  skill_name VARCHAR(120) NOT NULL,
  category_id INT NOT NULL,
  skill_type ENUM('Required','Optional') DEFAULT 'Optional',
  is_active TINYINT(1) DEFAULT 1,
  created_at TIMESTAMP NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  department_id BIGINT NOT NULL,
  created_via_skillmatrix_submit TINYINT(1) NOT NULL DEFAULT 0,
  created_by_emp_id BIGINT NULL,
  created_for_dept_id BIGINT NULL,
  PRIMARY KEY (skill_id),
  KEY fk_skill_department (department_id),
  KEY fk_skill_category (category_id),
  CONSTRAINT fk_skill_category FOREIGN KEY (category_id) REFERENCES skill_category_master (category_id),
  CONSTRAINT fk_skill_department FOREIGN KEY (department_id) REFERENCES department (dept_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE IF NOT EXISTS subskills_master (
  subskill_id INT NOT NULL AUTO_INCREMENT,
  skill_id INT NOT NULL,
  subskill_name VARCHAR(255) NOT NULL,
  created_at TIMESTAMP NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  is_active TINYINT(1) DEFAULT 1,
  PRIMARY KEY (subskill_id),
  UNIQUE KEY unique_subskill (skill_id, subskill_name),
  CONSTRAINT fk_subskill_skill FOREIGN KEY (skill_id) REFERENCES skills_master (skill_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- Minimal category so skill matrix DDL can run (optional seed row)
INSERT INTO skill_category_master (category_id, category_name)
SELECT 1, 'General'
WHERE NOT EXISTS (SELECT 1 FROM skill_category_master LIMIT 1);
