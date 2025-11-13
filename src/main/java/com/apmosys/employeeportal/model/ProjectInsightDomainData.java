package com.apmosys.employeeportal.model;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Table;
import javax.persistence.Transient;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.springframework.beans.factory.annotation.Value;

import javax.persistence.Index;
import javax.persistence.CascadeType;
import javax.persistence.Id;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.JoinColumn;
import javax.persistence.Column;
import javax.persistence.PrePersist;

import java.util.*;
import java.util.stream.Collectors;
import java.time.LocalDateTime;

import com.apmosys.employeeportal.listener.ProjectInsightDomainDataListener;
import com.fasterxml.jackson.annotation.JsonIgnore;

import lombok.Data;

@Data
@Entity
// @EntityListeners(ProjectInsightDomainDataListener.class)
@Table(name = "project_insight_domain_data", indexes = {
        @Index(name = "name", columnList = "name")
})
public class ProjectInsightDomainData {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, name = "name")
    private String name;

    private String type;

    @Column(name = "is_active")
    private Boolean isActive = true;

    @Column(name = "created_by")
    private Long createdBy;

    @Column(name = "is_approved")
    private String isApproved = "pending";

    @Column(name = "approved_by")
    private Long approvedBy;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_id")
    @JsonIgnore
    private ProjectInsightDomainData parent;

    @OneToMany(mappedBy = "parent", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ProjectInsightDomainData> children = new ArrayList<>();

    @CreationTimestamp
    @Column(name = "created_on")
    private LocalDateTime createdOn;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "domaincolor_code")
    private String domaincolorCode;

    @PrePersist
    public void assignRandomColor() {
        if ("domain".equalsIgnoreCase(this.type) &&
                (this.domaincolorCode == null || this.domaincolorCode.isEmpty())) {
            this.domaincolorCode = getRandomWarmColor();
        }
    }

    private String getRandomWarmColor() {
        Random random = new Random();
        // Hue: allow all color ranges (0–360°)
        float hue = random.nextFloat();
        // Very low saturation → muted, pastel tones
        float saturation = 0.15f + random.nextFloat() * 0.15f; // 0.15–0.30
        // Very high lightness → soft and pale
        float lightness = 0.85f + random.nextFloat() * 0.10f; // 0.85–0.95
        return hslToHex(hue, saturation, lightness);
    }

    private String hslToHex(float h, float s, float l) {
        float r, g, b;
        if (s == 0) {
            r = g = b = l;
        } else {
            float q = l < 0.5 ? l * (1 + s) : l + s - l * s;
            float p = 2 * l - q;
            r = hue2rgb(p, q, h + 1f / 3f);
            g = hue2rgb(p, q, h);
            b = hue2rgb(p, q, h - 1f / 3f);
        }
        int R = Math.round(r * 255);
        int G = Math.round(g * 255);
        int B = Math.round(b * 255);
        return String.format("#%02X%02X%02X", R, G, B);
    }

    private float hue2rgb(float p, float q, float t) {
        if (t < 0)
            t += 1;
        if (t > 1)
            t -= 1;
        if (t < 1f / 6f)
            return p + (q - p) * 6 * t;
        if (t < 1f / 2f)
            return q;
        if (t < 2f / 3f)
            return p + (q - p) * (2f / 3f - t) * 6;
        return p;
    }

}