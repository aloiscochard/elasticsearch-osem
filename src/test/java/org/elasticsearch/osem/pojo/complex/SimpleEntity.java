package org.elasticsearch.osem.pojo.complex;

import java.util.ArrayList;
import java.util.Collection;

import org.elasticsearch.osem.annotations.Index;
import org.elasticsearch.osem.annotations.Indexable;
import org.elasticsearch.osem.annotations.Searchable;

@Searchable
public class SimpleEntity {
	private Long id;

	private String field;

	@Indexable(index=Index.NA)
	private String ignorefield;

	@Indexable(index=Index.ANALYZED,analyzer="french")
	private String frenchfield;

	private Collection<String> stringsfield = new ArrayList<String>(); 
	
	private Collection<ChildEntity> sentities = new ArrayList<ChildEntity>(); 
	
	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public Collection<ChildEntity> getSentities() {
		return sentities;
	}

	public void setSentities(Collection<ChildEntity> sentities) {
		this.sentities = sentities;
	}

	public void addToSentities(ChildEntity entity) {
		this.sentities.add(entity);
	}

	public String getField() {
		return field;
	}

	public void setField(String field) {
		this.field = field;
	}

	public Collection<String> getStringsfield() {
		return stringsfield;
	}

	public void setStringsfield(Collection<String> stringsfield) {
		this.stringsfield = stringsfield;
	}

	public void addToStringsfield(String string) {
		this.stringsfield.add(string);
	}
	
	public String getIgnorefield() {
		return ignorefield;
	}
	
	public void setIgnorefield(String ignorefield) {
		this.ignorefield = ignorefield;
	}
	
	public String getFrenchfield() {
		return frenchfield;
	}
	
	public void setFrenchfield(String frenchfield) {
		this.frenchfield = frenchfield;
	}
}
