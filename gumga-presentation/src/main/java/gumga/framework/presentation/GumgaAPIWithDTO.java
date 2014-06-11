package gumga.framework.presentation;

import gumga.framework.core.GumgaIdable;
import gumga.framework.core.QueryObject;
import gumga.framework.core.SearchResult;
import gumga.framework.domain.GumgaService;
import gumga.framework.validation.exception.InvalidEntityException;

import java.lang.reflect.Constructor;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseStatus;

public abstract class GumgaAPIWithDTO<T> {
	
	protected final Logger logger = LoggerFactory.getLogger(getClass());
	
	public void beforeSearch(QueryObject query) { }
	public void afterSearch(QueryObject query) { }
	
	@SuppressWarnings("unchecked")
	@RequestMapping
	public SearchResult<T> pesquisa(QueryObject query) {
		beforeSearch(query);
		SearchResult<?> pesquisa = service().pesquisa(query);
		afterSearch(query);
		 
		return new SearchResult<T>(query, pesquisa.getCount(), translator().from((List<GumgaIdable>) pesquisa.getValues()));
	}
	
	public void beforeCreate(T entity) { }
	public void afterCreate(T entity) { }

	@Transactional
	@RequestMapping(method = RequestMethod.POST)
	public T save(@RequestBody @Valid T model, BindingResult result) {
		beforeCreate(model);
		T entity = saveOrCry(model, result);
		afterCreate(entity);
		
		return entity;
	}
	
	public void beforeLoad() { 	}
	public void afterLoad(T entity) { }

	@RequestMapping("/{id}")
	public T carrega(@PathVariable Long id) {
		beforeLoad();
		T entity = (T) translator().from(service().view(id));
		afterLoad(entity);
		
		return entity;
	}
	
	public void beforeUpdate(T entity) { }
	public void afterUpdate(T entity) { }

	@Transactional
	@RequestMapping(value = "/{id}", method = RequestMethod.PUT, consumes = "application/json")
	public T update(@PathVariable("id") Long id, @RequestBody T model, BindingResult result) {
		beforeUpdate(model);
		T entity = saveOrCry(model, result);
		afterUpdate(entity);
		
		return entity;
	}
	
	public void beforeDelete(T entity) { }
	public void afterDelete() { }

	@Transactional
	@ResponseStatus(value = HttpStatus.OK)
	@RequestMapping(value = "/{id}", method = RequestMethod.DELETE)
	public void delete(@PathVariable Long id, HttpServletRequest request) {
		GumgaIdable entity = service().view(id);
		
		beforeDelete(translator().from(entity));
		service().delete(entity);
		afterDelete();
	}

	@RequestMapping("/new")
	public T initialState() {
		return initialValue();
	}

	protected T initialValue() {
		try {
			@SuppressWarnings("unchecked")
			Constructor<T> constructor = (Constructor<T>) translator().dtoClass().getDeclaredConstructors()[0];
			constructor.setAccessible(true);
			return constructor.newInstance();
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
	
	@SuppressWarnings("unchecked")
	private <X extends GumgaIdable> T saveOrCry(T model, BindingResult result) {
		if (result.hasErrors())
			throw new InvalidEntityException(result);

		return translator().from(service().save((X) translator().to(model)));
	}
	
	public abstract <X extends GumgaIdable> GumgaService<X> service();
	public abstract <X extends GumgaIdable> GumgaTranslator<X, T> translator();
	
}
