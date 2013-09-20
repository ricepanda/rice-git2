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

-- assign 'WorkflowAdmin' to 'Technical Administrator' Role
INSERT INTO KRIM_ROLE_MBR_T(ROLE_MBR_ID, VER_NBR, OBJ_ID, ROLE_ID, MBR_ID, MBR_TYP_CD)
VALUES('1500', 1, '2eae152e-76e2-102c-b815-701c3cd98d38', '63', '1', 'G')
;

INSERT INTO KRIM_RSP_T(RSP_ID, OBJ_ID, RSP_TMPL_ID, nm, DESC_TXT, nmspc_cd, ACTV_IND) 
  VALUES('93', '5B4F0974284DEF33ED404F8189D44F24', '2', '93RSPNAME', null, 'KR-SYS', 'Y')
;
INSERT INTO KRIM_ROLE_RSP_T(ROLE_RSP_ID, OBJ_ID, VER_NBR, ROLE_ID, RSP_ID, ACTV_IND)
  VALUES('1080', '5DF45238F5528846E0404F8189D840B8', 1, '63', '93', 'Y')
;
INSERT INTO KRIM_RSP_ATTR_DATA_T (ATTR_DATA_ID, OBJ_ID, VER_NBR, RSP_ID, KIM_TYP_ID, KIM_ATTR_DEFN_ID, ATTR_VAL)
  VALUES('334', '5D8B0E3E634E96A3E0404F8189D8468D', 1, '93', '54', '13', 'RiceDocument')
;
INSERT INTO KRIM_RSP_T(RSP_ID, OBJ_ID, RSP_TMPL_ID, nm, DESC_TXT, nmspc_cd, ACTV_IND) VALUES('13', '5B4F09743F4DEF33ED404F8189D44F24', '2', '13RSPNAME', null, 'KR-SYS', 'Y')
;
INSERT INTO KRIM_ROLE_RSP_T(ROLE_RSP_ID, OBJ_ID, VER_NBR, ROLE_ID, RSP_ID, ACTV_IND) VALUES('999', '5DF45238F5528FD6E0404F8189D840B8', 1, '63', '13', 'Y')
;
INSERT INTO KRIM_RSP_ATTR_DATA_T (ATTR_DATA_ID, OBJ_ID, VER_NBR, RSP_ID, KIM_TYP_ID, KIM_ATTR_DEFN_ID, ATTR_VAL) VALUES('11', '5D8B0E3E634E96A3E02F4F8189D8468D', 1, '13', '54', '13', 'TestFinalApproverDocumentType')
;
INSERT INTO KRIM_ROLE_RSP_ACTN_T (ROLE_RSP_ACTN_ID, OBJ_ID, VER_NBR, ACTN_TYP_CD, PRIORITY_NBR, ACTN_PLCY_CD, ROLE_MBR_ID, ROLE_RSP_ID, FRC_ACTN) VALUES('200', '5D8B0E3E620096A3E0404F8189D8468D', 1, 'A', null, 'F', '*', '999', 'N')
;
INSERT INTO KRIM_RSP_T(RSP_ID, OBJ_ID, RSP_TMPL_ID, nm, DESC_TXT, nmspc_cd, ACTV_IND) VALUES('14', '5B4F09743F4DEF33ED404F8189D44F25', '2', '14RSPNAME', null, 'KR-SYS', 'Y')
;
INSERT INTO KRIM_ROLE_RSP_T(ROLE_RSP_ID, OBJ_ID, VER_NBR, ROLE_ID, RSP_ID, ACTV_IND) VALUES('9999', '5DF45238F5528FD6E0404F8189D840B9', 1, '63', '14', 'Y')
;
INSERT INTO KRIM_RSP_ATTR_DATA_T (ATTR_DATA_ID, OBJ_ID, VER_NBR, RSP_ID, KIM_TYP_ID, KIM_ATTR_DEFN_ID, ATTR_VAL) VALUES('12', '5D8B0E3E634E96A3E02F4F8189D8468E', 1, '14', '54', '13', 'RiceDocument')
;
INSERT INTO KRIM_ROLE_RSP_ACTN_T (ROLE_RSP_ACTN_ID, OBJ_ID, VER_NBR, ACTN_TYP_CD, PRIORITY_NBR, ACTN_PLCY_CD, ROLE_MBR_ID, ROLE_RSP_ID, FRC_ACTN) VALUES('201', '5D8B0E3E620096A3E0404F8189D8468E', 1, 'A', null, 'F', '*', '9999', 'N')
;
