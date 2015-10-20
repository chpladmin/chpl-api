CREATE OR REPLACE VIEW openchpl.certified_product_details AS

SELECT

a.certified_product_id,
a.certification_edition_id,
a.product_version_id,
a.testing_lab_id,
a.certification_body_id,
a.chpl_product_number,
a.report_file_location,
a.quality_management_system_att,
a.acb_certification_id,
a.practice_type_id,
a.product_classification_type_id,
a.other_acb,
a.certification_status_id,
a.deleted,
a.visible_on_chpl,
b.year,
c.certification_body_name,
d.product_classification_name,
e.practice_type_name,
f.product_version,
f.product_id,
g.product_name,
g.vendor_id,
h.vendor_name,
i.certification_date,
COALESCE(k.count_certifications, 0) as "count_certifications",
COALESCE(m.count_cqms, 0) as "count_cqms",
a.last_modified_date,
n.certification_status_name

FROM openchpl.certified_product a

LEFT JOIN (SELECT certification_edition_id, year FROM openchpl.certification_edition) b on a.certification_edition_id = b.certification_edition_id

LEFT JOIN (SELECT certification_body_id, name as "certification_body_name" FROM openchpl.certification_body) c on a.certification_body_id = c.certification_body_id

LEFT JOIN (SELECT product_classification_type_id, name as "product_classification_name" FROM openchpl.product_classification_type) d on a.product_classification_type_id = d.product_classification_type_id

LEFT JOIN (SELECT practice_type_id, name as "practice_type_name" from openchpl.practice_type) e on a.practice_type_id = e.practice_type_id

LEFT JOIN (SELECT product_version_id, version as "product_version", product_id from openchpl.product_version) f on a.product_version_id = f.product_version_id

LEFT JOIN (SELECT product_id, vendor_id, name as "product_name" FROM openchpl.product) g ON f.product_id = g.product_id

LEFT JOIN (SELECT vendor_id, name as "vendor_name" from openchpl.vendor) h on g.vendor_id = h.vendor_id

LEFT JOIN (SELECT certification_status_id, certification_status as "certification_status_name" FROM openchpl.certification_status) n on a.certification_status_id = n.certification_status_id

LEFT JOIN (SELECT DISTINCT ON (certified_product_id) certified_product_id, event_date as "certification_date" FROM openchpl.certification_event WHERE event_type_id = 1) i on a.certified_product_id = i.certified_product_id

LEFT JOIN (SELECT certified_product_id, count(*) as "count_certifications" FROM (SELECT * FROM openchpl.certification_result WHERE success = true AND deleted <> true) j GROUP BY certified_product_id) k ON a.certified_product_id = k.certified_product_id

LEFT JOIN (SELECT certified_product_id, count(*) as "count_cqms" FROM (SELECT DISTINCT ON (number, certified_product_id) * FROM openchpl.cqm_result_details WHERE success = true AND deleted <> true) l GROUP BY certified_product_id ORDER BY certified_product_id) m ON a.certified_product_id = m.certified_product_id
;

ALTER VIEW openchpl.certified_product_details OWNER TO openchpl;
