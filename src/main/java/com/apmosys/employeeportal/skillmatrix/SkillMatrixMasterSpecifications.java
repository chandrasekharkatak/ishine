package com.apmosys.employeeportal.skillmatrix;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import javax.persistence.criteria.Expression;
import javax.persistence.criteria.Join;
import javax.persistence.criteria.JoinType;
import javax.persistence.criteria.Predicate;

import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.StringUtils;

import com.apmosys.employeeportal.model.Department;
import com.apmosys.employeeportal.model.SkillCategoryMaster;
import com.apmosys.employeeportal.model.SkillDomainFeatureMaster;
import com.apmosys.employeeportal.model.SkillDomainMaster;
import com.apmosys.employeeportal.model.SkillSubdomainMaster;
import com.apmosys.employeeportal.model.SkillsMaster;
import com.apmosys.employeeportal.model.SubskillsMaster;

/**
 * Server-side filters for Skill Matrix master lists (used with paging).
 */
public final class SkillMatrixMasterSpecifications {

	private SkillMatrixMasterSpecifications() {
	}

	private static String likePattern(String raw) {
		if (raw == null) {
			return "%";
		}
		String t = raw.trim().toLowerCase(Locale.ROOT);
		return "%" + t.replace("\\", "\\\\").replace("%", "\\%").replace("_", "\\_") + "%";
	}

	public static Specification<SkillCategoryMaster> skillCategoryFilter(String categoryId, String categoryName) {
		return (root, query, cb) -> {
			List<Predicate> parts = new ArrayList<>();
			if (StringUtils.hasText(categoryId)) {
				String t = categoryId.trim();
				Expression<String> idStr = cb.function("CONCAT", String.class, cb.literal(""), root.get("categoryId"));
				parts.add(cb.like(cb.lower(idStr), likePattern(t), '\\'));
			}
			if (StringUtils.hasText(categoryName)) {
				parts.add(cb.like(cb.lower(root.get("categoryName")), likePattern(categoryName.trim()), '\\'));
			}
			return parts.isEmpty() ? cb.conjunction() : cb.and(parts.toArray(new Predicate[0]));
		};
	}

	/**
	 * Active skills for a department and exact skill type ({@code Required} / {@code Optional}, case-insensitive),
	 * optional name search.
	 */
	public static Specification<SkillsMaster> skillsDeptPool(Long departmentId, String skillTypeCanonical,
			String nameSearch) {
		return (root, query, cb) -> {
			List<Predicate> parts = new ArrayList<>();
			parts.add(cb.equal(root.get("departmentId"), departmentId));
			parts.add(cb.equal(cb.lower(root.get("skillType")), skillTypeCanonical.trim().toLowerCase(Locale.ROOT)));
			parts.add(cb.or(cb.isTrue(root.get("isActive")), cb.isNull(root.get("isActive"))));
			if (StringUtils.hasText(nameSearch)) {
				parts.add(cb.like(cb.lower(root.get("skillName")), likePattern(nameSearch.trim()), '\\'));
			}
			return cb.and(parts.toArray(new Predicate[0]));
		};
	}

	public static Specification<SkillsMaster> skillsAnyActiveForDepartment(Long departmentId) {
		return (root, query, cb) -> cb.and(cb.equal(root.get("departmentId"), departmentId),
				cb.or(cb.isTrue(root.get("isActive")), cb.isNull(root.get("isActive"))));
	}

	public static Specification<SkillsMaster> skillsMasterFilter(String skillId, String skillName, String categoryName,
			String skillType, String activeDisplay, String departmentName) {
		return (root, query, cb) -> {
			Join<SkillsMaster, SkillCategoryMaster> cat = root.join("skillCategory", JoinType.INNER);
			List<Predicate> parts = new ArrayList<>();
			if (StringUtils.hasText(skillId)) {
				String t = skillId.trim();
				Expression<String> idStr = cb.function("CONCAT", String.class, cb.literal(""), root.get("skillId"));
				parts.add(cb.like(cb.lower(idStr), likePattern(t), '\\'));
			}
			if (StringUtils.hasText(skillName)) {
				parts.add(cb.like(cb.lower(root.get("skillName")), likePattern(skillName.trim()), '\\'));
			}
			if (StringUtils.hasText(categoryName)) {
				parts.add(cb.like(cb.lower(cat.get("categoryName")), likePattern(categoryName.trim()), '\\'));
			}
			if (StringUtils.hasText(skillType)) {
				parts.add(cb.like(cb.lower(root.get("skillType")), likePattern(skillType.trim()), '\\'));
			}
			if (StringUtils.hasText(activeDisplay)) {
				String ad = activeDisplay.trim().toLowerCase(Locale.ROOT);
				if ("yes".equals(ad) || "y".equals(ad) || "1".equals(ad) || "true".equals(ad)) {
					parts.add(cb.isTrue(root.get("isActive")));
				} else if ("no".equals(ad) || "n".equals(ad) || "0".equals(ad) || "false".equals(ad)) {
					parts.add(cb.or(cb.isFalse(root.get("isActive")), cb.isNull(root.get("isActive"))));
				}
			}
			if (StringUtils.hasText(departmentName)) {
				Join<SkillsMaster, Department> dept = root.join("department", JoinType.INNER);
				parts.add(cb.like(cb.lower(dept.get("name")), likePattern(departmentName.trim()), '\\'));
			}
			return parts.isEmpty() ? cb.conjunction() : cb.and(parts.toArray(new Predicate[0]));
		};
	}

	public static Specification<SubskillsMaster> subskillsMasterFilter(String subskillId, String subskillName,
			String skillName, String activeDisplay) {
		return (root, query, cb) -> {
			Join<SubskillsMaster, SkillsMaster> sk = root.join("skill", JoinType.INNER);
			List<Predicate> parts = new ArrayList<>();
			if (StringUtils.hasText(subskillId)) {
				String t = subskillId.trim();
				Expression<String> idStr = cb.function("CONCAT", String.class, cb.literal(""), root.get("subskillId"));
				parts.add(cb.like(cb.lower(idStr), likePattern(t), '\\'));
			}
			if (StringUtils.hasText(subskillName)) {
				parts.add(cb.like(cb.lower(root.get("subskillName")), likePattern(subskillName.trim()), '\\'));
			}
			if (StringUtils.hasText(skillName)) {
				parts.add(cb.like(cb.lower(sk.get("skillName")), likePattern(skillName.trim()), '\\'));
			}
			if (StringUtils.hasText(activeDisplay)) {
				String ad = activeDisplay.trim().toLowerCase(Locale.ROOT);
				if ("yes".equals(ad) || "y".equals(ad) || "1".equals(ad) || "true".equals(ad)) {
					parts.add(cb.isTrue(root.get("isActive")));
				} else if ("no".equals(ad) || "n".equals(ad) || "0".equals(ad) || "false".equals(ad)) {
					parts.add(cb.or(cb.isFalse(root.get("isActive")), cb.isNull(root.get("isActive"))));
				}
			}
			return parts.isEmpty() ? cb.conjunction() : cb.and(parts.toArray(new Predicate[0]));
		};
	}

	public static Specification<SkillDomainMaster> skillDomainFilter(String domainId, String domainName) {
		return (root, query, cb) -> {
			List<Predicate> parts = new ArrayList<>();
			if (StringUtils.hasText(domainId)) {
				String t = domainId.trim();
				Expression<String> idStr = cb.function("CONCAT", String.class, cb.literal(""), root.get("domainId"));
				parts.add(cb.like(cb.lower(idStr), likePattern(t), '\\'));
			}
			if (StringUtils.hasText(domainName)) {
				parts.add(cb.like(cb.lower(root.get("domainName")), likePattern(domainName.trim()), '\\'));
			}
			return parts.isEmpty() ? cb.conjunction() : cb.and(parts.toArray(new Predicate[0]));
		};
	}

	public static Specification<SkillSubdomainMaster> skillSubdomainFilter(String subdomainId, String subdomainName,
			String domainName, String activeDisplay, Integer domainIdEq) {
		return (root, query, cb) -> {
			Join<SkillSubdomainMaster, SkillDomainMaster> dom = root.join("domain", JoinType.INNER);
			List<Predicate> parts = new ArrayList<>();
			if (domainIdEq != null) {
				parts.add(cb.equal(root.get("domainId"), domainIdEq));
			}
			if (StringUtils.hasText(subdomainId)) {
				String t = subdomainId.trim();
				Expression<String> idStr = cb.function("CONCAT", String.class, cb.literal(""), root.get("subdomainId"));
				parts.add(cb.like(cb.lower(idStr), likePattern(t), '\\'));
			}
			if (StringUtils.hasText(subdomainName)) {
				parts.add(cb.like(cb.lower(root.get("subdomainName")), likePattern(subdomainName.trim()), '\\'));
			}
			if (StringUtils.hasText(domainName)) {
				parts.add(cb.like(cb.lower(dom.get("domainName")), likePattern(domainName.trim()), '\\'));
			}
			if (StringUtils.hasText(activeDisplay)) {
				String ad = activeDisplay.trim().toLowerCase(Locale.ROOT);
				if ("yes".equals(ad) || "y".equals(ad) || "1".equals(ad) || "true".equals(ad)) {
					parts.add(cb.isTrue(root.get("isActive")));
				} else if ("no".equals(ad) || "n".equals(ad) || "0".equals(ad) || "false".equals(ad)) {
					parts.add(cb.or(cb.isFalse(root.get("isActive")), cb.isNull(root.get("isActive"))));
				}
			}
			return parts.isEmpty() ? cb.conjunction() : cb.and(parts.toArray(new Predicate[0]));
		};
	}

	public static Specification<SkillDomainFeatureMaster> skillDomainFeatureFilter(String featureId, String featureName,
			String domainName, String subdomainName, String activeDisplay) {
		return (root, query, cb) -> {
			Join<SkillDomainFeatureMaster, SkillDomainMaster> dom = root.join("domain", JoinType.INNER);
			Join<SkillDomainFeatureMaster, SkillSubdomainMaster> sub = root.join("subdomain", JoinType.LEFT);
			List<Predicate> parts = new ArrayList<>();
			if (StringUtils.hasText(featureId)) {
				String t = featureId.trim();
				Expression<String> idStr = cb.function("CONCAT", String.class, cb.literal(""), root.get("featureId"));
				parts.add(cb.like(cb.lower(idStr), likePattern(t), '\\'));
			}
			if (StringUtils.hasText(featureName)) {
				parts.add(cb.like(cb.lower(root.get("featureName")), likePattern(featureName.trim()), '\\'));
			}
			if (StringUtils.hasText(domainName)) {
				parts.add(cb.like(cb.lower(dom.get("domainName")), likePattern(domainName.trim()), '\\'));
			}
			if (StringUtils.hasText(subdomainName)) {
				parts.add(cb.and(cb.isNotNull(root.get("subdomainId")),
						cb.like(cb.lower(sub.get("subdomainName")), likePattern(subdomainName.trim()), '\\')));
			}
			if (StringUtils.hasText(activeDisplay)) {
				String ad = activeDisplay.trim().toLowerCase(Locale.ROOT);
				if ("yes".equals(ad) || "y".equals(ad) || "1".equals(ad) || "true".equals(ad)) {
					parts.add(cb.isTrue(root.get("isActive")));
				} else if ("no".equals(ad) || "n".equals(ad) || "0".equals(ad) || "false".equals(ad)) {
					parts.add(cb.or(cb.isFalse(root.get("isActive")), cb.isNull(root.get("isActive"))));
				}
			}
			return parts.isEmpty() ? cb.conjunction() : cb.and(parts.toArray(new Predicate[0]));
		};
	}
}
