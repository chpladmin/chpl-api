CREATE OR REPLACE VIEW openchpl.certification_result_details AS

SELECT

a.certification_result_id,
a.certified_product_id,
a.certification_criterion_id,
a.success,
a.deleted,
b.number,
b.title

FROM openchpl.certification_result a

LEFT JOIN (SELECT certification_criterion_id, number, title FROM openchpl.certification_criterion) b

ON a.certification_criterion_id = b.certification_criterion_id;

ALTER VIEW openchpl.certification_result_details OWNER TO openchpl;
