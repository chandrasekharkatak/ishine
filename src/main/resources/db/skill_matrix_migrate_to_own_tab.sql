-- One-time: move existing "Skill Matrix" feature from Performance tab to standalone Skill Matrix tab.
-- Safe if tab skill-matrix already exists (from seed). Run on UAT/prod after reviewing.

INSERT INTO tab_master (tab_name, tab_route_name, tab_icon, tab_sequence)
SELECT 'Skill Matrix', 'skill-matrix', 'fa fa-table', COALESCE((SELECT MAX(tm.tab_sequence) FROM tab_master tm), 0) + 1
WHERE NOT EXISTS (SELECT 1 FROM tab_master x WHERE x.tab_route_name = 'skill-matrix');

UPDATE feature_master f
INNER JOIN tab_master t ON t.tab_route_name = 'skill-matrix'
SET f.tab_id = t.tab_id
WHERE f.feature_name = 'Skill Matrix';
