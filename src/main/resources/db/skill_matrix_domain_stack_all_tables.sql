-- Skill Matrix domain stack: domain → sub domain (optional) → domain feature (optional sub domain).
-- Run on emp_portal_db (UAT/prod) once. Uses IF NOT EXISTS; safe to re-run if tables already exist.
-- Applied to UAT emp_portal_db 2026-04-19.

SET NAMES utf8mb4;

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

CREATE TABLE IF NOT EXISTS skill_domain_feature_master (
  feature_id INT NOT NULL AUTO_INCREMENT,
  domain_id INT NOT NULL,
  subdomain_id INT NULL,
  feature_name VARCHAR(255) NOT NULL,
  is_active TINYINT(1) DEFAULT 1,
  PRIMARY KEY (feature_id),
  KEY idx_sdfm_domain (domain_id),
  KEY idx_sdfm_subdomain (subdomain_id),
  CONSTRAINT fk_sdfm_domain FOREIGN KEY (domain_id)
    REFERENCES skill_domain_master (domain_id),
  CONSTRAINT fk_sdfm_subdomain FOREIGN KEY (subdomain_id)
    REFERENCES skill_subdomain_master (subdomain_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
