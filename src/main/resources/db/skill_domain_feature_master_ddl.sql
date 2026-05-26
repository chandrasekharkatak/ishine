-- Domain-level business features: either on the domain only (subdomain_id NULL)
-- or on a sub domain (subdomain_id set; must belong to domain_id).

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
