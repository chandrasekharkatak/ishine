package com.apmosys.employeeportal.utility;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.criteria.Predicate;

import org.springframework.data.jpa.domain.Specification;

import com.apmosys.employeeportal.model.TagMaster;

public class TagSpecifications {
    public static Specification<TagMaster> tagNameLikeAny(List<String> keywords) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            for (String keyword : keywords) {
                predicates.add(cb.like(cb.lower(root.get("tag")), "%" + keyword.toLowerCase() + "%"));
            }
            return cb.or(predicates.toArray(new Predicate[0]));
        };
    }
}
