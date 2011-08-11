/*
 * Copyright 2007-2008 The Kuali Foundation
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
package org.kuali.rice.kns.dao.proxy;

import java.util.ArrayList;

import org.kuali.rice.core.util.OrmUtils;
import org.kuali.rice.kns.bo.Note;
import org.kuali.rice.kns.dao.NoteDao;
import org.springframework.dao.DataAccessException;

public class NoteDaoProxy implements NoteDao {

    private NoteDao noteDaoJpa;
    private NoteDao noteDaoOjb;
	
    private NoteDao getDao(Class clazz) {
    	return (OrmUtils.isJpaAnnotated(clazz) && OrmUtils.isJpaEnabled()) ? noteDaoJpa : noteDaoOjb; 
    }
    
    public void save(Note note) throws DataAccessException {
		getDao(Note.class).save(note);
	}

	public void deleteNote(Note note) throws DataAccessException {
		getDao(Note.class).deleteNote(note);
	}
	
    public Note getNoteByNoteId(Long noteId){
    	return getDao(Note.class).getNoteByNoteId(noteId);
    }

	public ArrayList findByremoteObjectId(String remoteObjectId) {
		return getDao(Note.class).findByremoteObjectId(remoteObjectId);
	}

	public void setNoteDaoJpa(NoteDao noteDaoJpa) {
		this.noteDaoJpa = noteDaoJpa;
	}

	public void setNoteDaoOjb(NoteDao noteDaoOjb) {
		this.noteDaoOjb = noteDaoOjb;
	}

}
