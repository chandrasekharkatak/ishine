-- Skill Matrix: Domain Master + Sub Domain Master (run on emp_portal_db or local DB before using APIs).
-- Safe to run once; adjust if tables already exist.

CREATE TABLE IF NOT EXISTS skill_domain_master (
  domain_id INT NOT NULL AUTO_INCREMENT,
  domain_name VARCHAR(150) NOT NULL,
  PRIMARY KEY (domain_id),
  UNIQUE KEY uk_skill_domain_name (domain_name)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS skill_subdomain_master (
  subdomain_id INT NOT NULL AUTO_INCREMENT,
  domain_id INT NOT NULL,
  subdomain_name VARCHAR(255) NOT NULL,
  is_active TINYINT(1) DEFAULT 1,
  PRIMARY KEY (subdomain_id),
  KEY idx_skill_subdomain_domain (domain_id),
  CONSTRAINT fk_skill_subdomain_domain FOREIGN KEY (domain_id)
    REFERENCES skill_domain_master (domain_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
