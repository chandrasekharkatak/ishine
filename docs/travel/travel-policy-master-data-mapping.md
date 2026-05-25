# Travel Policy v0.1 — Travel Desk master data mapping

**Policy:** ApMoSys HR Travel Policy, version 0.1, publish date 25-Jan-2023 (approver: Sangeeta Padhy).

**UAT seed script:** `docs/travel/uat-sql/002_travel_policy_master_data.sql`  
**Legacy seed (generic):** `scripts/db/uat-travel-config-master-seed.sql`

Run on UAT:

```bash
mysql -h 192.168.21.195 -u dbuser_emp_portal -p db_emp_backup_new < docs/travel/uat-sql/002_travel_policy_master_data.sql
```

## UI ↔ master data

| Travel Desk UI | Master table | Policy reference |
|----------------|--------------|------------------|
| **Travel** tab — Travel Reason | `travel_reason` | Business purpose (§2, Annexure I) |
| Travel Mode / Class | `travel_mode`, `travel_class` | Flight/Train/Bus/Cab (Annexure I #1, #5) |
| **Hotels** tab | `travel_reason` = **Hotel & Lodging** | Lodging §2, Annexure I #2 |
| Hotel Category / Sub-category / City | `hotel_category`, `hotel_sub_category`, `city` | Per-night limits Annexure I & II |
| 7-day advance (form) | App validation (`today + 7`) | §2a — plan 7 days before travel |
| Approval workflow | `travel_approval_matrix*` | Dept head approval (§2a) |

## Hotel per-night limits (seeded categories)

| Category | Limit (INR/night) | Cities |
|----------|-------------------|--------|
| Metro — TL and Below | 2,000 | Mumbai, Delhi, Chennai, Kolkata, Bengaluru, Hyderabad |
| Metro — Above TL | 4,000 | Same metros |
| Tier A Cities | 1,800 | Annexure II column A (Ahmedabad, Pune, …) |
| Tier B Cities | 1,500 | Surat (and other column B as added) |
| Guest House / Company Facility | Admin booking | — |
| Friends / Relatives Stay | 50% of limit, no bills | — |

Legacy star-based hotel categories (`Budget (3 Star)`, etc.) are set **inactive** after policy seed; use policy tier categories in the Hotels tab.

## Configuration screen

**Configuration → Travel** (`travel-config` component): maintain reasons, modes, classes, hotel categories, sub-categories, and cities. Align edits with this policy document.

## Not in master data (process / future)

- Travel advance (Accounts approval)
- Daily food allowance INR 500/day, INR 200 extra meal
- Project allowance INR 3,500/month
- Own car INR 12/km, motorcycle INR 6/km
- PM+ flight / cab within 300 km rules (designation-based)

These may be handled in reimbursement or separate modules.
