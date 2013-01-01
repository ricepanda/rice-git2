--
-- Copyright 2005-2013 The Kuali Foundation
--
-- Licensed under the Educational Community License, Version 2.0 (the "License");
-- you may not use this file except in compliance with the License.
-- You may obtain a copy of the License at
--
-- http://www.opensource.org/licenses/ecl2.php
--
-- Unless required by applicable law or agreed to in writing, software
-- distributed under the License is distributed on an "AS IS" BASIS,
-- WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
-- See the License for the specific language governing permissions and
-- limitations under the License.
--

--
-- KULRICE-7378 - MySQL Upgrade script for Rice 2.0 is dropping not null constraints
--

--
-- NOTE - This is only an issue for the MySQL scripts, so that is why there is no corresponding
--        2012-010-24.sql script for Oracle.
--

ALTER TABLE KRSB_SVC_DEF_T MODIFY APPL_ID VARCHAR(255) NOT NULL;

ALTER TABLE KRSB_MSG_QUE_T MODIFY APPL_ID VARCHAR(255) NOT NULL;

ALTER TABLE KRCR_PARM_T MODIFY APPL_ID VARCHAR(255) NOT NULL;

ALTER TABLE KRMS_AGENDA_T MODIFY ACTV varchar(1) DEFAULT 'Y' NOT NULL;

ALTER TABLE KREW_DOC_TYP_ATTR_T MODIFY DOC_TYP_ID VARCHAR(40) NOT NULL;

ALTER TABLE KREW_DOC_TYP_PROC_T MODIFY DOC_TYP_ID VARCHAR(40) NOT NULL;

ALTER TABLE KREW_ACTN_ITM_T MODIFY ACTN_RQST_ID VARCHAR(40) NOT NULL;

ALTER TABLE KREW_ACTN_ITM_T MODIFY RSP_ID VARCHAR(40) NOT NULL;

ALTER TABLE KREW_ACTN_RQST_T MODIFY RSP_ID VARCHAR(40) NOT NULL;

ALTER TABLE KREW_RULE_TMPL_ATTR_T MODIFY RULE_TMPL_ID VARCHAR(40) NOT NULL;

ALTER TABLE KREW_RULE_TMPL_ATTR_T MODIFY RULE_ATTR_ID VARCHAR(40) NOT NULL;

ALTER TABLE KREW_DLGN_RSP_T MODIFY RSP_ID VARCHAR(40) NOT NULL;

ALTER TABLE KREW_DLGN_RSP_T MODIFY DLGN_RULE_BASE_VAL_ID VARCHAR(40) NOT NULL;

ALTER TABLE KREW_RULE_RSP_T MODIFY RSP_ID VARCHAR(40) NOT NULL;

ALTER TABLE KREW_RULE_RSP_T MODIFY RULE_ID VARCHAR(40) NOT NULL;

ALTER TABLE KREW_RULE_EXT_T MODIFY RULE_TMPL_ATTR_ID VARCHAR(40) NOT NULL;

ALTER TABLE KREW_RULE_EXT_T MODIFY RULE_ID VARCHAR(40) NOT NULL;

ALTER TABLE KREW_RTE_NODE_INSTN_T MODIFY RTE_NODE_ID VARCHAR(40) NOT NULL;

ALTER TABLE KREW_RTE_BRCH_ST_T MODIFY RTE_BRCH_ID VARCHAR(40) NOT NULL;

ALTER TABLE KREW_RTE_NODE_INSTN_ST_T MODIFY RTE_NODE_INSTN_ID VARCHAR(40) NOT NULL;

ALTER TABLE KREW_DOC_TYP_ATTR_T MODIFY  DOC_TYP_ID VARCHAR(40) NOT NULL;

ALTER TABLE KREW_DOC_TYP_ATTR_T MODIFY RULE_ATTR_ID VARCHAR(40) NOT NULL;

ALTER TABLE KREW_OUT_BOX_ITM_T MODIFY ACTN_RQST_ID VARCHAR(40) NOT NULL;

ALTER TABLE KREW_OUT_BOX_ITM_T MODIFY RSP_ID VARCHAR(40) NOT NULL;

ALTER TABLE KREW_RTE_NODE_CFG_PARM_T MODIFY RTE_NODE_ID VARCHAR(40) NOT NULL;

ALTER TABLE KREW_RULE_EXT_VAL_T MODIFY RULE_EXT_ID VARCHAR(40) NOT NULL;