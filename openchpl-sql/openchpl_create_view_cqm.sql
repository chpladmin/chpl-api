CREATE OR REPLACE VIEW openchpl.cqm_result_details AS

SELECT

a.cqm_result_id,
a.certified_product_id,
a.success,
a.cqm_criterion_id,
a.deleted,
b.number,
b.cms_id,
b.title,
b.description,
b.cqm_domain,
b.nqf_number,
b.cqm_criterion_type_id,
c.cqm_version_id,
c.version

FROM openchpl.cqm_result a

LEFT JOIN openchpl.cqm_criterion b ON a.cqm_criterion_id = b.cqm_criterion_id

LEFT JOIN openchpl.cqm_version c ON b.cqm_version_id = c.cqm_version_id;

ALTER VIEW openchpl.cqm_result_details OWNER TO openchpl;
