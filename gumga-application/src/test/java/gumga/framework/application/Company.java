package gumga.framework.application;


import gumga.framework.domain.GumgaModel;
import gumga.framework.domain.GumgaMultitenancy;
import javax.persistence.Entity;
import javax.persistence.SequenceGenerator;

@GumgaMultitenancy
@Entity
@SequenceGenerator(name = GumgaModel.SEQ_NAME, sequenceName = "SEQ_COMPANY")
public class Company extends GumgaModel<Long> {
	
	private String name;
	
	public Company() {
	}
	
	public Company(String name) {
		this.name = name;
	}
	
	public String getName() {
		return name;
	}
	
	public void setName(String name) {
		this.name = name;
	}

}
