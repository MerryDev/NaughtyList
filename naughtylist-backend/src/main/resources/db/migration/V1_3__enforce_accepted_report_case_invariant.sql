ALTER TABLE report
    ADD CONSTRAINT chk_report_accepted_requires_case CHECK (status <> 'ACCEPTED' OR case_id IS NOT NULL);