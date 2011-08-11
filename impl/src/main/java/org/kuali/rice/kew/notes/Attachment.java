/*
 * Copyright 2005-2008 The Kuali Foundation
 * 
 * 
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * http://www.opensource.org/licenses/ecl2.php
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.kuali.rice.kew.notes;

import java.io.InputStream;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.PrePersist;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.persistence.Version;

import org.kuali.rice.core.jpa.annotations.Sequence;
import org.kuali.rice.core.util.OrmUtils;
import org.kuali.rice.kns.service.KNSServiceLocator;

/**
 * An attachment which is attached to a {@link Note}.
 * 
 * @see Note
 *
 * @author Kuali Rice Team (rice.collab@kuali.org)
 */
@Entity
@Table(name="KREW_ATT_T")
@Sequence(name="KREW_DOC_NTE_S",property="attachmentId")
@NamedQueries({
	@NamedQuery(name="Attachment.FindAttachmentById",query="select a from Attachment as a where a.attachmentId = :attachmentId")
})
public class Attachment {

	@Id
	@Column(name="ATTACHMENT_ID")
	private Long attachmentId;
	@Transient
	private Long noteId;
	@Column(name="FILE_NM")
	private String fileName;
	@Column(name="FILE_LOC")
	private String fileLoc;
	@Column(name="MIME_TYP")
	private String mimeType;
	@Version
	@Column(name="VER_NBR")
	private Integer lockVerNbr;
    @Transient
	private InputStream attachedObject;
	@ManyToOne(fetch=FetchType.EAGER)
	@JoinColumn(name="NTE_ID")
	private Note note;
	
	public Long getAttachmentId() {
		return attachmentId;
	}
	public void setAttachmentId(Long attachmentId) {
		this.attachmentId = attachmentId;
	}
	public String getFileLoc() {
		return fileLoc;
	}
	public void setFileLoc(String fileLoc) {
		this.fileLoc = fileLoc;
	}
	public String getFileName() {
		return fileName;
	}
	public void setFileName(String fileName) {
		this.fileName = fileName;
	}
	public Integer getLockVerNbr() {
		return lockVerNbr;
	}
	public void setLockVerNbr(Integer lockVerNbr) {
		this.lockVerNbr = lockVerNbr;
	}
	public String getMimeType() {
		return mimeType;
	}
	public void setMimeType(String mimeType) {
		this.mimeType = mimeType;
	}
	public Long getNoteId() {
		//noteId field not mapped in JPA 
		if (noteId == null && note != null){
			return note.getNoteId();
		}
		return noteId;
	}
	public void setNoteId(Long noteId) {
		this.noteId = noteId;
	}
	public Note getNote() {
		return note;
	}
	public void setNote(Note note) {
		this.note = note;
	}
	public InputStream getAttachedObject() {
		return attachedObject;
	}
	public void setAttachedObject(InputStream attachedObject) {
		this.attachedObject = attachedObject;
	}
	
	@PrePersist
	public void beforeInsert(){
		OrmUtils.populateAutoIncValue(this, KNSServiceLocator.getEntityManagerFactory().createEntityManager());		
	}

}

